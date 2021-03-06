package net.earthcomputer.multiconnectintellij.inspection

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.psi.PsiElement

fun InspectionManager.createProblem(
    element: PsiElement,
    isOnTheFly: Boolean,
    description: String,
    quickFix: LocalQuickFix? = null,
): Array<ProblemDescriptor> {
    return arrayOf(createProblemDescriptor(
        element,
        description,
        quickFix,
        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
        isOnTheFly
    ))
}
