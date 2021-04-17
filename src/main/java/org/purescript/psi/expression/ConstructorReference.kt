package org.purescript.psi.expression

import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.LocalQuickFixProvider
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.PsiReferenceBase
import org.purescript.file.ExportedConstructorsIndex
import org.purescript.psi.PSPsiElement
import org.purescript.psi.name.PSQualifiedProperName

class ConstructorReference(
    element: PSPsiElement,
    val qualifiedProperName: PSQualifiedProperName
) :
    LocalQuickFixProvider,
    PsiReferenceBase<PSPsiElement>(
        element,
        qualifiedProperName.properName.textRangeInParent,
        false
    ) {

    override fun getVariants(): Array<Any> =
        candidates.toList().toTypedArray()

    override fun resolve(): PsiNamedElement? =
        candidates.firstOrNull { it.name == element.name }

    private val candidates: Sequence<PsiNamedElement>
        get() {
            val module = element.module ?: return emptySequence()
            val qualifyingName = qualifiedProperName.moduleName?.name
            if (qualifyingName != null) {
                val importDeclaration = module.importDeclarations
                    .firstOrNull { it.importAlias?.name == qualifyingName }
                    ?: return emptySequence()
                return sequence {
                    yieldAll(importDeclaration.importedNewTypeConstructors)
                    yieldAll(importDeclaration.importedDataConstructors)
                }
            } else {
                return sequence {
                    yieldAll(module.newTypeConstructors)
                    yieldAll(module.dataConstructors)
                    val importDeclarations = module.importDeclarations
                        .filter { it.importAlias == null }
                    yieldAll(importDeclarations.flatMap { it.importedNewTypeConstructors })
                    yieldAll(importDeclarations.flatMap { it.importedDataConstructors })
                }
            }
        }

    override fun getQuickFixes(): Array<LocalQuickFix> {
        return importCandidates
            .map { ImportQuickFix(it) }
            .toTypedArray()
    }

    private val importCandidates: List<String>
        get() = ExportedConstructorsIndex
            .filesExportingConstructor(element.project, element.name!!)
            .mapNotNull { it.module?.name }

}