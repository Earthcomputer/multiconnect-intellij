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
import net.earthcomputer.multiconnectintellij.csv.psi.CsvEntry
import net.earthcomputer.multiconnectintellij.csv.psi.CsvTypes
import net.earthcomputer.multiconnectintellij.csv.psi.stubs.CsvEntryStub

class CsvEntryStubImpl(
    override val namespace: String,
    override val path: String,
    parent: StubElement<*>?
) : StubBase<CsvEntry>(parent, CsvTypes.ENTRY as IStubElementType<*, *>), CsvEntryStub

class CsvEntryType(debugName: String) : ILightStubElementType<CsvEntryStub, CsvEntry>(debugName, CsvLanguage) {
    override fun getExternalId() = "multiconnect.csv.entry"

    override fun deserialize(dataStream: StubInputStream, parentStub: StubElement<*>?): CsvEntryStub {
        val namespace = dataStream.readNameString() ?: "minecraft"
        val path = dataStream.readNameString() ?: "null"
        return CsvEntryStubImpl(namespace, path, parentStub)
    }

    override fun createStub(tree: LighterAST, node: LighterASTNode, parentStub: StubElement<*>): CsvEntryStub {
        val identifier = tree.getChildren(node).firstOrNull { it.tokenType == CsvTypes.IDENTIFIER }
            ?: return CsvEntryStubImpl("minecraft", "null", parentStub)
        val parts = tree.getChildren(identifier)
            .filter { it.tokenType == CsvTypes.STRING_VALUE }
            .flatMap { tree.getChildren(it) }
        val namespace = if (parts.size < 2) "minecraft" else RecordUtil.intern(tree.charTable, parts[parts.size - 2])
        val path = if (parts.isEmpty()) "null" else RecordUtil.intern(tree.charTable, parts[parts.size - 1])
        return CsvEntryStubImpl(namespace, path, parentStub)
    }

    override fun createStub(psi: CsvEntry, parentStub: StubElement<out PsiElement>?): CsvEntryStub {
        return CsvEntryStubImpl(psi.namespace, psi.path, parentStub)
    }

    override fun createPsi(stub: CsvEntryStub): CsvEntry {
        return CsvEntryImpl(stub, this)
    }

    override fun indexStub(stub: CsvEntryStub, sink: IndexSink) {
        // TODO?
    }

    override fun serialize(stub: CsvEntryStub, dataStream: StubOutputStream) {
        dataStream.writeName(stub.namespace)
        dataStream.writeName(stub.path)
    }
}
