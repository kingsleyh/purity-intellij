package org.purescript

import com.intellij.psi.PsiFile
import org.purescript.file.PSFile
import org.purescript.psi.PSModule
import org.purescript.psi.PSNewTypeDeclarationImpl
import org.purescript.psi.classes.PSClassConstraint
import org.purescript.psi.classes.PSClassDeclaration
import org.purescript.psi.classes.PSClassMember
import org.purescript.psi.data.PSDataConstructor
import org.purescript.psi.data.PSDataDeclaration
import org.purescript.psi.exports.PSExportedData
import org.purescript.psi.exports.PSExportedDataMember
import org.purescript.psi.exports.PSExportedItem
import org.purescript.psi.imports.PSImportDeclarationImpl
import org.purescript.psi.imports.PSImportedClass
import org.purescript.psi.imports.PSImportedData
import org.purescript.psi.imports.PSImportedItem


fun PsiFile.getModule(): PSModule {
    return (this as PSFile).module
}

fun PsiFile.getDataDeclaration(): PSDataDeclaration {
    return getModule().dataDeclarations.single()
}

fun PsiFile.getDataConstructor(): PSDataConstructor {
    return getDataDeclaration().dataConstructorList!!.dataConstructors.single()
}

fun PsiFile.getExportedDataDeclarations(): List<PSDataDeclaration> {
    return getModule().exportedDataDeclarations
}

fun PsiFile.getImportDeclarations(): Array<PSImportDeclarationImpl> {
    return getModule().importDeclarations
}

fun PsiFile.getImportDeclaration(): PSImportDeclarationImpl {
    return getImportDeclarations().single()
}

fun PsiFile.getNewTypeDeclarations(): Array<PSNewTypeDeclarationImpl> {
    return getModule().newTypeDeclarations
}

fun PsiFile.getNewTypeDeclaration(): PSNewTypeDeclarationImpl {
    return getNewTypeDeclarations().single()
}

fun PsiFile.getImportedItem(): PSImportedItem {
    return getImportDeclaration().importList!!.importedItems.single()
}

fun PsiFile.getImportedClass(): PSImportedClass {
    return getImportedItem() as PSImportedClass
}

fun PsiFile.getImportedData(): PSImportedData {
    return getImportedItem() as PSImportedData
}

fun PsiFile.getExportedItem(): PSExportedItem {
    return getModule().exportList!!.exportedItems.single()
}

fun PsiFile.getExportedData(): PSExportedData {
    return getExportedItem() as PSExportedData
}

fun PsiFile.getExportedDataMember(): PSExportedDataMember {
    return getExportedData().dataMemberList!!.dataMembers.single()
}

fun PsiFile.getClassDeclarations(): Array<PSClassDeclaration> {
    return getModule().classDeclarations
}

fun PsiFile.getClassDeclaration(): PSClassDeclaration {
    return getModule().classDeclarations.single()
}

fun PsiFile.getClassMember(): PSClassMember {
    return getModule().classDeclarations.single().classMembers.single()
}

fun PsiFile.getClassConstraint(): PSClassConstraint {
    return getModule().classDeclarations.single().classConstraints.single()
}
