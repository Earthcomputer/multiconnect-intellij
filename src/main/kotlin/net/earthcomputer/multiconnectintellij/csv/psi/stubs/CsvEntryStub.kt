package net.earthcomputer.multiconnectintellij.csv.psi.stubs

import com.intellij.psi.stubs.StubElement
import net.earthcomputer.multiconnectintellij.csv.psi.CsvEntry

interface CsvEntryStub : StubElement<CsvEntry> {
    val namespace: String
    val path: String
}
