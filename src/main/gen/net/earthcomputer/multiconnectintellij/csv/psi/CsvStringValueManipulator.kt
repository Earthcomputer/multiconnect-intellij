package net.earthcomputer.multiconnectintellij.csv.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.AbstractElementManipulator

class CsvStringValueManipulator : AbstractElementManipulator<CsvStringValue>() {
    override fun handleContentChange(element: CsvStringValue, range: TextRange, newContent: String): CsvStringValue {
        val newString = range.replace(element.text, newContent)
        val newElement = element.manager.csvElementFactory.createStringValueFromText(newString)
        return element.replace(newElement) as CsvStringValue
    }
}
