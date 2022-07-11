package net.earthcomputer.multiconnectintellij.inspection

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFixOnPsiElement
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.CommonClassNames.JAVA_UTIL_FUNCTION_FUNCTION
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiParameter
import com.intellij.psi.PsiType
import com.intellij.psi.PsiTypeElement
import com.intellij.psi.codeStyle.JavaCodeStyleManager
import com.intellij.psi.util.parentOfType
import net.earthcomputer.multiconnectintellij.Constants
import net.earthcomputer.multiconnectintellij.Constants.JAVA_UTIL_FUNCTION_INT_FUNCTION
import net.earthcomputer.multiconnectintellij.Constants.JAVA_UTIL_FUNCTION_TO_INT_FUNCTION
import net.earthcomputer.multiconnectintellij.Constants.MESSAGE_VARIANT
import net.earthcomputer.multiconnectintellij.Constants.MINECRAFT_RESOURCE_LOCATION
import net.earthcomputer.multiconnectintellij.Constants.MINECRAFT_CLIENT_PACKET_LISTENER
import net.earthcomputer.multiconnectintellij.Constants.MULTICONNECT_DELAYED_PACKET_SENDER
import net.earthcomputer.multiconnectintellij.Constants.MULTICONNECT_TYPED_MAP
import net.earthcomputer.multiconnectintellij.getInt
import net.earthcomputer.multiconnectintellij.getVariantProvider
import net.earthcomputer.multiconnectintellij.protocolVersions
import kotlin.math.abs

class FilledArgumentInspection : MessageVariantAnnotationInspection(Constants.FILLED_ARGUMENT) {
    override fun getStaticDescription() = "Reports @FilledArgument problems"

    override fun checkAnnotation(
        clazz: PsiClass,
        annotation: PsiAnnotation,
        manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor>? {
        val parameter = annotation.parentOfType<PsiParameter>() ?: return null
        val paramType = parameter.type
        if (annotation.findDeclaredAttributeValue("fromRegistry") != null) {
            if (paramType != PsiType.BYTE
                && paramType != PsiType.SHORT
                && paramType != PsiType.INT
                && paramType != PsiType.LONG
                && (paramType !is PsiClassType || paramType.resolve()?.qualifiedName != MINECRAFT_RESOURCE_LOCATION)
            ) {
                return manager.createProblem(
                    parameter.typeElement ?: annotation.nameReferenceElement ?: return null,
                    isOnTheFly,
                    "@FilledArgument from registry must be of integral or identifier type"
                )
            }
        } else if (annotation.findDeclaredAttributeValue("registry") != null) {
            if (!isValidFilledRegistryType(paramType)) {
                return manager.createProblem(
                    parameter.typeElement ?: annotation.nameReferenceElement ?: return null,
                    isOnTheFly,
                    "@FilledArgument with registry must be of type IntFunction<Identifier> or ToIntFunction<Identifier>"
                )
            }
        } else if (annotation.findDeclaredAttributeValue("fromVersion") != null && annotation.findDeclaredAttributeValue("toVersion") != null) {
            return handleExplicitTranslator(
                paramType,
                parameter.typeElement ?: return null,
                annotation,
                manager,
                isOnTheFly,
            )
        } else {
            if (!isValidFilledType(parameter, paramType)) {
                return manager.createProblem(
                    parameter.typeElement ?: annotation.nameReferenceElement ?: return null,
                    isOnTheFly,
                    "Invalid @FilledArgument type"
                )
            }
        }

        return null
    }

    private fun isValidFilledRegistryType(type: PsiType): Boolean {
        val qName = (type as? PsiClassType)?.resolve()?.qualifiedName ?: return false
        if (qName != JAVA_UTIL_FUNCTION_INT_FUNCTION && qName != JAVA_UTIL_FUNCTION_TO_INT_FUNCTION) {
            return false
        }
        if ((type.parameters.singleOrNull() as? PsiClassType)?.resolve()?.qualifiedName != MINECRAFT_RESOURCE_LOCATION) {
            return false
        }
        return true
    }

    private fun handleExplicitTranslator(
        paramType: PsiType,
        paramTypeElement: PsiTypeElement,
        annotation: PsiAnnotation,
        manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor>? {
        val fromVersion = annotation.getInt("fromVersion", -1)
        val toVersion = annotation.getInt("toVersion", -1)
        val protocolVersions = manager.project.protocolVersions
        val fromIndex = protocolVersions.binarySearch(fromVersion)
        if (fromIndex == -1) {
            return manager.createProblem(
                annotation.findDeclaredAttributeValue("fromVersion") ?: annotation.nameReferenceElement ?: return null,
                isOnTheFly,
                "Invalid @FilledArgument fromVersion"
            )
        }
        val toIndex = protocolVersions.binarySearch(toVersion)
        if (toIndex == -1) {
            return manager.createProblem(
                annotation.findDeclaredAttributeValue("toVersion") ?: annotation.nameReferenceElement ?: return null,
                isOnTheFly,
                "Invalid @FilledArgument toVersion"
            )
        }

        // check that fromVersion and toVersion are adjacent versions
        if (abs(toIndex - fromIndex) != 1) {
            return manager.createProblem(
                annotation.findDeclaredAttributeValue("toVersion") ?: annotation.nameReferenceElement ?: return null,
                isOnTheFly,
                "fromVersion and toVersion must be adjacent versions"
            )
        }

        val paramTypeRefElt = paramTypeElement.innermostComponentReferenceElement

        // check that paramType is a Function
        if (paramType !is PsiClassType || paramType.resolve()?.qualifiedName != JAVA_UTIL_FUNCTION_FUNCTION) {
            return manager.createProblem(
                paramTypeElement.innermostComponentReferenceElement ?: paramTypeElement,
                isOnTheFly,
                "@FilledArgument must be a Function"
            )
        }

        val typeParams = paramType.parameters
        if (typeParams.size != 2) {
            return manager.createProblem(
                paramTypeRefElt?.referenceNameElement ?: paramTypeElement,
                isOnTheFly,
                "@FilledArgument must be a Function"
            )
        }

        val fromType = (typeParams[0] as? PsiClassType)?.resolve()
        if (fromType == null || !fromType.hasAnnotation(MESSAGE_VARIANT)) {
            return manager.createProblem(
                paramTypeRefElt?.parameterList?.typeParameterElements?.firstOrNull() ?: paramTypeElement,
                isOnTheFly,
                "@FilledArgument must be a Function from a messsage variant"
            )
        }
        val toType = (typeParams[1] as? PsiClassType)?.resolve()
        if (toType == null || !toType.hasAnnotation(MESSAGE_VARIANT)) {
            return manager.createProblem(
                paramTypeRefElt?.parameterList?.typeParameterElements?.lastOrNull() ?: paramTypeElement,
                isOnTheFly,
                "@FilledArgument must be a Function to a message variant"
            )
        }

        // check that fromType and toType are variants of the same message
        val variantProvider = getVariantProvider(fromType) ?: return null

        val expectedFromType = variantProvider.getVariant(fromVersion)?.clazz
        val expectedToType = variantProvider.getVariant(toVersion)?.clazz
        val problems = mutableListOf<ProblemDescriptor>()

        if (expectedFromType == null || !expectedFromType.isEquivalentTo(fromType)) {
            val toReplace = paramTypeRefElt?.parameterList?.typeParameterElements?.firstOrNull()
            problems += manager.createProblem(
                toReplace ?: paramTypeElement,
                isOnTheFly,
                "Function does not convert from the correct variant of the message",
                quickFix = toReplace?.let {
                    expectedFromType?.qualifiedName?.let { qName ->
                        ReplaceTypeReferenceFix(toReplace, qName, expectedFromType.name ?: qName)
                    }
                }
            )
        }

        if (expectedToType == null || !expectedToType.isEquivalentTo(toType)) {
            val toReplace = paramTypeRefElt?.parameterList?.typeParameterElements?.lastOrNull()
            problems += manager.createProblem(
                toReplace ?: paramTypeElement,
                isOnTheFly,
                "Function does not convert to the correct variant of the message",
                quickFix = toReplace?.let {
                    expectedToType?.qualifiedName?.let { qName ->
                        ReplaceTypeReferenceFix(toReplace, qName, expectedToType.name ?: qName)
                    }
                }
            )
        }

        return problems.takeIf { it.isNotEmpty() }?.toTypedArray()
    }

    private fun isValidFilledType(parameter: PsiParameter, type: PsiType): Boolean {
        val qName = (type as? PsiClassType)?.resolve()?.qualifiedName ?: return false
        return when (qName) {
            MINECRAFT_CLIENT_PACKET_LISTENER,
            MULTICONNECT_TYPED_MAP -> true
            MULTICONNECT_DELAYED_PACKET_SENDER -> {
                val method = parameter.parentOfType<PsiMethod>() ?: return false
                if (!method.hasAnnotation(Constants.HANDLER) && !method.hasAnnotation(Constants.PARTIAL_HANDLER)) {
                    return false
                }
                // TODO: validate the packet class?
                true
            }
            else -> false
        }
    }
}

private class ReplaceTypeReferenceFix(
    replacedElement: PsiTypeElement,
    private val newQName: String,
    private val newSimpleName: String,
) : LocalQuickFixOnPsiElement(replacedElement) {
    override fun getFamilyName() = "Replace type element"

    override fun getText() = "Replace with $newSimpleName"

    override fun invoke(project: Project, file: PsiFile, startElement: PsiElement, endElement: PsiElement) {
        val elementFactory = JavaPsiFacade.getElementFactory(project)
        val newType = elementFactory.createTypeByFQClassName(newQName, startElement.resolveScope)
        var newTypeElt: PsiElement = elementFactory.createTypeElement(newType)
        newTypeElt = startElement.replace(newTypeElt)
        JavaCodeStyleManager.getInstance(project).shortenClassReferences(newTypeElt)
    }

}
