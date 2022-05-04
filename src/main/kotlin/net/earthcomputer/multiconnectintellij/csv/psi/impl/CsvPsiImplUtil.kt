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

fun getKeyNames(element: CsvHeader): List<String> {
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

fun getNormalizedString(element: CsvIdentifier): String {
    return "${element.namespace}:${element.path}"
}

fun getEntry(element: CsvRow, name: String): CsvEntry? {
    val file = element.containingFile as? CsvFile ?: return null
    val index = file.header?.keyNames?.indexOf(name) ?: return null
    return element.entryList.getOrNull(index)
}

fun getKey(element: CsvEntry): String? {
    val row = element.parent as? CsvRow ?: return null
    val file = element.containingFile as? CsvFile ?: return null
    val index = row.entryList.indexOf(element)
    return file.header?.keyNames?.getOrNull(index)
}

fun getReferences(element: CsvStringValue): Array<PsiReference> {
    return ReferenceProvidersRegistry.getReferencesFromProviders(element)
}

