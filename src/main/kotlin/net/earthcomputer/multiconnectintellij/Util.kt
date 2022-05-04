package net.earthcomputer.multiconnectintellij

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.RecursionManager
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiField
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiModifier
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.searches.ClassInheritorsSearch
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.isAncestor
import com.intellij.psi.util.parentOfType
import net.earthcomputer.multiconnectintellij.csv.CsvFileType
import net.earthcomputer.multiconnectintellij.csv.CsvReferenceContributor
import net.earthcomputer.multiconnectintellij.csv.psi.CsvFile

val Project.protocolsFile: CsvFile? get() {
    return CachedValuesManager.getManager(this).getCachedValue(this) {
        var file: CsvFile? = null
        FileTypeIndex.processFiles(CsvFileType, {
            if (it.name == "protocols.csv" && it.parent?.name == "data") {
                file = PsiManager.getInstance(this).findFile(it) as? CsvFile
                false
            } else {
                true
            }
        }, GlobalSearchScope.projectScope(this))
        CachedValueProvider.Result(file, file ?: PsiModificationTracker.MODIFICATION_COUNT)
    }
}

val Project.protocolVersions: List<Int> get() {
    val protocolsFile = protocolsFile ?: return emptyList()
    return CachedValuesManager.getCachedValue(protocolsFile) {
        CachedValueProvider.Result(
            protocolsFile.rows.mapNotNull { it.getEntry("id")?.text?.toIntOrNull() }.toSortedSet().toList(),
            protocolsFile
        )
    }
}

fun Project.getProtocolName(id: Int): String? {
    return protocolsFile?.getRowByKey("id", id.toString())?.getEntry("name")?.text
}

enum class PacketDirection {
    CLIENTBOUND, SERVERBOUND, BOTH
}

fun getPacketDirection(clazz: PsiClass): PacketDirection? {
    return findAllPacketUsers(clazz).asSequence().flatMap { packet_ ->
        var packet = packet_

        if (packet.hasAnnotation(Constants.MESSAGE_VARIANT)) {
            val group = packet.interfaces.singleOrNull()
            if (group != null && group.hasAnnotation(Constants.MESSAGE)) {
                packet = group
            }
        }

        if (packet.hasAnnotation(Constants.MESSAGE_VARIANT)) {
            sequenceOf(packet)
        } else {
            ClassInheritorsSearch.search(packet).asSequence().filter { it.hasAnnotation(Constants.MESSAGE_VARIANT) }
        }
    }.flatMap { packet ->
        ReferencesSearch.search(packet).asSequence().filter {
            CsvReferenceContributor.protocolClassPattern.accepts(it.element)
        }
    }.mapNotNull { reference ->
        reference.element.containingFile?.virtualFile?.let {
            when (it.name) {
                "cpackets.csv" -> PacketDirection.SERVERBOUND
                "spackets.csv" -> PacketDirection.CLIENTBOUND
                else -> null
            }
        }
    }.reduceOrNull { a, b ->
        if (a == b) {
            a
        } else {
            PacketDirection.BOTH
        }
    }
}

fun isPacket(clazz: PsiClass): Boolean {
    return CachedValuesManager.getCachedValue(clazz) {
        CachedValueProvider.Result(
            clazz.hasAnnotation(Constants.MESSAGE_VARIANT) && ReferencesSearch.search(clazz).anyMatch {
                CsvReferenceContributor.protocolClassPattern.accepts(it.element)
            },
            PsiModificationTracker.MODIFICATION_COUNT
        )
    }
}

fun IntRange.intersectRange(other: IntRange): IntRange {
    val start = maxOf(first, other.first)
    val end = minOf(last, other.last)
    return start..end
}

fun findVariantUsages(classToSearch: PsiClass): List<PsiField> {
    return CachedValuesManager.getCachedValue(classToSearch) {
        val group = classToSearch.interfaces.singleOrNull()?.takeIf { classToSearch.hasAnnotation(Constants.MESSAGE_VARIANT) && it.hasAnnotation(Constants.MESSAGE) }
        val candidates = group?.let { ReferencesSearch.search(it) + ReferencesSearch.search(classToSearch) }
            ?: ReferencesSearch.search(classToSearch)
        val result = candidates.mapNotNull { ref ->
            val element = ref.element
            // PsiField -> PsiTypeElement -> PsiTypeReferenceElement
            val field = element.parent?.parent as? PsiField ?: return@mapNotNull null
            if (field.typeElement?.isAncestor(element) != true) {
                return@mapNotNull null
            }
            if (field.hasModifierProperty(PsiModifier.STATIC)) {
                return@mapNotNull null
            }
            val clazz = field.parentOfType<PsiClass>() ?: return@mapNotNull null
            if (!clazz.hasAnnotation(Constants.MESSAGE_VARIANT)) {
                return@mapNotNull null
            }
            field
        }
        CachedValueProvider.Result(result, PsiModificationTracker.MODIFICATION_COUNT)
    }
}

fun findAllPacketUsers(clazz: PsiClass): Collection<PsiClass> {
    return CachedValuesManager.getCachedValue(clazz) {
        CachedValueProvider.Result(doFindAllPacketUsers(clazz, getVersionRange(clazz)), PsiModificationTracker.MODIFICATION_COUNT)
    }
}

private fun doFindAllPacketUsers(clazz: PsiClass, versionRange: IntRange): Collection<PsiClass> {
    val packetUsers = mutableSetOf<PsiClass>()

    RecursionManager.doPreventingRecursion(clazz to versionRange, true) {
        if (isPacket(clazz)) {
            packetUsers += clazz
        }
        for (field in findVariantUsages(clazz)) {
            val referencingClass = field.containingClass ?: continue
            val referencingVersionRange = getVersionRange(referencingClass).intersectRange(versionRange)
            if (!referencingVersionRange.isEmpty()) {
                packetUsers += doFindAllPacketUsers(referencingClass, referencingVersionRange)
            }
        }
    }

    return packetUsers
}

fun getVersionRange(clazz: PsiClass): IntRange {
    return CachedValuesManager.getCachedValue(clazz) {
        val annotation = clazz.getAnnotation(Constants.MESSAGE_VARIANT)
            ?: return@getCachedValue CachedValueProvider.Result(Int.MIN_VALUE..Int.MAX_VALUE, clazz)
        val constantEvaluator = JavaPsiFacade.getInstance(clazz.project).constantEvaluationHelper
        var minVersion = annotation.findAttributeValue("minVersion")
            ?.let { constantEvaluator.computeConstantExpression(it) } as? Int ?: Int.MIN_VALUE
        if (minVersion == -1) {
            minVersion = Int.MIN_VALUE
        }
        var maxVersion = annotation.findAttributeValue("maxVersion")
            ?.let { constantEvaluator.computeConstantExpression(it) } as? Int ?: Int.MAX_VALUE
        if (maxVersion == -1) {
            maxVersion = Int.MAX_VALUE
        }
        CachedValueProvider.Result(minVersion..maxVersion, annotation)
    }
}

private fun getVariantProviderUncached(clazz: PsiClass): VariantProvider? {
    val classToSearch = if (clazz.hasAnnotation(Constants.MESSAGE_VARIANT)) {
        clazz.interfaces.singleOrNull() ?: return VariantProvider(listOf(VariantInfo(Int.MIN_VALUE..Int.MAX_VALUE, clazz)))
    } else {
        clazz
    }
    if (!classToSearch.hasAnnotation(Constants.MESSAGE)) {
        return null
    }
    val result = ClassInheritorsSearch.search(classToSearch)
        .mapNotNull { variant ->
            VariantInfo(getVersionRange(variant), variant)
        }
        .toMutableList()
    result.sortBy { it.versions.first }
    return VariantProvider(result)
}

fun getVariantProviderPossiblyInvalid(clazz: PsiClass): VariantProvider? {
    return CachedValuesManager.getCachedValue(clazz) {
        CachedValueProvider.Result(getVariantProviderUncached(clazz), PsiModificationTracker.MODIFICATION_COUNT)
    }
}

fun getVariantProvider(clazz: PsiClass): VariantProvider? {
    return getVariantProviderPossiblyInvalid(clazz)?.takeIf(VariantProvider::isValid)
}

class VariantProvider(private val variants: List<VariantInfo>) {
    fun isValid(): Boolean {
        if (variants.isEmpty()) {
            return false
        }
        if (variants.firstOrNull()?.versions?.isEmpty() == true) {
            return false
        }
        for (i in variants.indices.drop(1)) {
            if (variants[i - 1].versions.last >= variants[i].versions.first) {
                return false
            }
            if (variants[i].versions.isEmpty()) {
                return false
            }
        }
        return true
    }

    fun getVariant(version: Int): VariantInfo? {
        var index = variants.binarySearchBy(version) { it.versions.first }
        if (index < 0) {
            index = -index - 2
        }
        return variants.getOrNull(index)?.takeIf { version <= it.versions.last }
    }
}

data class VariantInfo(val versions: IntRange, val clazz: PsiClass)
