package net.earthcomputer.multiconnectintellij

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.completion.JavaCompletionContributor
import com.intellij.codeInsight.completion.JavaCompletionSorting
import com.intellij.codeInsight.completion.LegacyCompletionContributor

class MulticonnectCompletionContributor : CompletionContributor() {
    private val legacyContributor = LegacyCompletionContributor()

    override fun fillCompletionVariants(parameters: CompletionParameters, result: CompletionResultSet) {
        if (parameters.completionType != CompletionType.BASIC) {
            return
        }

        val position = parameters.position
        if (!JavaCompletionContributor.isInJavaContext(position)) {
            return
        }

        if (!StringReferenceProvider.ELEMENT_PATTERN.accepts(position)) {
            return
        }

        val javaResult = JavaCompletionSorting.addJavaSorting(parameters, result)
        legacyContributor.fillCompletionVariants(parameters, javaResult)
    }
}
