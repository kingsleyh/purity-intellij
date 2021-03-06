package org.purescript.psi.expression

import com.intellij.lang.ASTNode
import org.purescript.psi.PSPsiElement
import org.purescript.psi.name.PSQualifiedOperatorName

/**
 * A Operator in an expression, e.g.
 * ```
 * P.+
 * ```
 * in
 * ```
 * import Prelude as P
 * f = 1 P.+ 3
 * ```
 */
class PSExpressionOperator(node: ASTNode) : PSPsiElement(node) {

    /**
     * @return the [PSQualifiedOperatorName] identifying this constructor
     */
    internal val qualifiedOperator: PSQualifiedOperatorName
        get() = findNotNullChildByClass(PSQualifiedOperatorName::class.java)

    override fun getName(): String = qualifiedOperator.name

    override fun getReference() =
        ExpressionSymbolReference(
            this,
                this.qualifiedOperator.operator
        )
}