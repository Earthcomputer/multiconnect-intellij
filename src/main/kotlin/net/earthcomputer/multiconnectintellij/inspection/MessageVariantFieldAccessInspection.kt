package net.earthcomputer.multiconnectintellij.inspection

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.psi.JavaTokenType
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiModifier
import com.intellij.psi.tree.IElementType

class MessageVariantFieldAccessInspection : MessageVariantInspectionBase() {
    override fun getStaticDescription() = "Detects invalid message variant field modifiers"

    override fun doCheckField(
        field: PsiField,
        manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor>? {
        if (field.hasModifierProperty(PsiModifier.STATIC)) {
            return null
        }

        if (field.hasModifierProperty(PsiModifier.FINAL)) {
            return manager.createProblem(
                field.findModifierElement(JavaTokenType.FINAL_KEYWORD) ?: field.nameIdentifier,
                isOnTheFly,
                "Message variant field cannot be final"
            )
        }

        if (!field.hasModifierProperty(PsiModifier.PUBLIC)) {
            return manager.createProblem(
                field.findModifierElement(JavaTokenType.PROTECTED_KEYWORD, JavaTokenType.PRIVATE_KEYWORD) ?: field.nameIdentifier,
                isOnTheFly,
                "Message variant field must be public"
            )
        }

        return null
    }

    private fun PsiField.findModifierElement(vararg tokenTypes: IElementType): PsiElement? {
        return modifierList?.children?.firstOrNull { it.node.elementType in tokenTypes }
    }
}
