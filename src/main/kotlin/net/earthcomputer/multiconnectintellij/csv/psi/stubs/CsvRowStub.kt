package net.earthcomputer.multiconnectintellij.csv.psi.stubs

import com.intellij.psi.stubs.StubElement
import net.earthcomputer.multiconnectintellij.csv.psi.CsvEntry
import net.earthcomputer.multiconnectintellij.csv.psi.CsvRow

interface CsvRowStub : StubElement<CsvRow> {
    val entries: Array<CsvEntry>
}
