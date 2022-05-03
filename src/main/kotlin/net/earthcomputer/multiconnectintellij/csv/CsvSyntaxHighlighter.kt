package net.earthcomputer.multiconnectintellij.csv

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import net.earthcomputer.multiconnectintellij.csv.psi.CsvTypes

class CsvSyntaxHighlighter : SyntaxHighlighterBase() {
    companion object {
        val brackets = createTextAttributesKey("MULTICONNECT_CSV_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS)
        val equals = createTextAttributesKey("MULTICONNECT_CSV_EQUALS", DefaultLanguageHighlighterColors.OPERATION_SIGN)
        val comma = createTextAttributesKey("MULTICONNECT_CSV_COMMA", DefaultLanguageHighlighterColors.COMMA)
        val colon = createTextAttributesKey("MULTICONNECT_CSV_COLON", DefaultLanguageHighlighterColors.VALID_STRING_ESCAPE)
        val string = createTextAttributesKey("MULTICONNECT_CSV_STRING", DefaultLanguageHighlighterColors.STRING)
        val comment = createTextAttributesKey("MULTICONNECT_CSV_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
        val badCharacter = createTextAttributesKey("MULTICONNECT_CSV_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER)

        private val bracketsKeys = arrayOf(brackets)
        private val equalsKeys = arrayOf(equals)
        private val commaKeys = arrayOf(comma)
        private val colonKeys = arrayOf(colon)
        private val stringKeys = arrayOf(string)
        private val commentKeys = arrayOf(comment)
        private val badCharacterKeys = arrayOf(badCharacter)
    }

    override fun getHighlightingLexer(): CsvLexerAdapter {
        return CsvLexerAdapter()
    }

    override fun getTokenHighlights(tokenType: IElementType?): Array<TextAttributesKey> {
        return when (tokenType) {
            CsvTypes.OPEN_BRACKET, CsvTypes.CLOSE_BRACKET -> bracketsKeys
            CsvTypes.EQUALS -> equalsKeys
            CsvTypes.COMMA -> commaKeys
            CsvTypes.COLON -> colonKeys
            CsvTypes.STRING -> stringKeys
            CsvTypes.COMMENT -> commentKeys
            TokenType.BAD_CHARACTER -> badCharacterKeys
            else -> TextAttributesKey.EMPTY_ARRAY
        }
    }
}
