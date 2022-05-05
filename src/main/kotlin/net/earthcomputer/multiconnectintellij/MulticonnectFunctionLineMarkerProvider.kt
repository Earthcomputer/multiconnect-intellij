package net.earthcomputer.multiconnectintellij

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.PsiMethod
import com.intellij.psi.search.searches.ReferencesSearch

class MulticonnectFunctionLineMarkerProvider : LineMarkerProviderDescriptor() {
    override fun getName() = "Multiconnect function"

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (element !is PsiIdentifier) {
            return null
        }
        if (!element.isValid) {
            return null
        }
        val method = element.parent as? PsiMethod ?: return null
        if (!element.isEquivalentTo(method.nameIdentifier)) {
            return null
        }

        val clazz = method.containingClass ?: return null
        if (!clazz.hasAnnotation(Constants.MESSAGE_VARIANT)) {
            return null
        }

        val isMulticonnectFunction = method.hasAnnotation(Constants.HANDLER)
                || method.hasAnnotation(Constants.PARTIAL_HANDLER)
                || ReferencesSearch.search(method).anyMatch { it is StringReference }
        if (!isMulticonnectFunction) {
            return null
        }

        return LineMarkerInfo(
            element,
            element.textRange,
            Icons.multiconnectFunction,
            { "Multiconnect function" },
            null,
            GutterIconRenderer.Alignment.RIGHT,
            { "multiconnect function indicator" }
        )
    }
}
