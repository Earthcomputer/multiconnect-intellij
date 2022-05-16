package net.earthcomputer.multiconnectintellij.csv.psi.stubs

import com.intellij.lang.ASTNode
import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.psi.stubs.LightStubBuilder
import net.earthcomputer.multiconnectintellij.csv.psi.CsvTypes

class CsvStubBuilder : LightStubBuilder() {
    override fun skipChildProcessingWhenBuildingStubs(parent: ASTNode, node: ASTNode): Boolean {
        // skip over entry children
        return parent.elementType == CsvTypes.ENTRY
    }

    override fun skipChildProcessingWhenBuildingStubs(
        tree: LighterAST,
        parent: LighterASTNode,
        node: LighterASTNode
    ): Boolean {
        // skip over entry children
        return parent.tokenType == CsvTypes.ENTRY
    }
}
