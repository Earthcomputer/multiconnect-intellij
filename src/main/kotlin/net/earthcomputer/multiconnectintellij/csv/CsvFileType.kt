package net.earthcomputer.multiconnectintellij.csv

import com.intellij.openapi.fileTypes.LanguageFileType
import com.intellij.openapi.fileTypes.ex.FileTypeIdentifiableByVirtualFile
import com.intellij.openapi.vfs.VirtualFile
import net.earthcomputer.multiconnectintellij.Icons

object CsvFileType : LanguageFileType(CsvLanguage), FileTypeIdentifiableByVirtualFile {
    val versionRegex = "1\\.\\d+(?:\\.\\d+)?".toRegex()

    override fun getName() = "Multiconnect CSV File"

    override fun getDescription() = "Multiconnect data CSV file"

    override fun getDefaultExtension() = ""

    override fun getIcon() = Icons.multiconnectCsv

    override fun isMyFileType(file: VirtualFile): Boolean {
        if (file.extension != "csv") {
            return false
        }

        val parentFile = file.parent ?: return false
        if (versionRegex.matches(parentFile.name)) {
            val grandparentFile = parentFile.parent ?: return false
            if (grandparentFile.name == "data") {
                return true
            }
        } else {
            if (file.name != "protocols.csv") {
                return false
            }
            return parentFile.name == "data"
        }

        return false
    }
}
