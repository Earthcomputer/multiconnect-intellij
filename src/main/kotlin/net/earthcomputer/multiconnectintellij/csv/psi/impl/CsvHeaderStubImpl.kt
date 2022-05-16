package net.earthcomputer.multiconnectintellij.csv.psi.impl

import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.cache.RecordUtil
import com.intellij.psi.stubs.ILightStubElementType
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.stubs.IndexSink
import com.intellij.psi.stubs.StubBase
import com.intellij.psi.stubs.StubElement
import com.intellij.psi.stubs.StubInputStream
import com.intellij.psi.stubs.StubOutputStream
import net.earthcomputer.multiconnectintellij.csv.CsvLanguage
import net.earthcomputer.multiconnectintellij.csv.psi.CsvHeader
import net.earthcomputer.multiconnectintellij.csv.psi.CsvTypes
import net.earthcomputer.multiconnectintellij.csv.psi.stubs.CsvHeaderStub

class CsvHeaderStubImpl(
    override val keys: Array<String>, parent: StubElement<*>?
) : StubBase<CsvHeader>(parent, CsvTypes.HEADER as IStubElementType<*, *>), CsvHeaderStub

class CsvHeaderType(debugName: String) : ILightStubElementType<CsvHeaderStub, CsvHeader>(debugName, CsvLanguage) {
    override fun getExternalId() = "multiconnect.csv.header"

    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): CsvHeaderStub {
        val keys = (0 until dataStream.readVarInt()).mapNotNull { dataStream.readNameString() }.toTypedArray()
        return CsvHeaderStubImpl(keys, parentStub)
    }

    override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): CsvHeaderStub {
        val keys = tree.getChildren(node).filter { it.tokenType == CsvTypes.STRING_VALUE }
            .flatMap { tree.getChildren(it) }
            .map { RecordUtil.intern(tree.charTable, it) }
            .toTypedArray()
        return CsvHeaderStubImpl(keys, parentStub)
    }

    override fun createStub(psi: CsvHeader, parentStub: StubElement<out PsiElement>?): CsvHeaderStub {
        return CsvHeaderStubImpl(psi.keyNames.toTypedArray(), parentStub)
    }

    override fun createPsi(stub: CsvHeaderStub): CsvHeader {
        return CsvHeaderImpl(stub, this)
    }

    override fun indexStub(stub: CsvHeaderStub, sink: IndexSink) {
        // TODO?
    }

    override fun serialize(stub: CsvHeaderStub, dataStream: StubOutputStream) {
        dataStream.writeVarInt(stub.keys.size)
        for (key in stub.keys) {
            dataStream.writeName(key)
        }
    }
}
