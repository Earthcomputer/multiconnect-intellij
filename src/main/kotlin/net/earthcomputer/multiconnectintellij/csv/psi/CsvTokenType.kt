package net.earthcomputer.multiconnectintellij.csv.psi

import com.intellij.psi.tree.IElementType
import net.earthcomputer.multiconnectintellij.csv.CsvLanguage

class CsvTokenType(debugName: String) : IElementType(debugName, CsvLanguage) {
    override fun toString() = "CsvTokenType.${super.toString()}"
}
