package net.earthcomputer.multiconnectintellij.csv

import com.intellij.lexer.FlexAdapter

class CsvLexerAdapter : FlexAdapter(CsvLexer(null))
