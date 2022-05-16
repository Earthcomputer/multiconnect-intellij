package net.earthcomputer.multiconnectintellij.csv.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.application.runReadAction
import com.intellij.psi.FileViewProvider
import com.intellij.psi.SmartPsiElementPointer
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.createSmartPointer
import com.intellij.util.AstLoadingFilter
import net.earthcomputer.multiconnectintellij.csv.CsvFileType
import net.earthcomputer.multiconnectintellij.csv.CsvLanguage

private typealias RowsByKeyMap = Map<Pair<String, String>, List<SmartPsiElementPointer<CsvRow>>>

class CsvFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, CsvLanguage) {
    override fun getFileType() = CsvFileType
    override fun toString() = "Multiconnect CSV File"

    val header: CsvHeader? get() {
        val stub = stub
        if (stub != null) {
            return stub.getChildrenByType<CsvHeader>(CsvTypes.HEADER) { arrayOfNulls<CsvHeader>(it) }
                .singleOrNull() as CsvHeader?
        }

        return findChildByClass(CsvHeader::class.java)
    }

    val rows: Array<CsvRow> get() {
        val stub = stub
        if (stub != null) {
            @Suppress("UNCHECKED_CAST")
            return stub.getChildrenByType<CsvRow>(CsvTypes.ROW) { arrayOfNulls<CsvRow>(it) } as Array<CsvRow>
        }

        return this.findChildrenByClass(CsvRow::class.java)
    }

    private fun getRowsByKeyMap(key: String): RowsByKeyMap {
        return AstLoadingFilter.disallowTreeLoading<RowsByKeyMap, Throwable> {
            val keyIndex = header?.keyNames?.indexOf(key)?.takeIf { it >= 0 } ?: return@disallowTreeLoading emptyMap()
            val cachedValue = CachedValuesManager.getCachedValue(this) {
                CachedValueProvider.Result(mutableMapOf<String, RowsByKeyMap>(), this)
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

            return@disallowTreeLoading result!!
        }
    }

    private fun computeRowsByKeyMap(keyIndex: Int): RowsByKeyMap {
        return this.rows.asSequence()
            .mapNotNull { row -> row.entries.getOrNull(keyIndex)?.let { (it.namespace to it.path) to row.createSmartPointer() } }
            .groupBy({ it.first }) { it.second }
    }

    fun getRowByKey(key: String, value: String): CsvRow? {
        return getRowsByKey(key, value).singleOrNull()
    }

    fun getRowByKey(key: String, namespace: String, path: String): CsvRow? {
        return getRowsByKey(key, namespace, path).singleOrNull()
    }

    fun getRowsByKey(key: String, value: String): List<CsvRow> {
        return getRowsByKeyMap(key)["minecraft" to value]?.mapNotNull { it.element } ?: emptyList()
    }

    fun getRowsByKey(key: String, namespace: String, path: String): List<CsvRow> {
        return getRowsByKeyMap(key)[namespace to path]?.mapNotNull { it.element } ?: emptyList()
    }
}
