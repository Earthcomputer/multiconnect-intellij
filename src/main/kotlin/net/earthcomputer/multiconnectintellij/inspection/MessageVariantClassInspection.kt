package net.earthcomputer.multiconnectintellij.inspection

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.psi.CommonClassNames.JAVA_LANG_OBJECT
import com.intellij.psi.PsiAnonymousClass
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiModifier
import com.intellij.psi.search.searches.ClassInheritorsSearch
import net.earthcomputer.multiconnectintellij.Constants
import net.earthcomputer.multiconnectintellij.getBoolean
import net.earthcomputer.multiconnectintellij.getDoubleArray
import net.earthcomputer.multiconnectintellij.getLongArray
import net.earthcomputer.multiconnectintellij.getStringArray

class MessageVariantClassInspection : MessageVariantInspectionBase() {
    override fun getStaticDescription() = "Reports issues with message variant declarations"

    override fun doCheckClass(
        clazz: PsiClass,
        manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor>? {
        if (clazz is PsiAnonymousClass) {
            return manager.createProblem(
                clazz.baseClassReference,
                isOnTheFly,
                "Message variant class cannot be anonymous"
            )
        }
        if (clazz.isInterface || clazz.isEnum || clazz.isAnnotationType || clazz.isRecord) {
            return manager.createProblem(
                clazz.nameIdentifier ?: return null,
                isOnTheFly,
                "Message variant class must be a normal class"
            )
        }
        if (!clazz.hasModifierProperty(PsiModifier.PUBLIC)) {
            return manager.createProblem(
                clazz.nameIdentifier ?: return null,
                isOnTheFly,
                "Message variant class must be public"
            )
        }
        if (clazz.parent !is PsiFile && !clazz.hasModifierProperty(PsiModifier.STATIC)) {
            return manager.createProblem(
                clazz.nameIdentifier ?: return null,
                isOnTheFly,
                "Message variant inner class must be static"
            )
        }

        val interfaces = clazz.interfaces
        if (interfaces.size >= 2) {
            return manager.createProblem(
                clazz.nameIdentifier ?: return null,
                isOnTheFly,
                "Message variant cannot implement more than one interface"
            )
        }
        if (interfaces.firstOrNull()?.hasAnnotation(Constants.MESSAGE) == false) {
            return manager.createProblem(
                clazz.implementsList?.referenceElements?.firstOrNull() ?: clazz.nameIdentifier ?: return null,
                isOnTheFly,
                "Message variant can only implement messages"
            )
        }

        val constructors = clazz.constructors
        if (constructors.isNotEmpty()) {
            if (constructors.none { !it.hasParameters() && it.hasModifierProperty(PsiModifier.PUBLIC) && !McTypes.throwsCheckedExceptions(it) }) {
                return manager.createProblem(
                    clazz.nameIdentifier ?: return null,
                    isOnTheFly,
                    "Message variant must have a public no-arg constructor that doesn't throw checked exceptions"
                )
            }
        }

        val isTailrec = clazz.getAnnotation(Constants.MESSAGE_VARIANT)?.getBoolean("tailrec") == true

        val polymorphic = clazz.getAnnotation(Constants.POLYMORPHIC)
        val isPolymorphicParent = polymorphic != null && clazz.superClass?.qualifiedName == JAVA_LANG_OBJECT
        val isPolymorphicChild = polymorphic != null && !isPolymorphicParent

        if (isPolymorphicParent) {
            if (!clazz.hasModifierProperty(PsiModifier.ABSTRACT)) {
                return manager.createProblem(
                    clazz.nameIdentifier ?: return null,
                    isOnTheFly,
                    "Polymorphic parent must be abstract"
                )
            }
        } else {
            if (clazz.hasModifierProperty(PsiModifier.ABSTRACT)) {
                return manager.createProblem(
                    clazz.nameIdentifier ?: return null,
                    isOnTheFly,
                    "Message variant cannot be abstract unless it is a polymorphic parent"
                )
            }
            if (isTailrec) {
                val lastField = clazz.fields.lastOrNull { !it.hasModifierProperty(PsiModifier.STATIC) }
                val isActuallyTailrec = (lastField?.type as? PsiClassType)?.resolve() == clazz
                if (!isActuallyTailrec) {
                    return manager.createProblem(
                        clazz.nameIdentifier ?: return null,
                        isOnTheFly,
                        "Message variant declared as tailrec is not actually tail recursive"
                    )
                }
            }
        }

        if (polymorphic == null) {
            if (clazz.superClass?.qualifiedName != JAVA_LANG_OBJECT) {
                return manager.createProblem(
                    clazz.extendsList?.referenceElements?.firstOrNull() ?: clazz.nameIdentifier ?: return null,
                    isOnTheFly,
                    "Message variant cannot extend a non-Object class unless it is @Polymorphic"
                )
            }
        }

        if (isPolymorphicChild) {
            val polymorphicParent = clazz.superClass ?: return null
            if (!polymorphicParent.hasAnnotation(Constants.MESSAGE_VARIANT)
                || !polymorphicParent.hasAnnotation(Constants.POLYMORPHIC)
                || !polymorphicParent.hasModifierProperty(PsiModifier.ABSTRACT)
            ) {
                return manager.createProblem(
                    clazz.nameIdentifier ?: return null,
                    isOnTheFly,
                    "Polymorphic child must inherit an abstract polymorphic message variant"
                )
            }

            polymorphic!!
            val isOtherwise = polymorphic.getBoolean("otherwise")
            val isTrue = polymorphic.getBoolean("booleanValue")
            val intValue = polymorphic.getLongArray("intValue")
            val doubleValue = polymorphic.getDoubleArray("doubleValue")
            val stringValue = polymorphic.getStringArray("stringValue")

            val polymorphicChildren = ClassInheritorsSearch.search(polymorphicParent, false)
                .filter { !it.isEquivalentTo(clazz) && it.hasAnnotation(Constants.MESSAGE_VARIANT) }
                .mapNotNull { it.getAnnotation(Constants.POLYMORPHIC) }

            for (child in polymorphicChildren) {
                if (isOtherwise && child.getBoolean("otherwise")) {
                    return manager.createProblem(
                        polymorphic.findDeclaredAttributeValue("otherwise") ?: clazz.nameIdentifier ?: return null,
                        isOnTheFly,
                        "Multiple polymorphic children cannot specify otherwise = true"
                    )
                }
                if (isTrue && child.getBoolean("booleanValue")) {
                    return manager.createProblem(
                        polymorphic.findDeclaredAttributeValue("booleanValue") ?: clazz.nameIdentifier ?: return null,
                        isOnTheFly,
                        "Multiple polymorphic children have the same booleanValue"
                    )
                }
                if (intValue.isNotEmpty() && child.getLongArray("intValue").any { it in intValue }) {
                    return manager.createProblem(
                        polymorphic.findDeclaredAttributeValue("intValue") ?: clazz.nameIdentifier ?: return null,
                        isOnTheFly,
                        "Multiple polymorphic children have the same intValue"
                    )
                }
                if (doubleValue.isNotEmpty() && child.getDoubleArray("doubleValue").any { n -> doubleValue.any { it == n } }) {
                    return manager.createProblem(
                        polymorphic.findDeclaredAttributeValue("doubleValue") ?: clazz.nameIdentifier ?: return null,
                        isOnTheFly,
                        "Multiple polymorphic children have the same doubleValue"
                    )
                }
                if (stringValue.isNotEmpty() && child.getStringArray("stringValue").any { it in stringValue }) {
                    return manager.createProblem(
                        polymorphic.findDeclaredAttributeValue("stringValue") ?: clazz.nameIdentifier ?: return null,
                        isOnTheFly,
                        "Multiple polymorphic children have the same stringValue"
                    )
                }
            }
        }

        if (isPolymorphicParent) {
            val polymorphicChildren = ClassInheritorsSearch.search(clazz, false).filter { it.hasAnnotation(Constants.MESSAGE_VARIANT) }
            if (polymorphicChildren.isEmpty()) {
                return manager.createProblem(
                    clazz.nameIdentifier ?: return null,
                    isOnTheFly,
                    "Polymorphic parent does not have any children"
                )
            }

            val hasTypeField = clazz.fields.any { !it.hasModifierProperty(PsiModifier.STATIC) }
            if (!hasTypeField) {
                return manager.createProblem(
                    clazz.nameIdentifier ?: return null,
                    isOnTheFly,
                    "Polymorphic parent must have at least one field"
                )
            }

            if (isTailrec) {
                val isActuallyTailrec = polymorphicChildren.any { child ->
                    val lastField = child.fields.lastOrNull { !it.hasModifierProperty(PsiModifier.STATIC) }
                    (lastField?.type as? PsiClassType)?.resolve() != clazz
                }
                if (!isActuallyTailrec) {
                    return manager.createProblem(
                        clazz.nameIdentifier ?: return null,
                        isOnTheFly,
                        "Message variant declared as tailrec is not actually tail recursive"
                    )
                }
            }
        }

        return null
    }
}
