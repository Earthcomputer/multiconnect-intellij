package net.earthcomputer.multiconnectintellij.inspection

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiParameter
import com.intellij.psi.PsiType
import com.intellij.psi.util.parentOfType
import net.earthcomputer.multiconnectintellij.Constants
import net.earthcomputer.multiconnectintellij.Constants.JAVA_UTIL_FUNCTION_INT_FUNCTION
import net.earthcomputer.multiconnectintellij.Constants.JAVA_UTIL_FUNCTION_TO_INT_FUNCTION
import net.earthcomputer.multiconnectintellij.Constants.MINECRAFT_IDENTIFIER
import net.earthcomputer.multiconnectintellij.Constants.MINECRAFT_NETWORK_HANDLER
import net.earthcomputer.multiconnectintellij.Constants.MULTICONNECT_DELAYED_PACKET_SENDER
import net.earthcomputer.multiconnectintellij.Constants.MULTICONNECT_TYPED_MAP

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
                && (paramType !is PsiClassType || paramType.resolve()?.qualifiedName != MINECRAFT_IDENTIFIER)
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
        if ((type.parameters.singleOrNull() as? PsiClassType)?.resolve()?.qualifiedName != MINECRAFT_IDENTIFIER) {
            return false
        }
        return true
    }

    private fun isValidFilledType(parameter: PsiParameter, type: PsiType): Boolean {
        val qName = (type as? PsiClassType)?.resolve()?.qualifiedName ?: return false
        return when (qName) {
            MINECRAFT_NETWORK_HANDLER,
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
