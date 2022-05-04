package net.earthcomputer.multiconnectintellij

import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.search.UseScopeEnlarger
import net.earthcomputer.multiconnectintellij.csv.CsvFileType

class MulticonnectUseScopeEnlarger : UseScopeEnlarger() {
    override fun getAdditionalUseScope(element: PsiElement): SearchScope? {
        // can't use isPacket() here because that looks for usages and leads to infinite recursion,
        // so we use this heuristic instead.
        if (element is PsiClass && element.hasAnnotation(Constants.MESSAGE_VARIANT)) {
            return GlobalSearchScope.getScopeRestrictedByFileTypes(
                GlobalSearchScope.allScope(element.project),
                CsvFileType
            )
        }

        return null
    }
}
