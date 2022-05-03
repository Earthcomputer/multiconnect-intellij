package net.earthcomputer.multiconnectintellij

import com.intellij.codeInsight.completion.CompletionConfidence
import com.intellij.codeInsight.completion.SkipAutopopupInStrings
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiLiteral
import com.intellij.psi.util.parentOfType
import com.intellij.util.ThreeState

class MulticonnectCompletionConfidence : CompletionConfidence() {
    override fun shouldSkipAutopopup(contextElement: PsiElement, psiFile: PsiFile, offset: Int): ThreeState {
        // Enable auto complete for StringReferenceProvider literals

        // This check is more performant than the parentOfType call below, so do this first for an early out
        if (!SkipAutopopupInStrings.isInStringLiteral(contextElement)) {
            return ThreeState.UNSURE
        }

        val literal = contextElement.parentOfType<PsiLiteral>() ?: return ThreeState.UNSURE
        return if (StringReferenceProvider.ELEMENT_PATTERN.accepts(literal)) {
            ThreeState.NO
        } else {
            ThreeState.UNSURE
        }
    }
}
