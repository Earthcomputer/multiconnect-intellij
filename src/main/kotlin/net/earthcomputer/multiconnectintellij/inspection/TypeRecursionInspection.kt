package net.earthcomputer.multiconnectintellij.inspection

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.util.RecursionManager
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import net.earthcomputer.multiconnectintellij.Constants
import net.earthcomputer.multiconnectintellij.getBoolean
import net.earthcomputer.multiconnectintellij.getVariantProvider
import net.earthcomputer.multiconnectintellij.getVersionRange
import net.earthcomputer.multiconnectintellij.intersectRange

class TypeRecursionInspection : MessageVariantInspectionBase() {
    override fun getStaticDescription() = "Type recursion in message variant without tailrec"

    override fun doCheckClass(
        clazz: PsiClass,
        manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor>? {
        val parentClass = clazz.superClass?.takeIf { it.hasAnnotation(Constants.MESSAGE_VARIANT) }

        val isTailrec = clazz.getAnnotation(Constants.MESSAGE_VARIANT)?.getBoolean("tailrec") ?: false
        val isParentTailrec = parentClass?.getAnnotation(Constants.MESSAGE_VARIANT)?.getBoolean("tailrec") ?: false

        if (referencesClass(clazz, parentClass, isTailrec, isParentTailrec, clazz, getVersionRange(clazz))) {
            return manager.createProblem(
                clazz.nameIdentifier ?: return null,
                isOnTheFly,
                "Message variant type is recursive outside tailrec constraints"
            )
        }

        return null
    }

    private fun referencesClass(
        originalClass: PsiClass,
        originalParent: PsiClass?,
        originalTailrec: Boolean,
        originalParentTailrec: Boolean,
        clazz: PsiClass,
        versionRange: IntRange,
    ): Boolean {
        return RecursionManager.doPreventingRecursion(clazz to versionRange, true) {
            val fields = McTypes.getFieldsOrdered(clazz)
            for ((index, field) in fields.withIndex()) {
                val fieldType = (McTypes.getDeepComponentType(field.type) as? PsiClassType)?.resolve() ?: continue
                val fieldVariants = when {
                    fieldType.hasAnnotation(Constants.MESSAGE_VARIANT) -> sequenceOf(fieldType)
                    fieldType.hasAnnotation(Constants.MESSAGE) -> getVariantProvider(fieldType)?.asSequence()?.mapNotNull { variantInfo ->
                        variantInfo.clazz.takeUnless { variantInfo.versions.intersectRange(versionRange).isEmpty() }
                    } ?: continue
                    else -> continue
                }
                for (fieldVariant in fieldVariants) {
                    if (fieldType.isEquivalentTo(originalClass)
                        && (!originalTailrec || index != fields.lastIndex || !clazz.isEquivalentTo(originalClass))
                    ) {
                        return@doPreventingRecursion true
                    }
                    if (fieldType.isEquivalentTo(originalParent)
                        && (!originalParentTailrec || index != fields.lastIndex || !clazz.isEquivalentTo(originalClass))
                    ) {
                        return@doPreventingRecursion true
                    }
                    val intersectedRange = getVersionRange(fieldType).intersectRange(versionRange)
                    if (referencesClass(originalClass, originalParent, originalTailrec, originalParentTailrec, fieldType, intersectedRange)) {
                        return@doPreventingRecursion true
                    }
                }
            }
            false
        } ?: false
    }
}
