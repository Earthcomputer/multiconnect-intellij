package net.earthcomputer.multiconnectintellij

import com.intellij.openapi.util.TextRange
import com.intellij.openapi.util.text.StringUtil
import com.intellij.patterns.PsiJavaElementPattern
import com.intellij.patterns.PsiJavaPatterns.psiAnnotation
import com.intellij.patterns.PsiJavaPatterns.psiClass
import com.intellij.patterns.PsiJavaPatterns.psiLiteral
import com.intellij.patterns.PsiJavaPatterns.psiNameValuePair
import com.intellij.patterns.PsiNameValuePairPattern
import com.intellij.patterns.StandardPatterns.*
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiLiteral
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiModifier
import com.intellij.psi.PsiPolyVariantReference
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.PsiReferenceContributor
import com.intellij.psi.PsiReferenceProvider
import com.intellij.psi.PsiReferenceRegistrar
import com.intellij.psi.ResolveResult
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext

private fun PsiNameValuePairPattern.insideAnnotation(qName: String): PsiNameValuePairPattern {
    return inside(psiAnnotation().qName(qName))
}

class MulticonnectReferenceContributor : PsiReferenceContributor() {

    override fun registerReferenceProviders(registrar: PsiReferenceRegistrar) {
        registrar.registerReferenceProvider(
            StringReferenceProvider.ELEMENT_PATTERN,
            StringReferenceProvider
        )
    }
}

object StringReferenceProvider : PsiReferenceProvider() {
    val ELEMENT_PATTERN: PsiJavaElementPattern.Capture<PsiLiteral> =
        psiLiteral(string()).inside(psiClass().withAnnotation(Constants.MESSAGE_VARIANT))
            .inside(or(
                psiNameValuePair().withName("value").insideAnnotation(Constants.ARGUMENT),
                psiNameValuePair().withName("compute").insideAnnotation(Constants.DEFAULT_CONSTRUCT),
                psiNameValuePair().withName("compute").insideAnnotation(Constants.INTRODUCE),
                psiNameValuePair().withName("compute").insideAnnotation(Constants.LENGTH),
                psiNameValuePair().withName("value").insideAnnotation(Constants.ONLY_IF),
                psiNameValuePair().withName("condition").insideAnnotation(Constants.POLYMORPHIC),
                psiNameValuePair().withName("field").insideAnnotation(Constants.POLYMORPHIC_BY),
            ))

    override fun getReferencesByElement(
        element: PsiElement,
        context: ProcessingContext
    ): Array<PsiReference> {
        if (element !is PsiLiteral) {
            return PsiReference.EMPTY_ARRAY
        }
        val text = element.text
        if (text.length < 2) {
            return PsiReference.EMPTY_ARRAY
        }

        // create a separate reference for each part delimited by .
        val result = mutableListOf<PsiReference>()
        var endIndex = 0
        var startIndex: Int
        var parent: StringReference? = null
        do {
            startIndex = endIndex
            endIndex = text.indexOf('.', startIndex = endIndex + 1)
            if (endIndex == -1) {
                endIndex = text.length - 1
            }

            val range = TextRange(startIndex + 1, endIndex)
            val referenceName = StringUtil.unescapeStringCharacters(range.substring(text))
            val reference = StringReference(element, range, referenceName, parent, endIndex == text.length - 1)

            result += reference
            parent = reference
        } while (endIndex != text.length - 1)

        return result.toTypedArray()
    }
}

class StringReference(
    literal: PsiLiteral,
    range: TextRange,
    private val referenceName: String,
    private val parent: StringReference?,
    private val isLast: Boolean
) : PsiReferenceBase<PsiLiteral>(literal, range), PsiPolyVariantReference {
    override fun resolve(): PsiElement? {
        return this.multiResolve(false).singleOrNull()?.element
    }

    override fun multiResolve(incompleteCode: Boolean): Array<ResolveResult> {
        return doResolve().distinctBy { it.element }.map { PsiElementResolveResult(it.element) }.toTypedArray()
    }

    private fun doResolve(): List<Result> {
        val element = element
        val annotationName = element.parentOfType<PsiAnnotation>()?.qualifiedName ?: return emptyList()
        val clazz = element.parentOfType<PsiClass>() ?: return emptyList()
        return when (annotationName) {
            Constants.ARGUMENT -> {
                val method = element.parentOfType<PsiMethod>() ?: return emptyList()
                ReferencesSearch.search(method).filterIsInstance<StringReference>().flatMap { reference ->
                    val refElt = reference.element
                    val refAnnName = refElt.parentOfType<PsiAnnotation>()?.qualifiedName ?: return@flatMap emptyList()
                    val contextClasses = when {
                        parent != null -> parent.doResolve().mapNotNull { result ->
                            (result.element as? PsiClass)?.let { VariantInfo(result.versionRange, it) }
                        }
                        refAnnName == Constants.INTRODUCE -> emptyList() // TODO
                        else -> listOfNotNull(refElt.parentOfType<PsiClass>()?.let { VariantInfo(getVersionRange(it), it) })
                    }
                    if (isLast) {
                        contextClasses.mapNotNull { contextClass ->
                            contextClass.clazz.findFieldByName(referenceName, true)?.let { Result(it, contextClass.versions) }
                        }
                    } else {
                        if (referenceName != "outer") {
                            return@flatMap emptyList()
                        }
                        contextClasses.flatMap { contextClass ->
                            findVariantUsages(contextClass.clazz).mapNotNull { field ->
                                val usage = field.parentOfType<PsiClass>() ?: return@mapNotNull null
                                val range = contextClass.versions.intersectRange(getVersionRange(usage))
                                if (range.isEmpty()) {
                                    return@mapNotNull null
                                }
                                Result(usage, range)
                            }
                        }.distinct()
                    }
                }
            }
            Constants.POLYMORPHIC_BY -> {
                if (!isLast) {
                    return emptyList()
                }
                listOfNotNull(clazz.findFieldByName(referenceName, true)
                    ?.let { Result(it, getVersionRange(clazz)) })
            }
            else -> {
                if (!isLast) {
                    return emptyList()
                }
                clazz.findMethodsByName(referenceName, true)
                    .filter { it.hasModifierProperty(PsiModifier.STATIC) }
                    .map { Result(it, getVersionRange(clazz)) }
            }
        }
    }

    override fun getVariants(): Array<Any> {
        val element = element
        val annotationName = element.parentOfType<PsiAnnotation>()?.qualifiedName ?: return emptyArray()
        val clazz = element.parentOfType<PsiClass>() ?: return emptyArray()

        return when (annotationName) {
            Constants.ARGUMENT -> emptyArray() // TODO
            Constants.POLYMORPHIC_BY -> clazz.allFields
                .filter { !it.hasModifierProperty(PsiModifier.STATIC) }
                .toTypedArray()
            else -> clazz.allMethods
                .filter { it.hasModifierProperty(PsiModifier.STATIC) }
                .toTypedArray()
        }
    }

    data class Result(val element: PsiElement, val versionRange: IntRange)
}
