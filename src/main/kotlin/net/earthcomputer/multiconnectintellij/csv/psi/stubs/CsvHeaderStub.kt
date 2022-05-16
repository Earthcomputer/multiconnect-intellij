package net.earthcomputer.multiconnectintellij.csv.psi.stubs

import com.intellij.psi.stubs.StubElement
import net.earthcomputer.multiconnectintellij.csv.psi.CsvHeader

interface CsvHeaderStub : StubElement<CsvHeader> {
    val keys: Array<String>
}
