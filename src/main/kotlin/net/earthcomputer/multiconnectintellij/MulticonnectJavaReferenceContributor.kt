package net.earthcomputer.multiconnectintellij

import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.patterns.PsiJavaElementPattern
import com.intellij.patterns.PsiJavaPatterns.psiAnnotation
import com.intellij.patterns.PsiJavaPatterns.psiClass
import com.intellij.patterns.PsiJavaPatterns.psiLiteral
import com.intellij.patterns.PsiJavaPatterns.psiNameValuePair
import com.intellij.patterns.PsiNameValuePairPattern
import com.intellij.patterns.StandardPatterns.*
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiEnumConstant
import com.intellij.psi.PsiLiteral
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.psi.PsiType
import com.intellij.psi.ResolveResult
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.createSmartPointer
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import net.earthcomputer.multiconnectintellij.Constants.MINECRAFT_IDENTIFIER
import net.earthcomputer.multiconnectintellij.csv.psi.CsvFile
import net.earthcomputer.multiconnectintellij.inspection.McTypes

interface ErrorOnUnresolved

private fun PsiNameValuePairPattern.insideAnnotation(qName: String): PsiNameValuePairPattern {
    return inside(psiAnnotation().qName(qName))
}

class MulticonnectJavaReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            StringReferenceProvider.ELEMENT_PATTERN,
            StringReferenceProvider
        )
        registrar.registerReferenceProvider(
            StringValueReferenceProvider.ELEMENT_PATTERN,
            StringValueReferenceProvider
        )
    }
}

object StringReferenceProvider : PsiReferenceProvider() {
    val ELEMENT_PATTERN: PsiJavaElementPattern.Capture<PsiLiteral> =
        psiLiteral(string()).inside(psiClass().withAnnotation(Constants.MESSAGE_VARIANT))
            .inside(or(
                psiNameValuePair().withName("value").insideAnnotation(Constants.ARGUMENT),
                psiNameValuePair().withName("compute").insideAnnotation(Constants.DEFAULT_CONSTRUCT),
                psiNameValuePair().withName("compute").insideAnnotation(Constants.INTRODUCE),
                psiNameValuePair().withName("compute").insideAnnotation(Constants.LENGTH),
                psiNameValuePair().withName("value").insideAnnotation(Constants.ONLY_IF),
                psiNameValuePair().withName("condition").insideAnnotation(Constants.POLYMORPHIC),
                psiNameValuePair().withName("field").insideAnnotation(Constants.POLYMORPHIC_BY),
                psiNameValuePair().withName("preprocess").insideAnnotation(Constants.DATAFIX),
            ))

    override fun getReferencesByElement(
        element: PsiElement,
        context: ProcessingContext
    ): Array<PsiReference> {
        if (element !is PsiLiteral) {
            return PsiReference.EMPTY_ARRAY
        }
        val text = element.text
        if (text.length < 2) {
            return PsiReference.EMPTY_ARRAY
        }

        // create a separate reference for each part delimited by .
        val result = mutableListOf<PsiReference>()
        var endIndex = 0
        var startIndex: Int
        var parent: StringReference? = null
        do {
            startIndex = endIndex
            endIndex = text.indexOf('.', startIndex = endIndex + 1)
            if (endIndex == -1) {
                endIndex = text.length - 1
            }

            val range = TextRange(startIndex + 1, endIndex)
            val referenceName = StringUtil.unescapeStringCharacters(range.substring(text))
            val reference = StringReference(element, range, referenceName, parent, endIndex == text.length - 1)

            result += reference
            parent = reference
        } while (endIndex != text.length - 1)

        return result.toTypedArray()
    }
}

class StringReference(
    literal: PsiLiteral,
    range: TextRange,
    private val referenceName: String,
    private val parent: StringReference?,
    private val isLast: Boolean
) : PsiReferenceBase<PsiLiteral>(literal, range), PsiPolyVariantReference, ErrorOnUnresolved {
    override fun resolve(): PsiElement? {
        return this.multiResolve(false).singleOrNull()?.element
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        return doResolve().distinctBy { it.element }.map { PsiElementResolveResult(it.element) }.toTypedArray()
    }

    private fun doResolve(): List<Result> {
        val element = element
        val annotationName = element.parentOfType<PsiAnnotation>()?.qualifiedName ?: return emptyList()
        val clazz = element.parentOfType<PsiClass>() ?: return emptyList()
        return when (annotationName) {
            Constants.ARGUMENT -> {
                val contextClasses = getArgumentContextClasses(element)
                if (isLast) {
                    contextClasses.mapNotNull { contextClass ->
                        contextClass.clazz.findFieldByName(referenceName, true)?.let { Result(it, contextClass.versions) }
                    }
                } else {
                    if (referenceName != "outer") {
                        return emptyList()
                    }
                    contextClasses.flatMap { contextClass ->
                        findVariantUsages(contextClass.clazz).mapNotNull { field ->
                            val usage = field.parentOfType<PsiClass>() ?: return@mapNotNull null
                            val range = contextClass.versions.intersectRange(getVersionRange(usage))
                            if (range.isEmpty()) {
                                return@mapNotNull null
                            }
                            Result(usage, range)
                        }
                    }.distinct()
                }
            }
            Constants.POLYMORPHIC_BY -> {
                if (!isLast) {
                    return emptyList()
                }
                listOfNotNull(clazz.findFieldByName(referenceName, true)
                    ?.let { Result(it, getVersionRange(clazz)) })
            }
            else -> {
                if (!isLast) {
                    return emptyList()
                }
                clazz.findMethodsByName(referenceName, true)
                    .filter { it.hasModifierProperty(PsiModifier.STATIC) }
                    .map { Result(it, getVersionRange(clazz)) }
            }
        }
    }

    private fun getArgumentContextClasses(element: PsiElement): List<VariantInfo> {
        val method = element.parentOfType<PsiMethod>() ?: return emptyList()
        val result = ReferencesSearch.search(method).asSequence().filterIsInstance<StringReference>().flatMap { reference ->
            val refElt = reference.element
            val refAnn = refElt.parentOfType<PsiAnnotation>() ?: return@flatMap emptyList()
            val refAnnName = refAnn.qualifiedName ?: return@flatMap emptyList()
            when {
                parent != null -> parent.doResolve().mapNotNull { result ->
                    (result.element as? PsiClass)?.let { VariantInfo(result.versionRange, it) }
                }
                refAnnName == Constants.INTRODUCE -> {
                    val containingClass = refElt.parentOfType<PsiClass>() ?: return@flatMap emptyList()
                    val direction = when (refAnn.getEnumConstant("direction")?.name) {
                        "FROM_NEWER" -> PacketDirection.SERVERBOUND
                        "FROM_OLDER" -> PacketDirection.CLIENTBOUND
                        else -> null
                    } ?: getPacketDirection(containingClass)?.takeIf { it != PacketDirection.BOTH }
                    ?: return@flatMap emptyList()
                    val variantProvider = getVariantProvider(containingClass) ?: return@flatMap emptyList()
                    val versionRange = getVersionRange(containingClass)
                    if ((direction == PacketDirection.CLIENTBOUND && versionRange.first == Int.MIN_VALUE)
                        || (direction == PacketDirection.SERVERBOUND && versionRange.last == Int.MAX_VALUE)) {
                        return@flatMap emptyList()
                    }
                    val allProtocols = refElt.project.protocolVersions
                    val previousProtocol = if (direction == PacketDirection.CLIENTBOUND) {
                        allProtocols.getOrNull(allProtocols.binarySearch(versionRange.first) - 1)
                    } else {
                        allProtocols.getOrNull(allProtocols.binarySearch(versionRange.last) + 1)
                    } ?: return@flatMap emptyList()
                    val variant = variantProvider.getVariant(previousProtocol) ?: return@flatMap emptyList()
                    listOf(VariantInfo(previousProtocol..previousProtocol, variant.clazz))
                }
                else -> listOfNotNull(refElt.parentOfType<PsiClass>()?.let { VariantInfo(getVersionRange(it), it) })
            }
        }.toMutableList()

        if (method.hasAnnotation(Constants.HANDLER) || method.hasAnnotation(Constants.PARTIAL_HANDLER)) {
            val clazz = method.containingClass ?: return result
            result += VariantInfo(getVersionRange(clazz), clazz)
        }

        return result
    }

    override fun getVariants(): Array<Any> {
        val element = element
        val annotationName = element.parentOfType<PsiAnnotation>()?.qualifiedName ?: return emptyArray()
        val clazz = element.parentOfType<PsiClass>() ?: return emptyArray()

        return when (annotationName) {
            Constants.ARGUMENT -> {
                getArgumentContextClasses(element)
                    .flatMap { it.clazz.allFields.toList() }
                    .filter { !it.hasModifierProperty(PsiModifier.STATIC) }
                    .distinct()
                    .toTypedArray()
            }
            Constants.POLYMORPHIC_BY -> clazz.allFields
                .filter { !it.hasModifierProperty(PsiModifier.STATIC) }
                .toTypedArray()
            else -> clazz.allMethods
                .filter { it.hasModifierProperty(PsiModifier.STATIC) }
                .toTypedArray()
        }
    }

    data class Result(val element: PsiElement, val versionRange: IntRange)
}

object StringValueReferenceProvider : PsiReferenceProvider() {
    val ELEMENT_PATTERN: PsiJavaElementPattern.Capture<PsiLiteral> =
        psiLiteral(string())
            .inside(psiClass().withAnnotation(Constants.MESSAGE_VARIANT))
            .inside(or(
                psiNameValuePair().withName("stringValue").insideAnnotation(Constants.DEFAULT_CONSTRUCT),
                psiNameValuePair().withName("stringValue").insideAnnotation(Constants.INTRODUCE),
                psiNameValuePair().withName("stringValue").insideAnnotation(Constants.POLYMORPHIC),
            ))

    override fun getReferencesByElement(element: PsiElement, context: ProcessingContext): Array<PsiReference> {
        if (element !is PsiLiteral) {
            return PsiReference.EMPTY_ARRAY
        }

        val annotation = element.parentOfType<PsiAnnotation>()?.qualifiedName ?: return PsiReference.EMPTY_ARRAY
        val contextField = when (annotation) {
            Constants.POLYMORPHIC -> element.parentOfType<PsiClass>()
                ?.superClass?.takeIf { it.hasAnnotation(Constants.MESSAGE_VARIANT) }
                ?.fields?.firstOrNull { !it.hasModifierProperty(PsiModifier.STATIC) } ?: return PsiReference.EMPTY_ARRAY
            else -> element.parentOfType() ?: return PsiReference.EMPTY_ARRAY
        }

        val fieldType = McTypes.getDeepComponentType(contextField.type)
        val fieldClass = (fieldType as? PsiClassType)?.resolve()

        val rangeInElement = TextRange(1, element.textLength - 1)

        val reference = when {
            fieldClass?.hasAnnotation(Constants.NETWORK_ENUM) == true -> EnumConstantReference(element, rangeInElement, fieldClass.createSmartPointer())
            contextField.hasAnnotation(Constants.REGISTRY)
                    && (fieldType == PsiType.BYTE
                        || fieldType == PsiType.SHORT
                        || fieldType == PsiType.INT
                        || fieldType == PsiType.LONG
                        || fieldClass?.qualifiedName == MINECRAFT_IDENTIFIER
                    ) -> {
                val clazz = contextField.containingClass ?: return PsiReference.EMPTY_ARRAY
                val registryValue = contextField.getAnnotation(Constants.REGISTRY)?.getEnumConstant("value") ?: return PsiReference.EMPTY_ARRAY
                val registryName = registryValue.name.lowercase()
                RegistryReference(element, rangeInElement, getVersionRange(clazz), registryName)
            }
            else -> return PsiReference.EMPTY_ARRAY
        }

        return arrayOf(reference)
    }
}

class EnumConstantReference(
    literal: PsiLiteral,
    range: TextRange,
    private val enum: SmartPsiElementPointer<PsiClass>
) : PsiReferenceBase<PsiLiteral>(literal, range), ErrorOnUnresolved {
    override fun resolve(): PsiElement? {
        val text = element.value as? String ?: return null
        val enum = this.enum.element?.takeIf { it.hasAnnotation(Constants.NETWORK_ENUM) } ?: return null
        return enum.findFieldByName(text, false)?.takeIf { it is PsiEnumConstant }
    }

    override fun getVariants(): Array<Any> {
        val enum = this.enum.element?.takeIf { it.hasAnnotation(Constants.NETWORK_ENUM) } ?: return emptyArray()
        return enum.fields.filterIsInstance<PsiEnumConstant>().toTypedArray()
    }
}

class RegistryReference(
    literal: PsiLiteral,
    range: TextRange,
    private val versionRange: IntRange,
    private val registry: String,
) : PsiReferenceBase<PsiLiteral>(literal, range), PsiPolyVariantReference, ErrorOnUnresolved {
    override fun resolve(): PsiElement? {
        return multiResolve(false).singleOrNull()?.element
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        val element = element
        val text = element.value as? String ?: return ResolveResult.EMPTY_ARRAY
        val (namespace, path) = if (text.contains(':')) {
            val pair = text.split(':', limit = 2)
            pair[0] to pair[1]
        } else {
            "minecraft" to text
        }

        return element.project.protocolVersions.asSequence()
            .filter { it in versionRange }
            .mapNotNull { element.project.getProtocolName(it) }
            .mapNotNull { element.project.protocolsFile?.virtualFile?.parent?.findChild(it)?.findChild("${this.registry}.csv") }
            .mapNotNull { PsiManager.getInstance(element.project).findFile(it) as? CsvFile }
            .flatMap { it.getRowsByKey("name", namespace, path) }
            .mapNotNull { it.getEntry("name") }
            .map(::PsiElementResolveResult)
            .toList().toTypedArray()
    }

    override fun getVariants(): Array<Any> {
        return element.project.protocolVersions.asSequence()
            .filter { it in versionRange }
            .mapNotNull { element.project.getProtocolName(it) }
            .mapNotNull { element.project.protocolsFile?.virtualFile?.parent?.findChild(it)?.findChild("${this.registry}.csv") }
            .mapNotNull { PsiManager.getInstance(element.project).findFile(it) as? CsvFile }
            .flatMap { csvFile -> csvFile.rows.asSequence().mapNotNull { it.getEntry("name") } }
            .map { it.text }
            .toSortedSet().toTypedArray()
    }
}
