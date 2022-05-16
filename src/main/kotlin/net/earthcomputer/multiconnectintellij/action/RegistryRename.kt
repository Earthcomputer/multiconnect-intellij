package net.earthcomputer.multiconnectintellij.action

import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiLiteral
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.createSmartPointer
import com.intellij.psi.util.parentOfType
import com.intellij.refactoring.BaseRefactoringProcessor
import com.intellij.refactoring.RefactorJBundle
import com.intellij.refactoring.RefactoringActionHandler
import com.intellij.refactoring.ui.RefactoringDialog
import com.intellij.refactoring.util.CommonRefactoringUtil
import com.intellij.refactoring.util.FixableUsageInfo
import com.intellij.ui.EnumComboBoxModel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.selected
import com.intellij.usageView.BaseUsageViewDescriptor
import com.intellij.usageView.UsageInfo
import com.intellij.usageView.UsageViewDescriptor
import com.intellij.util.IncorrectOperationException
import net.earthcomputer.multiconnectintellij.RegistryReference
import net.earthcomputer.multiconnectintellij.csv.CsvFileType
import net.earthcomputer.multiconnectintellij.csv.psi.CsvEntry
import net.earthcomputer.multiconnectintellij.csv.psi.CsvFile
import net.earthcomputer.multiconnectintellij.csv.psi.CsvRow
import net.earthcomputer.multiconnectintellij.csv.psi.csvElementFactory
import javax.swing.JComponent

class RegistryRenameAction : BaseCsvRefactoringAction() {
    override fun isAvailableInEditorOnly() = false

    override fun isAvailableOnElementInEditorAndFile(
        element: PsiElement,
        editor: Editor,
        file: PsiFile,
        context: DataContext
    ): Boolean {
        return element.parentOfType<CsvEntry>()?.key == "name"
    }

    override fun isEnabledOnElements(elements: Array<PsiElement>): Boolean {
        return elements.singleOrNull()?.parentOfType<CsvEntry>()?.key == "name"
    }

    override fun getHandler(dataContext: DataContext): RefactoringActionHandler = RegistryRenameHandler()
}

private class RegistryRenameHandler : RefactoringActionHandler {
    override fun invoke(project: Project, editor: Editor, file: PsiFile?, dataContext: DataContext) {
        val scrollingModel = editor.scrollingModel
        scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)
        val element = CommonDataKeys.PSI_ELEMENT.getData(dataContext)?.parentOfType<CsvEntry>()
        if (element == null) {
            CommonRefactoringUtil.showErrorHint(
                project,
                editor,
                RefactorJBundle.message("cannot.perform.the.refactoring") + "The caret should be positioned at the CSV entry to be refactored.",
                "Registry Rename",
                null
            )
        } else {
            invoke(element, editor)
        }
    }

    override fun invoke(project: Project, elements: Array<PsiElement>, dataContext: DataContext) {
        val entry = elements.singleOrNull()?.parentOfType<CsvEntry>() ?: return
        val editor = CommonDataKeys.EDITOR.getData(dataContext)
        invoke(entry, editor)
    }

    private fun invoke(entry: CsvEntry, editor: Editor?) {
        val (selectedVersion, versions) = getSelectedVersionAndVersions(entry.containingFile) ?: run {
            CommonRefactoringUtil.showErrorHint(
                entry.project,
                editor,
                RefactorJBundle.message("cannot.perform.the.refactoring") + "Not a registry file.",
                "Registry Rename",
                null
            )
            return
        }
        versions.sortWith(::compareVersions)
        RegistryRenameDialog(entry, selectedVersion, versions).show()
    }

    // Do not override isAvailableForFile on the Action as that is required to execute quickly
    // (no file system operations)
    private fun getSelectedVersionAndVersions(file: PsiFile): Pair<String, Array<String>>? {
        val vFile = file.virtualFile
        if (vFile.name == "cpackets.csv" || vFile.name == "spackets.csv") {
            return null
        }

        val protocolDir = vFile?.parent ?: return null
        if (!CsvFileType.versionRegex.matches(protocolDir.name)) {
            return null
        }
        val dataDir = protocolDir.parent ?: return null
        if (dataDir.name != "data") {
            return null
        }

        val versions = dataDir.children.filter { it.isDirectory && CsvFileType.versionRegex.matches(it.name) }
            .map { it.name }
            .toTypedArray()
            .ifEmpty { return null }

        return protocolDir.name to versions
    }
}

class RegistryRenameDialog(
    private val entry: CsvEntry,
    selectedVersion: String,
    private val versions: Array<String>
) : RefactoringDialog(entry.project, true) {
    var beforeAfter = BeforeAfter.BEFORE
    var version = selectedVersion
    var newName: String = entry.text
    var moveExistingToOld = true

    private lateinit var dialogPanel: DialogPanel

    init {
        title = "Registry Rename"
        init()
    }

    @Suppress("UnstableApiUsage")
    override fun createCenterPanel(): JComponent {
        return panel {
            row("Name:") {
                textField().bindText(::newName).applyIfEnabled().validationOnInput { textField ->
                    try {
                        entry.manager.csvElementFactory.createEntryFromText(textField.text)
                        return@validationOnInput null
                    } catch (e: IncorrectOperationException) {
                        val message = if (textField.text.isBlank()) {
                            "Enter a new name"
                        } else {
                            e.message?.substringBefore('\n')?.let { "Incorrect syntax for CSV entry: $it" }
                                ?: "Incorrect syntax for CSV entry"
                        }
                        return@validationOnInput error(message)
                    }
                }.focused().component.selectAll()
            }

            group("Old Name Handling") {
                var myCheckBoxMut: JBCheckBox? = null
                row("Move existing name to oldName:") {
                    myCheckBoxMut = checkBox("").bindSelected(::moveExistingToOld).component
                }
                val myCheckBox = myCheckBoxMut!!
                row("Versions:") {
                    comboBox(EnumComboBoxModel(BeforeAfter::class.java)).enabledIf(myCheckBox.selected)
                        .bindItem(::beforeAfter)
                    comboBox(versions).enabledIf(myCheckBox.selected).bindItem(::version)
                }
            }
        }.also {
            dialogPanel = it
        }
    }

    override fun updateErrorInfo(info: MutableList<ValidationInfo>) {
        super.updateErrorInfo(info)
        refactorAction.isEnabled = okAction.isEnabled
        previewAction.isEnabled = okAction.isEnabled
    }

    override fun postponeValidation() = false

    override fun doAction() {
        if (doValidateAll().isNotEmpty()) {
            return
        }

        dialogPanel.apply()

        val oldName = entry.text
        if (oldName == newName) {
            return
        }

        invokeRefactoring(RegistryRenameProcessor(
            entry,
            beforeAfter,
            version,
            oldName,
            newName,
            moveExistingToOld,
        ))
    }
}

private class RegistryRenameProcessor(
    private val entry: CsvEntry,
    private val beforeAfter: BeforeAfter,
    private val version: String,
    private val oldName: String,
    private val newName: String,
    private val moveExistingToOld: Boolean,
) : BaseRefactoringProcessor(entry.project) {
    override fun createUsageViewDescriptor(usages: Array<UsageInfo>): UsageViewDescriptor {
        return BaseUsageViewDescriptor(entry)
    }

    override fun findUsages(): Array<UsageInfo> {
        val usages = mutableListOf<UsageInfo>()
        val entryProperties = entry.properties?.toMap()
        val javaReferences = mutableSetOf<RegistryReference>()

        val virtualFile = entry.containingFile.virtualFile ?: return UsageInfo.EMPTY_ARRAY
        val dataDir = virtualFile.parent?.parent ?: return UsageInfo.EMPTY_ARRAY
        for (protocolDir in dataDir.children) {
            if (!protocolDir.isDirectory || !CsvFileType.versionRegex.matches(protocolDir.name)) {
                continue
            }
            val registryFile = protocolDir.findChild(virtualFile.name) ?: continue
            val registryPsi = entry.manager.findFile(registryFile) as? CsvFile ?: continue
            var rows = registryPsi.getRowsByKey("name", entry.namespace, entry.path)
                .reversed() // reverse so later stuff in the document is modified first
            if (entryProperties != null) {
                rows = rows.filter {
                        val nameEntry = it.getEntry("name") ?: return@filter false
                        nameEntry.properties?.toMap() == entryProperties
                    }
            }
            val keepOldName = moveExistingToOld && run {
                val cmp = compareVersions(protocolDir.name, version)
                when (beforeAfter) {
                    BeforeAfter.BEFORE -> cmp <= 0
                    BeforeAfter.AFTER -> cmp >= 0
                }
            }
            usages += rows.map { RenameInCsv(it, newName, keepOldName) }

            rows.flatMapTo(javaReferences) { row ->
                val nameElement = row.getEntry("name") ?: return@flatMapTo emptyList()
                ReferencesSearch.search(nameElement).filterIsInstance<RegistryReference>()
            }

            if (virtualFile.name == "block.csv") {
                val blockStateFile = protocolDir.findChild("block_state.csv") ?: continue
                val blockStatePsi = entry.manager.findFile(blockStateFile) as? CsvFile ?: continue
                val blockStateRows = blockStatePsi.getRowsByKey("name", entry.namespace, entry.path)
                usages += blockStateRows.map { RenameBlockState(it, newName, keepOldName) }
            }
        }

        javaReferences.mapTo(usages) { RenameJavaReference(it.element, newName) }

        return usages.toTypedArray()
    }

    override fun performRefactoring(usages: Array<UsageInfo>) {
        for (usage in usages) {
            when (usage) {
                is FixableUsageInfo -> usage.fixUsage()
                is MyFixableUsageInfo -> usage.fixUsage()
            }
        }
        PsiDocumentManager.getInstance(myProject).commitAllDocuments()
    }

    override fun getCommandName(): String {
        return "Rename registry entry \"$oldName\" to \"$newName\""
    }
}

private abstract class MyFixableUsageInfo(element: PsiElement) : UsageInfo(element.containingFile) {
    private val myActualElement = element.createSmartPointer(project)

    val actualElement get() = myActualElement.element

    abstract fun fixUsage()
}

private class RenameInCsv(
    element: CsvRow,
    private val toName: String,
    private val keepOldName: Boolean
) : MyFixableUsageInfo(element) {
    override fun fixUsage() {
        val row = actualElement as? CsvRow ?: return

        if (keepOldName && row.getEntry("oldName") == null) {
            row.copyEntry("name", "oldName", false)
        }

        val newName = row.manager.csvElementFactory.createEntryFromText(toName)
        row.setEntry("name", newName, false)
    }
}

private class RenameBlockState(
    element: CsvRow,
    private val toName: String,
    private val keepOldName: Boolean
) : MyFixableUsageInfo(element) {
    override fun fixUsage() {
        val row = actualElement as? CsvRow ?: return

        val newName = row.manager.csvElementFactory.createEntryFromText(toName)

        if (keepOldName && row.getEntry("oldName") == null) {
            row.copyEntry("oldName", "name", false)
        }

        row.replaceEntry("name", {
            it.identifier.replace(newName.identifier)
            it
        }, false)
    }
}

private class RenameJavaReference(
    element: PsiLiteral,
    private val toName: String
) : FixableUsageInfo(element) {
    override fun fixUsage() {
        val element = element as? PsiLiteral ?: return
        val text = "\"${StringUtil.escapeStringCharacters(toName)}\""
        element.replace(JavaPsiFacade.getElementFactory(element.project).createExpressionFromText(text, element))
    }

}

// simple semver comparison, only works for release versions
private fun compareVersions(a: String, b: String): Int {
    val aParts = a.split('.')
    val bParts = b.split('.')
    return aParts.asSequence()
        .map { it.toIntOrNull() }
        .zip(bParts.asSequence().map { it.toIntOrNull() })
        .map { (a, b) -> nullsLast<Int>().compare(a, b) }
        .firstOrNull { it != 0 }
        ?: aParts.size.compareTo(bParts.size)
}

enum class BeforeAfter {
    BEFORE, AFTER;

    override fun toString() = StringUtil.capitalize(name.lowercase()) + " (Inclusive)"
}
