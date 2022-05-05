package net.earthcomputer.multiconnectintellij.inspection

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.psi.CommonClassNames.JAVA_LANG_STRING
import com.intellij.psi.CommonClassNames.JAVA_UTIL_LIST
import com.intellij.psi.CommonClassNames.JAVA_UTIL_OPTIONAL
import com.intellij.psi.PsiArrayType
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiField
import com.intellij.psi.PsiModifier
import com.intellij.psi.PsiPrimitiveType
import com.intellij.psi.PsiType
import net.earthcomputer.multiconnectintellij.Constants.FASTUTIL_INT_LIST
import net.earthcomputer.multiconnectintellij.Constants.FASTUTIL_LONG_LIST
import net.earthcomputer.multiconnectintellij.Constants.JAVA_UTIL_BIT_SET
import net.earthcomputer.multiconnectintellij.Constants.JAVA_UTIL_OPTIONAL_INT
import net.earthcomputer.multiconnectintellij.Constants.JAVA_UTIL_OPTIONAL_LONG
import net.earthcomputer.multiconnectintellij.Constants.JAVA_UTIL_UUID
import net.earthcomputer.multiconnectintellij.Constants.MESSAGE
import net.earthcomputer.multiconnectintellij.Constants.MESSAGE_VARIANT
import net.earthcomputer.multiconnectintellij.Constants.MINECRAFT_IDENTIFIER
import net.earthcomputer.multiconnectintellij.Constants.MINECRAFT_NBT_COMPOUND
import net.earthcomputer.multiconnectintellij.Constants.NETWORK_ENUM

class InvalidMessageFieldTypeInspection : MessageVariantInspectionBase() {
    override fun getStaticDescription() = "Checks fields in @MessageVariant classes have valid types"

    override fun doCheckField(
        field: PsiField,
        manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor>? {
        if (field.hasModifierProperty(PsiModifier.STATIC)) {
            return null
        }

        val typeElement = field.typeElement ?: return null
        if (!isValidType(field.type)) {
            return manager.createProblem(typeElement, isOnTheFly, "Invalid message variant field type")
        }
        return null
    }

    companion object {
        fun isValidType(type: PsiType): Boolean {
            return when (type) {
                is PsiPrimitiveType -> type != PsiType.CHAR
                is PsiArrayType -> isValidType(type.deepComponentType)
                is PsiClassType -> isValidClassType(type)
                else -> false
            }
        }

        private fun isValidClassType(type: PsiClassType): Boolean {
            val clazz = type.resolve() ?: return true // unresolved will produce a different error
            val qName = clazz.qualifiedName ?: return true

            return when (qName) {
                JAVA_LANG_STRING,
                JAVA_UTIL_UUID,
                MINECRAFT_IDENTIFIER,
                MINECRAFT_NBT_COMPOUND,
                JAVA_UTIL_OPTIONAL_INT,
                JAVA_UTIL_OPTIONAL_LONG,
                FASTUTIL_INT_LIST,
                FASTUTIL_LONG_LIST,
                JAVA_UTIL_BIT_SET -> true
                JAVA_UTIL_LIST,
                JAVA_UTIL_OPTIONAL -> isValidType(type.parameters.singleOrNull() ?: return false)
                else -> {
                    return clazz.hasAnnotation(MESSAGE_VARIANT)
                            || clazz.hasAnnotation(MESSAGE)
                            || clazz.hasAnnotation(NETWORK_ENUM)
                }
            }
        }
    }
}
