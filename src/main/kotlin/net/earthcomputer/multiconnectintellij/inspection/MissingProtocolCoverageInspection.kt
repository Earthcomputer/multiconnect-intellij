package net.earthcomputer.multiconnectintellij.inspection

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.psi.PsiClass
import net.earthcomputer.multiconnectintellij.Constants
import net.earthcomputer.multiconnectintellij.getProtocolName
import net.earthcomputer.multiconnectintellij.getVariantProviderPossiblyInvalid
import net.earthcomputer.multiconnectintellij.getVersionRange
import net.earthcomputer.multiconnectintellij.intersectRange
import net.earthcomputer.multiconnectintellij.protocolVersions

class MissingProtocolCoverageInspection : MessageVariantInspectionBase() {
    override fun getStaticDescription() = "Reports missing protocol coverage of messages"

    override fun doCheckClass(
        clazz: PsiClass,
        manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor>? {
        val annotation = clazz.getAnnotation(Constants.MESSAGE_VARIANT) ?: return null

        val versionRange = getVersionRange(clazz)
        val allProtocols = clazz.project.protocolVersions

        var versionRangeProblems = ProblemDescriptor.EMPTY_ARRAY
        if (versionRange.first != Int.MIN_VALUE && allProtocols.binarySearch(versionRange.first) < 0) {
            versionRangeProblems += manager.createProblem(
                annotation.findDeclaredAttributeValue("minVersion") ?: clazz.nameIdentifier ?: return null,
                isOnTheFly,
                "minVersion is not in the protocol list"
            )
        }
        if (versionRange.last != Int.MAX_VALUE && allProtocols.binarySearch(versionRange.last) < 0) {
            versionRangeProblems += manager.createProblem(
                annotation.findDeclaredAttributeValue("maxVersion") ?: clazz.nameIdentifier ?: return null,
                isOnTheFly,
                "maxVersion is not in the protocol list"
            )
        }
        if (versionRangeProblems.isNotEmpty()) {
            return versionRangeProblems
        }

        val group = clazz.interfaces.singleOrNull()?.takeIf { it.hasAnnotation(Constants.MESSAGE) }

        if (group == null) {
            if (versionRange.first != Int.MIN_VALUE || versionRange.last != Int.MAX_VALUE) {
                return manager.createProblem(
                    annotation.findDeclaredAttributeValue("minVersion") ?: annotation.findDeclaredAttributeValue("maxVersion") ?: clazz.nameIdentifier ?: return null,
                    isOnTheFly,
                    "Message variant cannot restrict its protocol range if it's not in a group"
                )
            }
        } else {
            val variantProvider = getVariantProviderPossiblyInvalid(clazz) ?: return null
            if (!variantProvider.isValid()) {
                if (variantProvider.any { it.clazz != clazz && !it.versions.intersectRange(versionRange).isEmpty() }) {
                    return manager.createProblem(
                        clazz.nameIdentifier ?: return null,
                        isOnTheFly,
                        "Message variant overlaps protocol with another variant"
                    )
                }
            } else {
                if (versionRange.first != Int.MIN_VALUE) {
                    val lastProtocol = allProtocols.getOrNull(allProtocols.binarySearch(versionRange.first) - 1)
                    val hasLastProtocol = lastProtocol != null && variantProvider.getVariant(lastProtocol) != null
                    if (!hasLastProtocol) {
                        versionRangeProblems += manager.createProblem(
                            annotation.findDeclaredAttributeValue("minVersion") ?: clazz.nameIdentifier ?: return null,
                            isOnTheFly,
                            if (lastProtocol == null) {
                                "This minVersion declaration is not allowed because there are no older variants"
                            } else {
                                "This minVersion declaration prevents protocol ${clazz.project.getProtocolName(lastProtocol) ?: lastProtocol.toString()} from being handled"
                            }
                        )
                    }
                }
                if (versionRange.last != Int.MAX_VALUE) {
                    val nextProtocol = allProtocols.getOrNull(allProtocols.binarySearch(versionRange.last) + 1)
                    val hasNextProtocol = nextProtocol != null && variantProvider.getVariant(nextProtocol) != null
                    if (!hasNextProtocol) {
                        versionRangeProblems += manager.createProblem(
                            annotation.findDeclaredAttributeValue("maxVersion") ?: clazz.nameIdentifier ?: return null,
                            isOnTheFly,
                            if (nextProtocol == null) {
                                "This maxVersion declaration is not allowed because there are no newer variants"
                            } else {
                                "This maxVersion declaration prevents protocol ${clazz.project.getProtocolName(nextProtocol) ?: nextProtocol.toString()} from being handled"
                            }
                        )
                    }
                }
                if (versionRangeProblems.isNotEmpty()) {
                    return versionRangeProblems
                }
            }
        }

        return null
    }
}
