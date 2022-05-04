package net.earthcomputer.multiconnectintellij.csv

import com.intellij.openapi.project.Project
import com.intellij.patterns.PatternCondition
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.patterns.PlatformPatterns.string
import com.intellij.patterns.PlatformPatterns.virtualFile
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.psi.impl.source.resolve.reference.impl.providers.JavaClassReferenceProvider
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.ProcessingContext
import net.earthcomputer.multiconnectintellij.csv.psi.CsvEntry
import net.earthcomputer.multiconnectintellij.csv.psi.CsvIdentifier
import net.earthcomputer.multiconnectintellij.csv.psi.CsvStringValue

class CsvReferenceContributor : PsiReferenceContributor() {
    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(protocolClassPattern, object : JavaClassReferenceProvider() {
            override fun getScope(project: Project): GlobalSearchScope {
                return GlobalSearchScope.allScope(project)
            }
        })
    }

    companion object {
        val protocolClassPattern = psiElement(CsvStringValue::class.java)
            .inVirtualFile(virtualFile().withName(string().oneOf("spackets.csv", "cpackets.csv"))
                .withParent(virtualFile().withName(string().matches(CsvFileType.versionRegex.pattern))))
            // CsvEntry -> CsvIdentifier -> CsvStringValue
            .withSuperParent(2, psiElement(CsvEntry::class.java).with(object : PatternCondition<CsvEntry>("clazzKey") {
                override fun accepts(t: CsvEntry, context: ProcessingContext?): Boolean {
                    return t.key == "clazz"
                }
            }))
            .withParent(psiElement(CsvIdentifier::class.java).with(object : PatternCondition<CsvIdentifier>("emptyNamespace") {
                override fun accepts(t: CsvIdentifier, context: ProcessingContext?): Boolean {
                    return t.namespaceElement == null
                }
            }))
    }
}
