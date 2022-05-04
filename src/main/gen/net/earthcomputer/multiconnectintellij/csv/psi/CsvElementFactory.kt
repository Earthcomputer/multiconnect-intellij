package net.earthcomputer.multiconnectintellij.csv.psi

import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import com.intellij.util.IncorrectOperationException
import net.earthcomputer.multiconnectintellij.csv.CsvLanguage

class CsvElementFactory internal constructor(private val psiManager: PsiManager) {
    fun createFileFromText(text: String): CsvFile {
        return PsiFileFactory.getInstance(psiManager.project).createFileFromText("_Dummy_.csv", CsvLanguage, text) as CsvFile
    }

    fun createHeaderFromText(text: String): CsvHeader {
        return createFileFromText(text).header ?: throw IncorrectOperationException(text)
    }

    fun createRowFromText(text: String): CsvRow {
        return createFileFromText("foo\n$text").rows.singleOrNull() ?: throw IncorrectOperationException(text)
    }

    fun createEntryFromText(text: String): CsvEntry {
        return createRowFromText(text).entryList.singleOrNull() ?: throw IncorrectOperationException(text)
    }

    fun createIdentifierFromText(text: String): CsvIdentifier {
        return createEntryFromText(text).identifier
    }

    fun createStringValueFromText(text: String): CsvStringValue {
        return createHeaderFromText(text).keyElements.singleOrNull() ?: throw IncorrectOperationException(text)
    }

    fun createKvPairFromText(text: String): CsvKvPair {
        return createEntryFromText("a[$text]").properties?.kvPairList?.singleOrNull() ?: throw IncorrectOperationException(text)
    }
}

val PsiManager.csvElementFactory: CsvElementFactory get() {
    return CsvElementFactory(this)
}
