@file:Suppress("UnstableApiUsage")

package net.earthcomputer.multiconnectintellij

import com.intellij.codeInsight.hints.ChangeListener
import com.intellij.codeInsight.hints.FactoryInlayHintsCollector
import com.intellij.codeInsight.hints.ImmediateConfigurable
import com.intellij.codeInsight.hints.InlayHintsCollector
import com.intellij.codeInsight.hints.InlayHintsProvider
import com.intellij.codeInsight.hints.InlayHintsSink
import com.intellij.codeInsight.hints.NoSettings
import com.intellij.codeInsight.hints.SettingsKey
import com.intellij.codeInsight.hints.presentation.SequencePresentation
import com.intellij.openapi.editor.BlockInlayPriority
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.psi.CommonClassNames.JAVA_LANG_STRING
import com.intellij.psi.CommonClassNames.JAVA_UTIL_LIST
import com.intellij.psi.CommonClassNames.JAVA_UTIL_OPTIONAL
import com.intellij.psi.PsiArrayType
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiClassType
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiField
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiModifier
import com.intellij.psi.PsiPrimitiveType
import com.intellij.psi.PsiType
import com.intellij.psi.PsiWhiteSpace
import net.earthcomputer.multiconnectintellij.Constants.FASTUTIL_INT_LIST
import net.earthcomputer.multiconnectintellij.Constants.FASTUTIL_LONG_LIST
import net.earthcomputer.multiconnectintellij.Constants.JAVA_UTIL_BIT_SET
import net.earthcomputer.multiconnectintellij.Constants.JAVA_UTIL_OPTIONAL_INT
import net.earthcomputer.multiconnectintellij.Constants.JAVA_UTIL_OPTIONAL_LONG
import net.earthcomputer.multiconnectintellij.Constants.JAVA_UTIL_UUID
import net.earthcomputer.multiconnectintellij.Constants.MINECRAFT_RESOURCE_LOCATION
import net.earthcomputer.multiconnectintellij.Constants.MINECRAFT_COMPOUND_TAG
import org.intellij.lang.annotations.Language
import javax.swing.JComponent
import javax.swing.JPanel

class MulticonnectInlayHintsProvider : InlayHintsProvider<NoSettings> {
    companion object {
        private val settingsKey = SettingsKey<NoSettings>("multiconnect")
    }

    override val key = settingsKey
    override val name = "Multiconnect"
    @Language("JAVA")
    override val previewText = """import net.earthcomputer.multiconnect.ap.MessageVariant;

@MessageVariant
public class Message {
    public int field;
}
"""

    override fun createSettings() = NoSettings()

    override fun getCollectorFor(
        file: PsiFile,
        editor: Editor,
        settings: NoSettings,
        sink: InlayHintsSink
    ): InlayHintsCollector? {
        val document = PsiDocumentManager.getInstance(file.project).getDocument(file) ?: return null
        return MulticonnectInlayHintsCollector(document, editor)
    }

    override fun createConfigurable(settings: NoSettings) = object : ImmediateConfigurable {
        override fun createComponent(listener: ChangeListener): JComponent {
            return JPanel()
        }
    }
}

private class MulticonnectInlayHintsCollector(
    private val document: Document,
    editor: Editor
) : FactoryInlayHintsCollector(editor) {
    override fun collect(element: PsiElement, editor: Editor, sink: InlayHintsSink): Boolean {
        if (element is PsiField) {
            if (!element.hasModifierProperty(PsiModifier.STATIC)
                && element.containingClass?.hasAnnotation(Constants.MESSAGE_VARIANT) == true
                && !element.hasAnnotation(Constants.TYPE)
            ) {
                val type = getInferredWireType(element.type)
                if (type != null) {
                    sink.addHint(element, "@Type(Types.$type)")
                }
            }
        }

        if (element is PsiClass) {
            if (element.hasAnnotation(Constants.MESSAGE_VARIANT) || element.hasAnnotation(Constants.MESSAGE)) {
                val direction = getPacketDirection(element)?.name?.lowercase()?.replaceFirstChar { it.uppercaseChar() }
                    ?: "Unknown"
                sink.addHint(element, "Packet Direction: $direction")
            }
        }

        return true
    }

    private fun InlayHintsSink.addHint(element: PsiElement, text: String) {
        val offset = (element.children.firstOrNull {
            it !is PsiComment && it !is PsiWhiteSpace
        } ?: element).textRange.startOffset
        val line = document.getLineNumber(offset)
        val startOffset = document.getLineStartOffset(line)
        val column = offset - startOffset
        val shiftedPresentation = SequencePresentation(listOf(
            factory.textSpacePlaceholder(column, true),
            factory.smallText(text)
        ))
        this.addBlockElement(
            startOffset,
            relatesToPrecedingText = true,
            showAbove = true,
            priority = BlockInlayPriority.ANNOTATIONS,
            presentation = shiftedPresentation
        )
    }
}

private fun getInferredWireType(type: PsiType): String? {
    return when (type) {
        is PsiPrimitiveType -> when (type) {
            PsiType.BOOLEAN -> "BOOLEAN"
            PsiType.BYTE -> "BYTE"
            PsiType.DOUBLE -> "DOUBLE"
            PsiType.INT -> "VAR_INT"
            PsiType.FLOAT -> "FLOAT"
            PsiType.LONG -> "VAR_LONG"
            PsiType.SHORT -> "SHORT"
            else -> null
        }
        is PsiArrayType -> getInferredWireType(type.deepComponentType)
        is PsiClassType -> {
            val clazz = type.resolve() ?: return null
            when (clazz.qualifiedName) {
                JAVA_UTIL_LIST, JAVA_UTIL_OPTIONAL -> getInferredWireType(type.parameters.singleOrNull() ?: return null)
                FASTUTIL_INT_LIST, JAVA_UTIL_OPTIONAL_INT -> "VAR_INT"
                FASTUTIL_LONG_LIST, JAVA_UTIL_OPTIONAL_LONG -> "VAR_LONG"
                JAVA_UTIL_BIT_SET -> "BITSET"
                MINECRAFT_RESOURCE_LOCATION -> "RESOURCE_LOCATION"
                MINECRAFT_COMPOUND_TAG -> "COMPOUND_TAG"
                JAVA_LANG_STRING -> "STRING"
                JAVA_UTIL_UUID -> "UUID"
                else -> if (clazz.hasAnnotation(Constants.NETWORK_ENUM)) {
                    "VAR_INT"
                } else {
                    null // don't hint message types, that's annoying
                }
            }
        }
        else -> null
    }
}
