package net.earthcomputer.multiconnectintellij.inspection

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiField
import com.intellij.psi.util.parentOfType
import net.earthcomputer.multiconnectintellij.Constants

class DatafixInspection : MessageVariantAnnotationInspection(Constants.DATAFIX) {
    override fun getStaticDescription() = "Reports @Datafix problems"

    override fun checkAnnotation(
        clazz: PsiClass,
        annotation: PsiAnnotation,
        manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor>? {
        val field = annotation.parentOfType<PsiField>() ?: return null
        val fieldType = McTypes.getDeepComponentType(field.type)
        if ((fieldType as? PsiClassType)?.resolve()?.qualifiedName != Constants.MINECRAFT_NBT_COMPOUND) {
            return manager.createProblem(
                field.typeElement ?: annotation.nameReferenceElement ?: return null,
                isOnTheFly,
                "@Datafix can only be used on NbtCompounds"
            )
        }
        return null
    }
}
