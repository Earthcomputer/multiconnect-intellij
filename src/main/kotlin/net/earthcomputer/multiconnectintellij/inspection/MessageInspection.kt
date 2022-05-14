package net.earthcomputer.multiconnectintellij.inspection

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.psi.PsiAnonymousClass
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiModifier
import com.intellij.psi.search.searches.ClassInheritorsSearch
import net.earthcomputer.multiconnectintellij.Constants

class MessageInspection : AbstractBaseJavaLocalInspectionTool() {
    override fun getStaticDescription() = "Reports problems with @Message interfaces"

    override fun checkClass(
        clazz: PsiClass,
        manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor>? {
        if (!clazz.hasAnnotation(Constants.MESSAGE)) {
            return null
        }

        if (!clazz.isInterface || clazz.isAnnotationType) {
            return manager.createProblem(
                (clazz as? PsiAnonymousClass)?.baseClassReference ?: clazz.nameIdentifier ?: return null,
                isOnTheFly,
                "@Message classes must be interfaces"
            )
        }

        if (!clazz.hasModifierProperty(PsiModifier.PUBLIC)) {
            return manager.createProblem(
                clazz.nameIdentifier ?: return null,
                isOnTheFly,
                "@Message must be public"
            )
        }

        if (!ClassInheritorsSearch.search(clazz, false).anyMatch { it.hasAnnotation(Constants.MESSAGE_VARIANT) }) {
            return manager.createProblem(
                clazz.nameIdentifier ?: return null,
                isOnTheFly,
                "@Message has no variants"
            )
        }

        return null
    }
}
