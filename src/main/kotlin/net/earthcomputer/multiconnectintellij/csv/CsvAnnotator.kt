package net.earthcomputer.multiconnectintellij.csv

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.psi.PsiElement
import net.earthcomputer.multiconnectintellij.csv.psi.CsvEntry
import net.earthcomputer.multiconnectintellij.csv.psi.CsvHeader
import net.earthcomputer.multiconnectintellij.csv.psi.CsvStringValue

class CsvAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val parent = element.parent ?: return
        if (element is CsvEntry) {
            val key = element.key
            if (key == "id" || key == "datafixVersion") {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(element).textAttributes(DefaultLanguageHighlighterColors.NUMBER).create()
            }
        }
        else if (element is CsvStringValue) {
            if (parent is CsvHeader) {
                holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                    .range(element).textAttributes(DefaultLanguageHighlighterColors.KEYWORD).create()
            }
        }
    }
}
