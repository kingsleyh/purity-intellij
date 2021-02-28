package org.purescript.psi

import com.intellij.lang.ASTNode

sealed class PSImportedItem(node: ASTNode) : PSPsiElement(node) {
    abstract override fun getName(): String
}

class PSImportedClass(node: ASTNode) : PSImportedItem(node) {
    private val properName: PSProperName get() =
        findNotNullChildByClass(PSProperName::class.java)

    override fun getName(): String = properName.name
}

class PSImportedData(node: ASTNode) : PSImportedItem(node) {
    private val properName: PSProperName get() =
        findNotNullChildByClass(PSProperName::class.java)

    override fun getName(): String = properName.name
}

class PSImportedKind(node: ASTNode) : PSImportedItem(node) {
    private val properName: PSProperName get() =
        findNotNullChildByClass(PSProperName::class.java)

    override fun getName(): String = properName.name
}

class PSImportedOperator(node: ASTNode) : PSImportedItem(node) {
    private val identifier: PSIdentifierImpl get() =
        findNotNullChildByClass(PSIdentifierImpl::class.java)

    override fun getName(): String = identifier.name
}

class PSImportedType(node: ASTNode) : PSImportedItem(node) {
    private val identifier: PSIdentifierImpl get() =
        findNotNullChildByClass(PSIdentifierImpl::class.java)

    override fun getName(): String = identifier.name
}

class PSImportedValue(node: ASTNode) : PSImportedItem(node) {
    private val identifier: PSIdentifierImpl get() =
        findNotNullChildByClass(PSIdentifierImpl::class.java)

    override fun getName(): String = identifier.name
}

