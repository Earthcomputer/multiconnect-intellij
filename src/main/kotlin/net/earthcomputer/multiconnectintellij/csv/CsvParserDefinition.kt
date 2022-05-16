package net.earthcomputer.multiconnectintellij.csv

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.stubs.PsiFileStub
import com.intellij.psi.tree.IStubFileElementType
import com.intellij.psi.tree.TokenSet
import net.earthcomputer.multiconnectintellij.csv.psi.CsvFile
import net.earthcomputer.multiconnectintellij.csv.psi.CsvTypes
import net.earthcomputer.multiconnectintellij.csv.psi.stubs.CsvStubBuilder

class CsvParserDefinition : ParserDefinition {
    private val whiteSpaces = TokenSet.create(TokenType.WHITE_SPACE)
    private val comments = TokenSet.create(CsvTypes.COMMENT)

    private val fileType = object : IStubFileElementType<PsiFileStub<CsvFile>>(CsvLanguage) {
        override fun getBuilder() = CsvStubBuilder()
    }

    override fun createLexer(project: Project?) : CsvLexerAdapter {
        return CsvLexerAdapter()
    }

    override fun getWhitespaceTokens() = whiteSpaces

    override fun getCommentTokens() = comments

    override fun getStringLiteralElements(): TokenSet = TokenSet.EMPTY

    override fun createParser(project: Project?): CsvParser {
        return CsvParser()
    }

    override fun getFileNodeType() = fileType

    override fun createFile(viewProvider: FileViewProvider): CsvFile {
        return CsvFile(viewProvider)
    }

    override fun spaceExistenceTypeBetweenTokens(left: ASTNode?, right: ASTNode?): ParserDefinition.SpaceRequirements {
        return ParserDefinition.SpaceRequirements.MAY
    }

    override fun createElement(node: ASTNode?): PsiElement {
        return CsvTypes.Factory.createElement(node)
    }
}
