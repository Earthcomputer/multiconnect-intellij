package net.earthcomputer.multiconnectintellij.csv

import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class CsvSyntaxHighlighterFactory : SyntaxHighlighterFactory() {
    override fun getSyntaxHighlighter(project: Project?, virtualFile: VirtualFile?): CsvSyntaxHighlighter {
        return CsvSyntaxHighlighter()
    }
}
