package net.earthcomputer.multiconnectintellij.action

import com.intellij.lang.Language
import com.intellij.refactoring.actions.BaseRefactoringAction
import net.earthcomputer.multiconnectintellij.csv.CsvLanguage

abstract class BaseCsvRefactoringAction : BaseRefactoringAction() {
    override fun isAvailableForLanguage(language: Language): Boolean {
        return language == CsvLanguage
    }
}
