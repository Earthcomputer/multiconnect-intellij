@file:JvmName("CsvPsiImplUtil")

package net.earthcomputer.multiconnectintellij.csv.psi.impl

import com.intellij.psi.PsiReference
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry
import net.earthcomputer.multiconnectintellij.csv.psi.CsvEntry
import net.earthcomputer.multiconnectintellij.csv.psi.CsvFile
import net.earthcomputer.multiconnectintellij.csv.psi.CsvHeader
import net.earthcomputer.multiconnectintellij.csv.psi.CsvIdentifier
import net.earthcomputer.multiconnectintellij.csv.psi.CsvKvPair
import net.earthcomputer.multiconnectintellij.csv.psi.CsvProperties
import net.earthcomputer.multiconnectintellij.csv.psi.CsvRow
import net.earthcomputer.multiconnectintellij.csv.psi.CsvStringValue
import net.earthcomputer.multiconnectintellij.csv.psi.csvElementFactory
import java.util.Collections

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

fun setEntry(element: CsvRow, key: String, entry: CsvEntry): CsvEntry? {
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

