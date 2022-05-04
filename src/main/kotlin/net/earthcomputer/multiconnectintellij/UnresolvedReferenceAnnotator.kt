package net.earthcomputer.multiconnectintellij

import com.intellij.analysis.AnalysisBundle
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLiteral

class UnresolvedReferenceAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element is PsiLiteral) {
            val references = element.references.filterIsInstance<StringReference>()
            for (reference in references) {
                if (reference.multiResolve(false).isEmpty()) {
                    val rangeInElement = reference.rangeInElement
                    val range = rangeInElement.shiftRight(element.textRange.startOffset)
                    val text = rangeInElement.substring(element.text)
                    holder.newAnnotation(HighlightSeverity.ERROR, AnalysisBundle.message("cannot.resolve.symbol", text))
                        .range(range)
                        .highlightType(ProblemHighlightType.LIKE_UNKNOWN_SYMBOL)
                        .create()
                }
            }
        }
    }
}
