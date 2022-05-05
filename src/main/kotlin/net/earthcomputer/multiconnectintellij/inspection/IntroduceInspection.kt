package net.earthcomputer.multiconnectintellij.inspection

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiField
import com.intellij.psi.PsiModifier
import com.intellij.psi.util.parentOfType
import net.earthcomputer.multiconnectintellij.Constants
import net.earthcomputer.multiconnectintellij.PacketDirection
import net.earthcomputer.multiconnectintellij.getEnumConstant
import net.earthcomputer.multiconnectintellij.getPacketDirection
import net.earthcomputer.multiconnectintellij.getString

class IntroduceInspection : MessageVariantAnnotationInspection(Constants.INTRODUCE) {
    override fun getStaticDescription() = "Reports @Introduce problems"

    override fun checkAnnotation(
        clazz: PsiClass,
        annotation: PsiAnnotation,
        manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor>? {
        checkCompute(clazz, annotation, manager, isOnTheFly)?.let { return it }
        checkDirection(clazz, annotation, manager, isOnTheFly)?.let { return it }
        checkDuplicateDirection(annotation, manager, isOnTheFly)?.let { return it }

        return null
    }

    private fun checkCompute(
        clazz: PsiClass,
        annotation: PsiAnnotation,
        manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor>? {
        val field = annotation.parentOfType<PsiField>() ?: return null
        val compute = annotation.getString("compute").ifEmpty { return null }
        val func = clazz.findMethodsByName(compute, true)
            .singleOrNull { it.hasModifierProperty(PsiModifier.STATIC) } ?: return null
        val returnType = func.returnType ?: return null
        if (returnType != field.type) {
            return manager.createProblem(
                annotation.findDeclaredAttributeValue("compute") ?: annotation.nameReferenceElement ?: return null,
                isOnTheFly,
                "Compute function must return the same type as the field"
            )
        }

        return null
    }

    private fun checkDirection(
        clazz: PsiClass,
        annotation: PsiAnnotation,
        manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor>? {
        val direction = when (annotation.getEnumConstant("direction")?.name) {
            "FROM_NEWER" -> PacketDirection.SERVERBOUND
            "FROM_OLDER" -> PacketDirection.CLIENTBOUND
            else -> PacketDirection.BOTH
        }
        val packetDirection = getPacketDirection(clazz) ?: return null
        if (direction == PacketDirection.BOTH) {
            if (packetDirection == PacketDirection.BOTH) {
                return manager.createProblem(
                    annotation.findDeclaredAttributeValue("direction") ?: annotation.nameReferenceElement ?: return null,
                    isOnTheFly,
                    "@Introduce direction cannot be AUTO if the packet goes in both directions"
                )
            } else {
                return null
            }
        }
        if (packetDirection != PacketDirection.BOTH && packetDirection != direction) {
            return manager.createProblem(
                annotation.findDeclaredAttributeValue("direction") ?: annotation.nameReferenceElement ?: return null,
                isOnTheFly,
                "Incorrect packet direction"
            )
        }

        return null
    }

    private fun checkDuplicateDirection(
        annotation: PsiAnnotation,
        manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor>? {
        val field = annotation.parentOfType<PsiField>() ?: return null
        val dir = annotation.getEnumConstant("direction")?.name ?: "AUTO"
        val otherIntroduces = field.annotations.filter {
            !it.isEquivalentTo(annotation) && it.hasQualifiedName(Constants.INTRODUCE)
        }
        if (dir == "AUTO" && otherIntroduces.isNotEmpty() || otherIntroduces.any { it.getEnumConstant("direction")?.name == dir }) {
            return manager.createProblem(
                annotation.findDeclaredAttributeValue("direction") ?: annotation.nameReferenceElement ?: return null,
                isOnTheFly,
                "@Introduce has duplicate direction on the same field"
            )
        }

        return null
    }
}
