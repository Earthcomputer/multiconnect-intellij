package net.earthcomputer.multiconnectintellij.csv.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.application.runReadAction
import com.intellij.psi.FileViewProvider
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import net.earthcomputer.multiconnectintellij.csv.CsvFileType
import net.earthcomputer.multiconnectintellij.csv.CsvLanguage

class CsvFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, CsvLanguage) {
    override fun getFileType() = CsvFileType
    override fun toString() = "Multiconnect CSV File"

    val header: CsvHeader? get() {
        return CachedValuesManager.getCachedValue(this) {
            CachedValueProvider.Result(this.findChildByClass(CsvHeader::class.java), this)
        }
    }

    val rows: Array<CsvRow> get() {
        return this.findChildrenByClass(CsvRow::class.java)
    }

    private fun getRowsByKeyMap(key: String): Map<Pair<String, String>, List<CsvRow>> {
        val keyIndex = header?.keyNames?.indexOf(key)?.takeIf { it >= 0 } ?: return emptyMap()
        val cachedValue = CachedValuesManager.getCachedValue(this) {
            CachedValueProvider.Result(mutableMapOf<String, Map<Pair<String, String>, List<CsvRow>>>(), this)
        }
        var result = cachedValue[key]
        if (result == null) {
            runReadAction {
                synchronized(cachedValue) {
                    result = cachedValue[key]
                    if (result == null) {
                        val newResult = computeRowsByKeyMap(keyIndex)
                        cachedValue[key] = newResult
                        result = newResult
                    }
                }
            }
        }

        return result!!
    }

    private fun computeRowsByKeyMap(keyIndex: Int): Map<Pair<String, String>, List<CsvRow>> {
        return this.findChildrenByClass(CsvRow::class.java).asSequence()
            .mapNotNull { row -> row.entryList.getOrNull(keyIndex)?.let { (it.identifier.namespace to it.identifier.path) to row } }
            .groupBy({ it.first }) { it.second }
    }

    fun getRowByKey(key: String, value: String): CsvRow? {
        return getRowsByKey(key, value).singleOrNull()
    }

    fun getRowByKey(key: String, namespace: String, path: String): CsvRow? {
        return getRowsByKey(key, namespace, path).singleOrNull()
    }

    fun getRowsByKey(key: String, value: String): List<CsvRow> {
        return getRowsByKeyMap(key)["minecraft" to value] ?: emptyList()
    }

    fun getRowsByKey(key: String, namespace: String, path: String): List<CsvRow> {
        return getRowsByKeyMap(key)[namespace to path] ?: emptyList()
    }
}
