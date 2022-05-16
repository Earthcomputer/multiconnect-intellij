package net.earthcomputer.multiconnectintellij.csv.psi.impl

import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.ILightStubElementType
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import net.earthcomputer.multiconnectintellij.csv.CsvLanguage
import net.earthcomputer.multiconnectintellij.csv.psi.CsvEntry
import net.earthcomputer.multiconnectintellij.csv.psi.CsvRow
import net.earthcomputer.multiconnectintellij.csv.psi.CsvTypes
import net.earthcomputer.multiconnectintellij.csv.psi.stubs.CsvRowStub

class CsvRowStubImpl(
    parent: StubElement<*>?
) : StubBase<CsvRow>(parent, CsvTypes.ROW as IStubElementType<*, *>), CsvRowStub {
    override val entries: Array<CsvEntry>
        get() = getChildrenByType(CsvTypes.ENTRY) { arrayOfNulls<CsvEntry>(it) }
}

class CsvRowType(debugName: String) : ILightStubElementType<CsvRowStub, CsvRow>(debugName, CsvLanguage) {
    override fun getExternalId() = "multiconnect.csv.row"

    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): CsvRowStub {
        return CsvRowStubImpl(parentStub)
    }

    override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): CsvRowStub {
        return CsvRowStubImpl(parentStub)
    }

    override fun createStub(psi: CsvRow, parentStub: StubElement<out PsiElement>?): CsvRowStub {
        return CsvRowStubImpl(parentStub)
    }

    override fun createPsi(stub: CsvRowStub): CsvRow {
        return CsvRowImpl(stub, this)
    }

    override fun indexStub(stub: CsvRowStub, sink: IndexSink) {
        // TODO?
    }

    override fun serialize(stub: CsvRowStub, dataStream: StubOutputStream) {
    }
}
