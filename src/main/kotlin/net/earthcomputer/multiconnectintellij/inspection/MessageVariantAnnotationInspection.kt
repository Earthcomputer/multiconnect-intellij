package net.earthcomputer.multiconnectintellij.inspection

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.JavaElementVisitor
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.util.parentOfType
import net.earthcomputer.multiconnectintellij.Constants

abstract class MessageVariantAnnotationInspection(
    private val annotation: String
) : MessageVariantInspectionBase() {
    protected abstract fun checkAnnotation(
        clazz: PsiClass,
        annotation: PsiAnnotation,
        manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor>?

    final override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor = Visitor(holder, isOnTheFly)

    private inner class Visitor(private val holder: ProblemsHolder, private val isOnTheFly: Boolean) : JavaElementVisitor() {
        override fun visitAnnotation(annotation: PsiAnnotation) {
            if (!annotation.hasQualifiedName(this@MessageVariantAnnotationInspection.annotation)) {
                return
            }

            val clazz = annotation.parentOfType<PsiClass>() ?: return
            if (!clazz.hasAnnotation(Constants.MESSAGE_VARIANT)) {
                return
            }

            val problems = checkAnnotation(clazz, annotation, holder.manager, isOnTheFly) ?: return
            for (problem in problems) {
                holder.registerProblem(problem)
            }
        }
    }
}
