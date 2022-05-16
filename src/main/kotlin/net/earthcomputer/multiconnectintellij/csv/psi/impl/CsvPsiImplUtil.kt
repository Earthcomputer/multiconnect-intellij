@file:JvmName("CsvPsiImplUtil")

package net.earthcomputer.multiconnectintellij.csv.psi.impl

import com.intellij.lang.LighterAST
import com.intellij.lang.LighterASTNode
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiReference
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry
import com.intellij.psi.impl.source.tree.LightTreeUtil
import com.intellij.util.IncorrectOperationException
import net.earthcomputer.multiconnectintellij.csv.psi.CsvEntry
import net.earthcomputer.multiconnectintellij.csv.psi.CsvFile
import net.earthcomputer.multiconnectintellij.csv.psi.CsvHeader
import net.earthcomputer.multiconnectintellij.csv.psi.CsvIdentifier
import net.earthcomputer.multiconnectintellij.csv.psi.CsvKvPair
import net.earthcomputer.multiconnectintellij.csv.psi.CsvProperties
import net.earthcomputer.multiconnectintellij.csv.psi.CsvRow
import net.earthcomputer.multiconnectintellij.csv.psi.CsvStringValue
import net.earthcomputer.multiconnectintellij.csv.psi.CsvTypes
import net.earthcomputer.multiconnectintellij.csv.psi.csvElementFactory
import java.util.Collections
import java.util.function.UnaryOperator

fun getKeyNames(element: CsvHeader): List<String> {
    val stub = element.stub
    if (stub != null) {
        return stub.keys.toList()
    }
    return element.keyElements.map { it.text }
}

fun getKey(element: CsvKvPair): String {
    return element.keyElement.text
}

fun getValue(element: CsvKvPair): String? {
    return element.valueElement?.text
}

fun getProperty(properties: CsvProperties, key: String): String? {
    return properties.kvPairList.firstOrNull { it.key == key }?.value
}

fun toMap(properties: CsvProperties): Map<String, String> {
    return properties.kvPairList.mapNotNull { kvPair -> kvPair.value?.let { kvPair.key to it } }.toMap()
}

fun getNamespaceElement(element: CsvIdentifier): CsvStringValue? {
    val stringElements = element.stringElements
    if (stringElements.size == 2) {
        return stringElements[0]
    }
    return null
}

fun getPathElement(element: CsvIdentifier): CsvStringValue {
    return element.stringElements.last()
}

fun getNamespace(element: CsvIdentifier): String {
    return element.namespaceElement?.text ?: "minecraft"
}

fun getPath(element: CsvIdentifier): String {
    return element.pathElement.text
}

fun getName(element: CsvIdentifier): String {
    return element.text
}

fun setName(element: CsvIdentifier, name: String): CsvIdentifier {
    return element.replace(element.manager.csvElementFactory.createIdentifierFromText(name)) as CsvIdentifier
}

fun getNormalizedString(element: CsvIdentifier): String {
    return "${element.namespace}:${element.path}"
}

fun getEntries(element: CsvRow): List<CsvEntry> {
    val stub = element.stub
    if (stub != null) {
        return stub.entries.toList()
    }
    return element.entryList
}

fun getEntry(element: CsvRow, name: String): CsvEntry? {
    val file = element.containingFile as? CsvFile ?: return null
    val index = file.header?.keyNames?.indexOf(name) ?: return null
    return element.entries.getOrNull(index)
}

@JvmOverloads
fun setEntry(element: CsvRow, key: String, entry: CsvEntry, loadAst: Boolean = true): CsvEntry? {
    return if (!loadAst && element.stub != null) {
        // avoid loading the AST
        setEntryWithLightASTToText(element, key) { _, _ -> entry.text }
    } else {
        setEntryWithPsi(element, key, entry)
    }
}

private inline fun setEntryWithLightASTToText(
    element: CsvRow,
    key: String,
    textProvider: (LighterAST, List<LighterASTNode>) -> String
): CsvEntry? {
    val file = element.containingFile as? CsvFile ?: return null
    val entryIndex = file.header?.keyNames?.indexOf(key)?.takeIf { it >= 0 } ?: return null
    val rowIndex = file.rows.indexOf(element).takeIf { it >= 0 } ?: return null

    val ast = file.calcTreeElement().lighterAST
    val rowAst = ast.getChildren(ast.root).asSequence().filter { it.tokenType == CsvTypes.ROW }.drop(rowIndex).firstOrNull() ?: return null
    val entries = ast.getChildren(rowAst).filter { it.tokenType == CsvTypes.ENTRY }
    val entryText = textProvider(ast, entries)
    val entryAst = entries.getOrNull(entryIndex)

    val documentManager = PsiDocumentManager.getInstance(file.project)
    val document = documentManager.getDocument(file) ?: return null
    if (entryAst == null) {
        val numExisting = element.entries.size
        val toAppend = buildString {
            if (numExisting != 0) {
                append(" ")
            }
            for (_i in 0 until (entryIndex - numExisting)) {
                append("null ")
            }
            append(entryText)
        }
        document.insertString(rowAst.endOffset, toAppend)
    } else {
        document.replaceString(entryAst.startOffset, entryAst.endOffset, entryText)
    }
    return null
}

private fun setEntryWithPsi(element: CsvRow, key: String, entry: CsvEntry): CsvEntry? {
    val file = element.containingFile as? CsvFile ?: return null
    val index = file.header?.keyNames?.indexOf(key)?.takeIf { it >= 0 } ?: return null

    // fill up entries with null if necessary
    val numEntries = element.entryList.size
    if (numEntries <= index) {
        val fakeRow = element.manager.csvElementFactory.createRowFromText(
            Collections.nCopies(index - numEntries + 2, "null")
                .joinToString(" ")
        )
        val children = fakeRow.children
        // start at the first space if there are already entries
        val firstIndex = if (numEntries == 0) 2 else 1
        element.addRange(children[firstIndex], children.last())
    }

    return element.entryList[index].replace(entry) as CsvEntry
}

@JvmOverloads
fun copyEntry(element: CsvRow, fromKey: String, toKey: String, loadAst: Boolean = true) {
    if (loadAst || element.stub == null) {
        val entry = getEntry(element, fromKey) ?: return
        setEntryWithPsi(element, toKey, entry)
        return
    }

    copyEntryWithAST(element, fromKey, toKey)
}

private fun copyEntryWithAST(element: CsvRow, fromKey: String, toKey: String) {
    val file = element.containingFile as? CsvFile ?: return
    val fromIndex = file.header?.keyNames?.indexOf(fromKey)?.takeIf { it >= 0 } ?: return

    setEntryWithLightASTToText(element, toKey) { ast, entries ->
        val entry = entries.getOrNull(fromIndex) ?: return
        LightTreeUtil.toFilteredString(ast, entry, null)
    }
}

@JvmOverloads
fun replaceEntry(element: CsvRow, key: String, func: UnaryOperator<CsvEntry>, loadAst: Boolean = true) {
    if (loadAst || element.stub == null) {
        val entry = getEntry(element, key) ?: return
        val newEntry = func.apply(entry)
        setEntryWithPsi(element, key, newEntry)
    }

    val file = element.containingFile as? CsvFile ?: return
    val index = file.header?.keyNames?.indexOf(key)?.takeIf { it >= 0 } ?: return

    setEntryWithLightASTToText(element, key) { ast, entries ->
        val entryAst = entries.getOrNull(index) ?: return
        val entryText = LightTreeUtil.toFilteredString(ast, entryAst, null)
        val entry = try {
            element.manager.csvElementFactory.createEntryFromText(entryText)
        } catch (e: IncorrectOperationException) {
            return
        }
        val newEntry = func.apply(entry)
        newEntry.text
    }
}

fun getNamespace(element: CsvEntry): String {
    val stub = element.stub
    if (stub != null) {
        return stub.namespace
    }
    return element.identifier.namespace
}

fun getPath(element: CsvEntry): String {
    val stub = element.stub
    if (stub != null) {
        return stub.path
    }
    return element.identifier.path
}

fun getKey(element: CsvEntry): String? {
    val row = element.parent as? CsvRow ?: return null
    val file = element.containingFile as? CsvFile ?: return null
    val index = row.entries.indexOf(element)
    return file.header?.keyNames?.getOrNull(index)
}

fun getReferences(element: CsvStringValue): Array<PsiReference> {
    return ReferenceProvidersRegistry.getReferencesFromProviders(element)
}

