package net.earthcomputer.multiconnectintellij

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.search.searches.ClassInheritorsSearch

class MessageVariantLineMarkerProvider : RelatedItemLineMarkerProvider() {
    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        if (element !is PsiIdentifier) {
            return
        }
        val clazz = element.parent as? PsiClass ?: return
        if (!clazz.hasAnnotation(Constants.MESSAGE_VARIANT)) {
            return
        }

        val group = clazz.interfaces.singleOrNull() ?: return
        if (group.hasAnnotation(Constants.MESSAGE)) {
            val targets = ClassInheritorsSearch.search(group)
                .filter { !clazz.isEquivalentTo(it) }
                .mapNotNull { it.nameIdentifier }
                .toList()
            result += NavigationGutterIconBuilder.create(Icons.assocFile)
                .setTargets(targets)
                .setTooltipText("Navigate to other variants")
                .createLineMarkerInfo(element)
        }
    }
}
