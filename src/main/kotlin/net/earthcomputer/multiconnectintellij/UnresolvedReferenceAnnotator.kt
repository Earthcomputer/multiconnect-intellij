package net.earthcomputer.multiconnectintellij

import com.intellij.analysis.AnalysisBundle
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiLiteral
import com.intellij.psi.PsiPolyVariantReference

class UnresolvedReferenceAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element is PsiLiteral) {
            val references = element.references.filter { it is ErrorOnUnresolved }
            for (reference in references) {
                val unresolved = if (reference is PsiPolyVariantReference) {
                    reference.multiResolve(false).isEmpty()
                } else {
                    reference.resolve() == null
                }
                if (unresolved) {
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
