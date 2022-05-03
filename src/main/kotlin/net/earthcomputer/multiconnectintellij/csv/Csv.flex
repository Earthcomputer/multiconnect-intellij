package net.earthcomputer.multiconnectintellij.csv;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import net.earthcomputer.multiconnectintellij.csv.psi.CsvTypes;
import com.intellij.psi.TokenType;

%%

%class CsvLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{  return;
%eof}

NEWLINE=\R
SPACE=[ ]
WHITESPACE=[\t\r\n]
COMMA=,
EQUALS=\=
COLON=:
OPEN_BRACKET=\[
CLOSE_BRACKET=]
STRING=[A-Za-z0-9\-_\./]+
COMMENT=#[^\r\n]*

%%

<YYINITIAL> {COMMENT} { yybegin(YYINITIAL); return CsvTypes.COMMENT; }

<YYINITIAL> {NEWLINE} { yybegin(YYINITIAL); return CsvTypes.NEWLINE; }

<YYINITIAL> {SPACE} { yybegin(YYINITIAL); return CsvTypes.SPACE; }

<YYINITIAL> {WHITESPACE} { yybegin(YYINITIAL); return TokenType.WHITE_SPACE; }

<YYINITIAL> {COMMA} { yybegin(YYINITIAL); return CsvTypes.COMMA; }

<YYINITIAL> {EQUALS} { yybegin(YYINITIAL); return CsvTypes.EQUALS; }

<YYINITIAL> {COLON} { yybegin(YYINITIAL); return CsvTypes.COLON; }

<YYINITIAL> {OPEN_BRACKET} { yybegin(YYINITIAL); return CsvTypes.OPEN_BRACKET; }

<YYINITIAL> {CLOSE_BRACKET} { yybegin(YYINITIAL); return CsvTypes.CLOSE_BRACKET; }

<YYINITIAL> {STRING} { yybegin(YYINITIAL); return CsvTypes.STRING; }

[^] { return TokenType.BAD_CHARACTER; }
