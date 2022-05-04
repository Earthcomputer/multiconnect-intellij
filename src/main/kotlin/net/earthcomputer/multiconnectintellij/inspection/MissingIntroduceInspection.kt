package net.earthcomputer.multiconnectintellij.inspection

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiField
import com.intellij.psi.PsiModifier
import com.intellij.psi.PsiPrimitiveType
import com.intellij.psi.PsiType
import net.earthcomputer.multiconnectintellij.Constants
import net.earthcomputer.multiconnectintellij.PacketDirection
import net.earthcomputer.multiconnectintellij.VariantProvider
import net.earthcomputer.multiconnectintellij.getGroup
import net.earthcomputer.multiconnectintellij.getPacketDirection
import net.earthcomputer.multiconnectintellij.getVariantProvider
import net.earthcomputer.multiconnectintellij.getVersionRange
import net.earthcomputer.multiconnectintellij.protocolVersions

class MissingIntroduceInspection : MessageVariantInspectionBase() {
    override fun getStaticDescription() = "Message variant field missing @Introduce, automatic transfer not possible"

    override fun doCheckField(
        field: PsiField,
        manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor>? {
        if (field.hasModifierProperty(PsiModifier.STATIC)) {
            return null
        }
        if (field.hasAnnotation(Constants.INTRODUCE)) {
            return null
        }

        val clazz = field.containingClass ?: return null
        val variantProvider = getVariantProvider(clazz) ?: return null
        if (variantProvider.isSingleVariant) {
            return null
        }

        val directions = getPacketDirection(clazz) ?: return null
        val range = getVersionRange(clazz)
        val allProtocols = clazz.project.protocolVersions

        var result = ProblemDescriptor.EMPTY_ARRAY

        if (directions != PacketDirection.CLIENTBOUND && range.last != Int.MAX_VALUE) {
            val lastProtocol = allProtocols.getOrNull(allProtocols.binarySearch(range.last) + 1)
            result += handleField(field, manager, isOnTheFly, lastProtocol, variantProvider)
        }
        if (directions != PacketDirection.SERVERBOUND && range.first != Int.MIN_VALUE) {
            val lastProtocol = allProtocols.getOrNull(allProtocols.binarySearch(range.first) - 1)
            result += handleField(field, manager, isOnTheFly, lastProtocol, variantProvider)
        }

        return result
    }

    private fun handleField(
        field: PsiField,
        manager: InspectionManager,
        isOnTheFly: Boolean,
        lastProtocol: Int?,
        variantProvider: VariantProvider
    ): Array<ProblemDescriptor> {
        if (lastProtocol == null) {
            return ProblemDescriptor.EMPTY_ARRAY
        }

        val fromClass = variantProvider.getVariant(lastProtocol)?.clazz ?: return ProblemDescriptor.EMPTY_ARRAY
        val fromField = fromClass.findFieldByName(field.name, true)
            ?: return manager.createProblem(field.nameIdentifier, isOnTheFly, "Message variant field must either come from automatic transfer or @Introduce")

        val type = field.type
        val fromType = fromField.type
        if (isAutoTransferable(type, fromType)) {
            return ProblemDescriptor.EMPTY_ARRAY
        }

        return manager.createProblem(field.typeElement ?: field.nameIdentifier, isOnTheFly, "Message variant type is not auto-transferable")
    }

    private fun isAutoTransferable(a: PsiType, b: PsiType): Boolean {
        if (a == b) {
            return true
        }
        if (a is PsiPrimitiveType && b is PsiPrimitiveType) {
            return true
        }

        val componentTypeA = McTypes.getComponentType(a)
        val componentTypeB = McTypes.getComponentType(b)
        if (componentTypeA != null && componentTypeB != null) {
            if (McTypes.isOptional(a) == McTypes.isOptional(b) && isAutoTransferable(componentTypeA, componentTypeB)) {
                return true
            }
        }

        if (a is PsiClassType && b is PsiClassType) {
            val aClass = a.resolve()
            val bClass = b.resolve()
            if (aClass != null && bClass != null) {
                val aGroup = getGroup(aClass)
                val bGroup = getGroup(bClass)
                if (aGroup != null && aGroup == bGroup) {
                    return true
                }
            }
        }

        return false
    }
}
