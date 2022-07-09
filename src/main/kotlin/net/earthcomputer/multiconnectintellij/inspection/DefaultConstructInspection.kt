package net.earthcomputer.multiconnectintellij.inspection

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.psi.CommonClassNames.JAVA_LANG_OBJECT
import com.intellij.psi.CommonClassNames.JAVA_LANG_STRING
import com.intellij.psi.CommonClassNames.JAVA_UTIL_FUNCTION_SUPPLIER
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiField
import com.intellij.psi.PsiModifier
import com.intellij.psi.PsiParameter
import com.intellij.psi.PsiType
import com.intellij.psi.util.PsiTreeUtil
import net.earthcomputer.multiconnectintellij.Constants
import net.earthcomputer.multiconnectintellij.Constants.MINECRAFT_RESOURCE_LOCATION
import net.earthcomputer.multiconnectintellij.getBoolean
import net.earthcomputer.multiconnectintellij.getDoubleArray
import net.earthcomputer.multiconnectintellij.getLongArray
import net.earthcomputer.multiconnectintellij.getPsiType
import net.earthcomputer.multiconnectintellij.getString
import net.earthcomputer.multiconnectintellij.getStringArray

class DefaultConstructInspection : MessageVariantAnnotationInspection(Constants.DEFAULT_CONSTRUCT) {
    override fun getStaticDescription() = "Reports @DefaultConstruct problems"

    override fun checkAnnotation(
        clazz: PsiClass,
        annotation: PsiAnnotation,
        manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor>? {
        return when (val parent = PsiTreeUtil.getParentOfType(annotation, PsiParameter::class.java, PsiField::class.java, PsiClass::class.java)) {
            is PsiParameter -> checkParameterAnnotation(parent, annotation, manager, isOnTheFly)
            is PsiField -> checkFieldAnnotation(clazz, parent, annotation, manager, isOnTheFly)
            is PsiClass -> checkClassAnnotation(clazz, annotation, manager, isOnTheFly)
            else -> null
        }
    }

    private fun checkParameterAnnotation(
        parameter: PsiParameter,
        annotation: PsiAnnotation,
        manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor>? {
        val paramType = parameter.type
        if (!InvalidMessageFieldTypeInspection.isValidType(paramType) && (
                    paramType !is PsiClassType
                            || paramType.resolve()?.qualifiedName != JAVA_UTIL_FUNCTION_SUPPLIER
                            || paramType.parameters.singleOrNull()?.let(InvalidMessageFieldTypeInspection::isValidType) != true
                    )
        ) {
            return manager.createProblem(
                parameter.typeElement ?: annotation.nameReferenceElement ?: return null,
                isOnTheFly,
                "Type cannot be default constructed"
            )
        }

        if (annotation.parameterList.attributes.isNotEmpty()) {
            return manager.createProblem(
                annotation.parameterList,
                isOnTheFly,
                "Cannot use @DefaultConstruct attributes on a parameter"
            )
        }

        return null
    }

    private fun checkFieldAnnotation(
        clazz: PsiClass,
        field: PsiField,
        annotation: PsiAnnotation,
        manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor>? {
        val subType = annotation.getPsiType("subType").takeIf {
            it != PsiType.VOID && (it as? PsiClassType)?.resolve()?.qualifiedName != JAVA_LANG_OBJECT
        }
        val booleanValue = annotation.getBoolean("booleanValue")
        val intValue = annotation.getLongArray("intValue")
        val doubleValue = annotation.getDoubleArray("doubleValue")
        val stringValue = annotation.getStringArray("stringValue")
        val compute = annotation.getString("compute").takeIf { it.isNotEmpty() }
        if (arrayOf(
                subType != null,
                booleanValue,
                intValue.isNotEmpty(),
                doubleValue.isNotEmpty(),
                stringValue.isNotEmpty(),
                compute != null
            ).count { it } != 1
        ) {
            return manager.createProblem(
                annotation.parameterList.takeIf { it.attributes.isNotEmpty() } ?: annotation.nameReferenceElement ?: return null,
                isOnTheFly,
                "Must specify exactly one default construction method"
            )
        }

        val fieldType = field.type

        if (subType != null && !isSubTypeValid(fieldType, subType)) {
            return manager.createProblem(
                annotation.findDeclaredAttributeValue("subType") ?: annotation.nameReferenceElement ?: return null,
                isOnTheFly,
                "subType is not a polymorphic subclass of the field type"
            )
        }

        if (booleanValue && fieldType != PsiType.BOOLEAN) {
            return manager.createProblem(
                annotation.findDeclaredAttributeValue("booleanValue") ?: annotation.nameReferenceElement ?: return null,
                isOnTheFly,
                "booleanValue used on non-boolean field"
            )
        }

        if (intValue.isNotEmpty()) {
            if (fieldType != PsiType.BYTE && fieldType != PsiType.SHORT && fieldType != PsiType.INT && fieldType != PsiType.LONG) {
                return manager.createProblem(
                    annotation.findDeclaredAttributeValue("intValue") ?: annotation.nameReferenceElement ?: return null,
                    isOnTheFly,
                    "intValue used on non-integral field"
                )
            }
            if (intValue.size >= 2) {
                return manager.createProblem(
                    annotation.findDeclaredAttributeValue("intValue") ?: annotation.nameReferenceElement ?: return null,
                    isOnTheFly,
                    "intValue cannot contain multiple values"
                )
            }
            val value = intValue[0]
            val expectedRange = when (fieldType) {
                PsiType.BYTE -> Byte.MIN_VALUE.toLong()..Byte.MAX_VALUE
                PsiType.SHORT -> Short.MIN_VALUE.toLong()..Short.MAX_VALUE
                PsiType.INT -> Int.MIN_VALUE.toLong()..Int.MAX_VALUE
                else -> Long.MIN_VALUE..Long.MAX_VALUE
            }
            if (value !in expectedRange) {
                return manager.createProblem(
                    annotation.findDeclaredAttributeValue("intValue") ?: annotation.nameReferenceElement ?: return null,
                    isOnTheFly,
                    "Value is outside the valid range for ${fieldType.presentableText}"
                )
            }
        }

        if (doubleValue.isNotEmpty()) {
            if (fieldType != PsiType.FLOAT && fieldType != PsiType.DOUBLE) {
                return manager.createProblem(
                    annotation.findDeclaredAttributeValue("doubleValue") ?: annotation.nameReferenceElement ?: return null,
                    isOnTheFly,
                    "doubleValue used on non-floating-point field"
                )
            }
            if (doubleValue.size >= 2) {
                return manager.createProblem(
                    annotation.findDeclaredAttributeValue("doubleValue") ?: annotation.nameReferenceElement ?: return null,
                    isOnTheFly,
                    "doubleValue cannot contain multiple values"
                )
            }
        }

        if (stringValue.isNotEmpty()) {
            if (!isTypeValidForString(field, fieldType)) {
                return manager.createProblem(
                    annotation.findDeclaredAttributeValue("stringValue") ?: annotation.nameReferenceElement ?: return null,
                    isOnTheFly,
                    "stringValue used on invalid field type"
                )
            }
            if (stringValue.size >= 2) {
                return manager.createProblem(
                    annotation.findDeclaredAttributeValue("stringValue") ?: annotation.nameReferenceElement ?: return null,
                    isOnTheFly,
                    "stringValue cannot contain multiple values"
                )
            }
        }

        if (compute != null) {
            val func = clazz.findMethodsByName(compute, true).singleOrNull { it.hasModifierProperty(PsiModifier.STATIC) } ?: return null
            if (func.returnType != fieldType) {
                return manager.createProblem(
                    annotation.findDeclaredAttributeValue("compute") ?: annotation.nameReferenceElement ?: return null,
                    isOnTheFly,
                    "compute function does not return the same type as the field"
                )
            }
        }

        return null
    }

    private fun isSubTypeValid(parentType: PsiType, subType: PsiType): Boolean {
        if (parentType !is PsiClassType || subType !is PsiClassType) {
            return false
        }
        val parentClass = parentType.resolve() ?: return false
        val childClass = subType.resolve() ?: return false
        if (childClass.superClass?.isEquivalentTo(parentClass) != true) {
            return false
        }
        if (!parentClass.hasAnnotation(Constants.MESSAGE_VARIANT)) {
            return false
        }
        if (!childClass.hasAnnotation(Constants.MESSAGE_VARIANT)) {
            return false
        }
        return true
    }

    private fun isTypeValidForString(field: PsiField, fieldType: PsiType): Boolean {
        if (field.hasAnnotation(Constants.REGISTRY)) {
            if (fieldType == PsiType.BYTE || fieldType == PsiType.SHORT || fieldType == PsiType.INT || fieldType == PsiType.LONG) {
                return true
            }
        }
        if (fieldType !is PsiClassType) {
            return false
        }
        val fieldClass = fieldType.resolve() ?: return false
        val qName = fieldClass.qualifiedName ?: return false
        if (qName == JAVA_LANG_STRING || qName == MINECRAFT_RESOURCE_LOCATION) {
            return true
        }
        if (fieldClass.hasAnnotation(Constants.NETWORK_ENUM)) {
            return true
        }
        return false
    }

    private fun checkClassAnnotation(
        clazz: PsiClass,
        annotation: PsiAnnotation,
        manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor>? {
        if (!clazz.hasAnnotation(Constants.POLYMORPHIC) || !clazz.hasModifierProperty(PsiModifier.ABSTRACT)) {
            return manager.createProblem(
                annotation.nameReferenceElement ?: return null,
                isOnTheFly,
                "@DefaultConstruct can only be used on polymorphic parents"
            )
        }

        val subType = annotation.getPsiType("subType")
        if (!isSubTypeValid(JavaPsiFacade.getElementFactory(clazz.project).createType(clazz), subType)) {
            return manager.createProblem(
                annotation.findDeclaredAttributeValue("subType") ?: annotation.nameReferenceElement ?: return null,
                isOnTheFly,
                "Invalid polymorphic subtype"
            )
        }

        if (annotation.parameterList.attributes.any { it.name != "subType" }) {
            return manager.createProblem(
                annotation.parameterList,
                isOnTheFly,
                "@DefaultConstruct on types must only have the subType attribute"
            )
        }

        return null
    }
}
