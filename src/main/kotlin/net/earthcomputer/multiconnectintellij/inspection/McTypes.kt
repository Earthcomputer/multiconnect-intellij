package net.earthcomputer.multiconnectintellij.inspection

import com.intellij.psi.CommonClassNames.JAVA_UTIL_LIST
import com.intellij.psi.CommonClassNames.JAVA_UTIL_OPTIONAL
import com.intellij.psi.PsiArrayType
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiType
import net.earthcomputer.multiconnectintellij.Constants.FASTUTIL_INT_LIST
import net.earthcomputer.multiconnectintellij.Constants.FASTUTIL_LONG_LIST
import net.earthcomputer.multiconnectintellij.Constants.JAVA_UTIL_OPTIONAL_INT
import net.earthcomputer.multiconnectintellij.Constants.JAVA_UTIL_OPTIONAL_LONG

object McTypes {
    fun getComponentType(type: PsiType): PsiType? {
        return when (type) {
            is PsiArrayType -> type.componentType
            is PsiClassType -> {
                val clazz = type.resolve() ?: return null
                when (clazz.qualifiedName) {
                    JAVA_UTIL_LIST, JAVA_UTIL_OPTIONAL -> type.parameters.singleOrNull()
                    FASTUTIL_INT_LIST, JAVA_UTIL_OPTIONAL_INT -> PsiType.INT
                    FASTUTIL_LONG_LIST, JAVA_UTIL_OPTIONAL_LONG -> PsiType.LONG
                    else -> null
                }
            }
            else -> null
        }
    }

    fun isOptional(type: PsiType): Boolean {
        if (type !is PsiClassType) {
            return false
        }
        val qName = type.resolve()?.qualifiedName ?: return false
        return qName == JAVA_UTIL_OPTIONAL || qName == JAVA_UTIL_OPTIONAL_INT || qName == JAVA_UTIL_OPTIONAL_LONG
    }

    fun hasLength(type: PsiType): Boolean {
        return getComponentType(type) != null && !isOptional(type)
    }
}
