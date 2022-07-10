package net.earthcomputer.multiconnectintellij

import com.intellij.codeInsight.daemon.ImplicitUsageProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiEnumConstant
import com.intellij.psi.PsiField
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier
import net.earthcomputer.multiconnectintellij.Constants.HANDLER
import net.earthcomputer.multiconnectintellij.Constants.MESSAGE_VARIANT
import net.earthcomputer.multiconnectintellij.Constants.NETWORK_ENUM
import net.earthcomputer.multiconnectintellij.Constants.PARTIAL_HANDLER

class MulticonnectImplicitUsageProvider : ImplicitUsageProvider {
    override fun isImplicitUsage(element: PsiElement): Boolean {
        if (isImplicitReadOrWrite(element, null)) {
            return true
        }

        if (element is PsiMethod) {
            if (element.hasAnnotation(HANDLER) || element.hasAnnotation(PARTIAL_HANDLER)) {
                return true
            }
            if (element.isConstructor && !element.hasParameters() && element.containingClass?.hasAnnotation(MESSAGE_VARIANT) == true) {
                return true
            }
        }

        return false
    }

    private fun isImplicitReadOrWrite(element: PsiElement, isWrite: Boolean?): Boolean {
        if (element is PsiField) {
            val clazz = element.containingClass ?: return false
            if (clazz.hasAnnotation(MESSAGE_VARIANT) && !element.hasModifierProperty(PsiModifier.STATIC)) {
                return true
            }
            if (isWrite != true && element is PsiEnumConstant && clazz.hasAnnotation(NETWORK_ENUM)) {
                return true
            }
        }

        return false
    }

    override fun isImplicitRead(element: PsiElement) = isImplicitReadOrWrite(element, false)

    override fun isImplicitWrite(element: PsiElement) = isImplicitReadOrWrite(element, true)
}
