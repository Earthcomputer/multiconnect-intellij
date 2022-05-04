package net.earthcomputer.multiconnectintellij.inspection

import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool
import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiEnumConstant
import net.earthcomputer.multiconnectintellij.Constants

class NetworkEnumInspection : AbstractBaseJavaLocalInspectionTool() {
    override fun getStaticDescription() = "Checks the validity of network enums"

    override fun checkClass(
        clazz: PsiClass,
        manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor>? {
        val annotation = clazz.getAnnotation(Constants.NETWORK_ENUM) ?: return null
        if (!clazz.isEnum) {
            return manager.createProblem(
                annotation.nameReferenceElement ?: return null,
                isOnTheFly,
                "@NetworkEnum must only be used on enums"
            )
        }

        if (clazz.fields.none { it is PsiEnumConstant }) {
            return manager.createProblem(
                clazz.nameIdentifier ?: return null,
                isOnTheFly,
                "Network enum cannot be empty"
            )
        }

        return null
    }
}
