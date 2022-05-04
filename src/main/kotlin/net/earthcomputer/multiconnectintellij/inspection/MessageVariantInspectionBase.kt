package net.earthcomputer.multiconnectintellij.inspection

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiField
import com.intellij.psi.PsiMethod
import net.earthcomputer.multiconnectintellij.Constants

abstract class MessageVariantInspectionBase : AbstractBaseJavaLocalInspectionTool() {
    protected open fun doCheckMethod(
        method: PsiMethod,
        manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor>? {
        return null
    }

    final override fun checkMethod(
        method: PsiMethod,
        manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor>? {
        return if (method.containingClass?.hasAnnotation(Constants.MESSAGE_VARIANT) == true) {
            doCheckMethod(method, manager, isOnTheFly)
        } else {
            null
        }
    }

    protected open fun doCheckClass(
        clazz: PsiClass,
        manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor>? {
        return null
    }

    final override fun checkClass(
        clazz: PsiClass,
        manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor>? {
        return if (clazz.hasAnnotation(Constants.MESSAGE_VARIANT)) {
            doCheckClass(clazz, manager, isOnTheFly)
        } else {
            null
        }
    }

    protected open fun doCheckField(
        field: PsiField,
        manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor>? {
        return null
    }

    final override fun checkField(
        field: PsiField,
        manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor>? {
        return if (field.containingClass?.hasAnnotation(Constants.MESSAGE_VARIANT) == true) {
            return doCheckField(field, manager, isOnTheFly)
        } else {
            null
        }
    }
}
