package org.purescript.parser

import org.purescript.parser.Combinators.attempt
import org.purescript.parser.Combinators.braces
import org.purescript.parser.Combinators.choice
import org.purescript.parser.Combinators.commaSep
import org.purescript.parser.Combinators.commaSep1
import org.purescript.parser.Combinators.guard
import org.purescript.parser.Combinators.indented
import org.purescript.parser.Combinators.lexeme
import org.purescript.parser.Combinators.many1
import org.purescript.parser.Combinators.manyOrEmpty
import org.purescript.parser.Combinators.mark
import org.purescript.parser.Combinators.optional
import org.purescript.parser.Combinators.parens
import org.purescript.parser.Combinators.ref
import org.purescript.parser.Combinators.reserved
import org.purescript.parser.Combinators.same
import org.purescript.parser.Combinators.sepBy1
import org.purescript.parser.Combinators.squares
import org.purescript.parser.Combinators.token
import org.purescript.parser.Combinators.untilSame
import org.purescript.psi.PSElements
import org.purescript.psi.PSElements.Companion.Abs
import org.purescript.psi.PSElements.Companion.ArrayLiteral
import org.purescript.psi.PSElements.Companion.Bang
import org.purescript.psi.PSElements.Companion.Binder
import org.purescript.psi.PSElements.Companion.BooleanBinder
import org.purescript.psi.PSElements.Companion.BooleanLiteral
import org.purescript.psi.PSElements.Companion.CaseAlternative
import org.purescript.psi.PSElements.Companion.ConstrainedType
import org.purescript.psi.PSElements.Companion.Constructor
import org.purescript.psi.PSElements.Companion.DoNotationLet
import org.purescript.psi.PSElements.Companion.ExternDataDeclaration
import org.purescript.psi.PSElements.Companion.GenericIdentifier
import org.purescript.psi.PSElements.Companion.Guard
import org.purescript.psi.PSElements.Companion.Identifier
import org.purescript.psi.PSElements.Companion.NumericLiteral
import org.purescript.psi.PSElements.Companion.ObjectBinderField
import org.purescript.psi.PSElements.Companion.PrefixValue
import org.purescript.psi.PSElements.Companion.Program
import org.purescript.psi.PSElements.Companion.ProperName
import org.purescript.psi.PSElements.Companion.Qualified
import org.purescript.psi.PSElements.Companion.Row
import org.purescript.psi.PSElements.Companion.RowKind
import org.purescript.psi.PSElements.Companion.Star
import org.purescript.psi.PSElements.Companion.StringBinder
import org.purescript.psi.PSElements.Companion.StringLiteral
import org.purescript.psi.PSElements.Companion.TypeArgs
import org.purescript.psi.PSElements.Companion.TypeClassDeclaration
import org.purescript.psi.PSElements.Companion.TypeConstructor
import org.purescript.psi.PSElements.Companion.TypeHole
import org.purescript.psi.PSElements.Companion.TypeInstanceDeclaration
import org.purescript.psi.PSElements.Companion.UnaryMinus
import org.purescript.psi.PSElements.Companion.ValueDeclaration
import org.purescript.psi.PSElements.Companion.importModuleName
import org.purescript.psi.PSElements.Companion.pClassName
import org.purescript.psi.PSElements.Companion.pImplies
import org.purescript.psi.PSElements.Companion.qualifiedModuleName
import org.purescript.psi.PSTokens
import org.purescript.psi.PSTokens.Companion.ARROW
import org.purescript.psi.PSTokens.Companion.AS
import org.purescript.psi.PSTokens.Companion.BANG
import org.purescript.psi.PSTokens.Companion.COMMA
import org.purescript.psi.PSTokens.Companion.DARROW
import org.purescript.psi.PSTokens.Companion.DATA
import org.purescript.psi.PSTokens.Companion.DCOLON
import org.purescript.psi.PSTokens.Companion.DERIVE
import org.purescript.psi.PSTokens.Companion.DOT
import org.purescript.psi.PSTokens.Companion.EQ
import org.purescript.psi.PSTokens.Companion.FALSE
import org.purescript.psi.PSTokens.Companion.FLOAT
import org.purescript.psi.PSTokens.Companion.FORALL
import org.purescript.psi.PSTokens.Companion.FOREIGN
import org.purescript.psi.PSTokens.Companion.HIDING
import org.purescript.psi.PSTokens.Companion.IMPORT
import org.purescript.psi.PSTokens.Companion.INSTANCE
import org.purescript.psi.PSTokens.Companion.LDARROW
import org.purescript.psi.PSTokens.Companion.LET
import org.purescript.psi.PSTokens.Companion.LPAREN
import org.purescript.psi.PSTokens.Companion.NATURAL
import org.purescript.psi.PSTokens.Companion.NEWTYPE
import org.purescript.psi.PSTokens.Companion.OPERATOR
import org.purescript.psi.PSTokens.Companion.PIPE
import org.purescript.psi.PSTokens.Companion.PROPER_NAME
import org.purescript.psi.PSTokens.Companion.RPAREN
import org.purescript.psi.PSTokens.Companion.START
import org.purescript.psi.PSTokens.Companion.STRING
import org.purescript.psi.PSTokens.Companion.TRUE
import org.purescript.psi.PSTokens.Companion.WHERE

class PureParsecParser {
    private fun parseQualified(p: Parsec): Parsec =
        attempt(
        manyOrEmpty(
            attempt(token(PROPER_NAME).`as`(ProperName) + token(DOT))
        ) + p
        ).`as`(Qualified)

    // tokens
    private val dcolon = lexeme(DCOLON)
    private val dot = lexeme(DOT)
    private val eq = lexeme(EQ)
    private val where = lexeme(WHERE)

    private val idents =
        choice(
            token(PSTokens.IDENT),
            token(AS),
            token(HIDING),
            token(FORALL),
            token(PSTokens.QUALIFIED),
        )
    private val lname = lexeme(
        choice(
            token(PSTokens.IDENT),
            token(DATA),
            token(NEWTYPE),
            token(PSTokens.TYPE),
            token(FOREIGN),
            token(IMPORT),
            token(PSTokens.INFIXL),
            token(PSTokens.INFIXR),
            token(PSTokens.INFIX),
            token(PSTokens.CLASS),
            token(DERIVE),
            token(INSTANCE),
            token(PSTokens.MODULE),
            token(PSTokens.CASE),
            token(PSTokens.OF),
            token(PSTokens.IF),
            token(PSTokens.THEN),
            token(PSTokens.ELSE),
            token(PSTokens.DO),
            token(LET),
            token(TRUE),
            token(FALSE),
            token(PSTokens.IN),
            token(WHERE),
            token(FORALL),
            token(PSTokens.QUALIFIED),
            token(HIDING),
            token(AS)
        ).`as`(Identifier)
    )
    private val operator =
        choice(
            token(OPERATOR),
            token(DOT),
            token(PSTokens.DDOT),
            token(PSTokens.LARROW),
            token(LDARROW),
            token(PSTokens.OPTIMISTIC)
        )
    private val properName: Parsec = lexeme(PROPER_NAME).`as`(ProperName)
    private val moduleName = lexeme(parseQualified(token(PROPER_NAME)))
    private val stringLiteral = attempt(lexeme(STRING))
    private fun indentedList(p: Parsec): Parsec =
        mark(manyOrEmpty(untilSame(same(p))))
    private fun indentedList1(p: Parsec): Parsec =
        mark(many1(untilSame(same(p))))

    // Kinds.hs
    private val parseKind = ref()
    private val parseKindPrefixRef = ref()
    private val parseKindAtom = indented(
        choice(
            reserved("*").`as`(START).`as`(Star),
            reserved("!").`as`(BANG).`as`(Bang),
            parseQualified(properName).`as`(TypeConstructor),
            parens(parseKind)
        )
    )
    private val parseKindPrefix =
        choice((lexeme("#") + parseKindPrefixRef).`as`(RowKind), parseKindAtom)
    // Types.hs
    private val type = ref()
    private val parseForAllRef = ref()
    private val parseTypeWildcard = reserved("_")
    private val parseFunction = parens(reserved(ARROW))
    private val parseTypeVariable: Parsec =
        lexeme(guard(
            idents,
            { content: String? -> !(content == "∀" || content == "forall") },
            "not `forall`"
        )).`as`(GenericIdentifier)
    private val parseTypeConstructor: Parsec =
        parseQualified(properName).`as`(TypeConstructor)

    private fun parseNameAndType(p: Parsec): Parsec =
        indented(lexeme(lname.or(stringLiteral).`as`(GenericIdentifier))) +
        indented(dcolon) + p

    private val parseRowEnding =
        optional(
            indented(lexeme(PIPE)) +
            indented(
                attempt(parseTypeWildcard)
                .or(attempt(
                    optional(lexeme(manyOrEmpty(properName).`as`(TypeConstructor))) +
                    optional(lexeme(idents).`as`(GenericIdentifier)) +
                    optional(indented(lexeme(lname.or(stringLiteral)))) +
                    optional(indented(dcolon)) +
                    optional(type)
                ).`as`(PSElements.TypeVar))
            )
        )
    private val parseRow: Parsec =
        commaSep(parseNameAndType(type)).then(parseRowEnding).`as`(Row)
    private val parseObject  = braces(parseRow).`as`(PSElements.ObjectType)
    private val typeAtom: Parsec =
        indented(choice(
            attempt(squares(optional(type))),
            attempt(parseFunction),
            attempt(parseObject),
            attempt(parseTypeWildcard),
            attempt(parseTypeVariable),
            attempt(parseTypeConstructor),
            attempt(parseForAllRef),
            attempt(parens(parseRow)),
            attempt(parens(type))
        )).`as`(PSElements.TypeAtom)
    private val parseConstrainedType: Parsec =
        optional(attempt(
            parens(commaSep1(
                parseQualified(properName).`as`(TypeConstructor) +
                indented(manyOrEmpty(typeAtom))
            )) + lexeme(DARROW)
        )).then(indented(type)).`as`(ConstrainedType)
    private val forlall = reserved(FORALL)

    private val parseForAll =
        forlall
        .then(many1(indented(lexeme(idents).`as`(GenericIdentifier))))
        .then(indented(dot))
        .then(parseConstrainedType).`as`(PSElements.ForAll)
    private val ident =
        lexeme(idents.`as`(Identifier))
        .or(attempt(parens(lexeme(operator.`as`(Identifier)))))

    // Declarations.hs
    private val typeVarBinding =
        lexeme(idents).`as`(GenericIdentifier)
        .or(parens(
            lexeme(idents).`as`(GenericIdentifier)
                .then(indented(dcolon))
                .then(indented(parseKind))
        ))
    private val parseBinderNoParensRef = ref()
    private val parseBinderRef = ref()
    private val expr = ref()
    private val parseLocalDeclarationRef = ref()
    private val parseGuard =
        (lexeme(PIPE) + indented(commaSep(expr))).`as`(Guard)
    private val dataHead =
        reserved(DATA) +
        indented(properName).`as`(TypeConstructor) +
        manyOrEmpty(indented(typeVarBinding)).`as`(TypeArgs)

    val dataCtor =
        properName.`as`(TypeConstructor) + manyOrEmpty(indented(typeAtom))
    private val parseTypeDeclaration =
        (ident.`as`(PSElements.TypeAnnotationName) + dcolon + type)
            .`as`(PSElements.TypeDeclaration)
    private val newtypeHead =
        reserved(NEWTYPE) +
        indented(properName).`as`(TypeConstructor) +
        manyOrEmpty(indented(typeVarBinding))
        .`as`(TypeArgs)
    private val parseTypeSynonymDeclaration =
        reserved(PSTokens.TYPE)
        .then(reserved(PROPER_NAME).`as`(TypeConstructor))
        .then(manyOrEmpty(indented(lexeme(typeVarBinding))))
        .then(indented(eq) + (type))
        .`as`(PSElements.TypeSynonymDeclaration)
    private val exprWhere =
        expr + optional(where + indentedList1(parseLocalDeclarationRef))

    // Some Binders - rest at the bottom
    private val parseArrayBinder =
        squares(commaSep(parseBinderRef))
            .`as`(PSElements.ObjectBinder)
    private val parsePatternMatchObject =
        indented(braces(commaSep(
            lexeme(idents).or(lname).or(stringLiteral)
            .then(optional(indented(eq.or(lexeme(OPERATOR)))))
            .then(optional(indented(parseBinderRef)))
        ))).`as`(Binder)
    private val parseRowPatternBinder =
        indented(lexeme(OPERATOR)).then(indented(parseBinderRef))
    private val guardedDeclExpr = parseGuard + eq + exprWhere
    private val guardedDecl =
        choice(attempt(eq) + exprWhere, indented(many1(guardedDeclExpr)))

    private val parseValueDeclaration =
        attempt(many1(ident))
        .then(optional(attempt(
            indented(lexeme("@")).then(indented(braces(commaSep(lexeme(idents)))))
        )).`as`(PSElements.NamedBinder))
        .then(attempt(manyOrEmpty(parseBinderNoParensRef)))
        .then(guardedDecl).`as`(ValueDeclaration)
    private val parseDeps =
        parens(commaSep1(
            parseQualified(properName).`as`(TypeConstructor)
                .then(manyOrEmpty(typeAtom))
        )).then(indented(reserved(DARROW)))
    private val parseExternDeclaration =
        reserved(FOREIGN)
        .then(indented(reserved(IMPORT)))
        .then(indented(choice(
            reserved(DATA)
                .then(indented(reserved(PROPER_NAME).`as`(TypeConstructor)))
                .then(dcolon).then(parseKind)
                .`as`(ExternDataDeclaration),
            reserved(INSTANCE)
                .then(ident).then(indented(dcolon))
                .then(optional(parseDeps))
                .then(parseQualified(properName).`as`(pClassName))
                .then(manyOrEmpty(indented(typeAtom)))
                .`as`(PSElements.ExternInstanceDeclaration),
            attempt(ident)
                .then(optional(stringLiteral.`as`(PSElements.JSRaw)))
                .then(indented(lexeme(DCOLON)))
                .then(type)
                .`as`(PSElements.ExternDeclaration)
        )))
    private val parseAssociativity = choice(
        reserved(PSTokens.INFIXL),
        reserved(PSTokens.INFIXR),
        reserved(PSTokens.INFIX)
    )
    private val parseFixity =
        parseAssociativity.then(indented(lexeme(NATURAL))).`as`(
            PSElements.Fixity
        )
    private val parseFixityDeclaration = parseFixity
        .then(optional(reserved(PSTokens.TYPE)))
        .then(
            parseQualified(properName).`as`(PSElements.pModuleName)
            .or(ident.`as`(ProperName))
        )
        .then(reserved(AS))
        .then(lexeme(operator))
        .`as`(PSElements.FixityDeclaration)
    private val parseDeclarationRef =
        choice(
            reserved("kind")
                .then(parseQualified(properName).`as`(pClassName)),
            ident.`as`(PSElements.ValueRef),
            reserved(PSTokens.TYPE)
                .then(optional(parens(operator))),
            reserved(PSTokens.MODULE).then(moduleName)
                .`as`(importModuleName),
            reserved(PSTokens.CLASS)
                .then(parseQualified(properName).`as`(pClassName)),
            properName.`as`(ProperName)
            .then(optional(parens(optional(choice(
                reserved(PSTokens.DDOT),
                commaSep1(properName.`as`(TypeConstructor))
            )))))
        ).`as`(PSElements.PositionedDeclarationRef)
    private val parseTypeClassDeclaration =
        lexeme(PSTokens.CLASS)
        .then(optional(
            indented(choice(
                parens(commaSep1(
                    parseQualified(properName).`as`(TypeConstructor)
                        .then(manyOrEmpty(typeAtom))
                )),
                commaSep1(
                    parseQualified(properName).`as`(TypeConstructor)
                        .then(manyOrEmpty(typeAtom))
                )
            ))
            .then(optional(reserved(LDARROW)).`as`(pImplies))
        )).then(optional(indented(properName.`as`(pClassName))))
        .then(optional(manyOrEmpty(indented(typeVarBinding))))
        .then(optional(lexeme(PIPE).then(indented(commaSep1(type)))))
        .then(optional(attempt(
            indented(reserved(WHERE)).then(indentedList(parseTypeDeclaration)
        )))
        ).`as`(TypeClassDeclaration)
    private val parseTypeInstanceDeclaration =
        optional(reserved(DERIVE))
        .then(optional(reserved(NEWTYPE)))
        .then(
            reserved(INSTANCE)
            .then(ident.`as`(GenericIdentifier).then(indented(dcolon)))
            .then(optional(
                optional(reserved(LPAREN))
                .then(commaSep1(
                    parseQualified(properName).`as`(TypeConstructor)
                        .then(manyOrEmpty(typeAtom))
                ))
                .then(optional(reserved(RPAREN)))
                .then(optional(indented(reserved(DARROW))))
            ))
            .then(optional(indented(parseQualified(properName)).`as`(pClassName))
            ).then(manyOrEmpty(indented(typeAtom).or(lexeme(STRING))))
            .then(
                optional(
                    indented(reserved(DARROW))
                        .then(optional(reserved(LPAREN)))
                        .then(parseQualified(properName).`as`(TypeConstructor))
                        .then(manyOrEmpty(typeAtom))
                        .then(optional(reserved(RPAREN)))
                )
            )
            .then(optional(attempt(
                indented(reserved(WHERE))
                .then(indented(indentedList(parseValueDeclaration)))
            )))
        ).`as`(TypeInstanceDeclaration)
    private val importDeclarationType =
        optional(indented(parens(commaSep(parseDeclarationRef))))
    private val parseImportDeclaration =
        reserved(IMPORT)
        .then(indented(moduleName).`as`(importModuleName))
        .then(optional(reserved(HIDING)).then(importDeclarationType))
        .then(optional(reserved(AS).then(moduleName).`as`(importModuleName)))
        .`as`(PSElements.ImportDeclaration)
    private val decl = choice(
        (dataHead + optional(eq + sepBy1(dataCtor, PIPE)))
            .`as`(PSElements.DataDeclaration),
        (newtypeHead + eq + properName.`as`(TypeConstructor) + typeAtom)
            .`as`(PSElements.NewtypeDeclaration),
        attempt(parseTypeDeclaration),
        parseTypeSynonymDeclaration,
        optional(attempt(reserved(LPAREN)))
            .then(
                optional(
                    attempt(properName).`as`(Constructor)
                )
            )
            .then(
                optional(
                    attempt(
                        many1(
                            ident
                        )
                    )
                )
            )
            .then(optional(attempt(parseArrayBinder)))
            .then(
                optional(
                    attempt(
                        indented(lexeme("@"))
                            .then(
                                indented(
                                    braces(
                                        commaSep(
                                            lexeme(
                                                idents
                                            )
                                        )
                                    )
                                )
                            )
                    )
                ).`as`(PSElements.NamedBinder)
            ).then(
                optional(
                    attempt(parsePatternMatchObject)
                )
            )
            .then(optional(attempt(parseRowPatternBinder)))
            .then(
                optional(
                    attempt(
                        reserved(
                            RPAREN
                        )
                    )
                )
            )
            .then(
                attempt(
                    manyOrEmpty(
                        parseBinderNoParensRef
                    )
                )
            )
            .then(guardedDecl).`as`(ValueDeclaration),
        parseExternDeclaration,
        parseFixityDeclaration,
        parseImportDeclaration,
        parseTypeClassDeclaration,
        parseTypeInstanceDeclaration
    )
    private val parseLocalDeclaration = choice(
        attempt(parseTypeDeclaration),
        // this is for when used with LET
        optional(attempt(reserved(LPAREN)))
            .then(
                optional(
                    attempt(properName).`as`(Constructor)
                )
            )
            .then(
                optional(
                    attempt(
                        many1(
                            ident
                        )
                    )
                )
            )
            .then(optional(attempt(parseArrayBinder)))
            .then(
                optional(
                    attempt(
                        indented(lexeme("@"))
                            .then(
                                indented(
                                    braces(
                                        commaSep(
                                            lexeme(
                                                idents
                                            )
                                        )
                                    )
                                )
                            )
                    )
                ).`as`(PSElements.NamedBinder)
            ).then(
                optional(
                    attempt(parsePatternMatchObject)
                )
            )
            .then(optional(attempt(parseRowPatternBinder)))
            .then(
                optional(
                    attempt(
                        reserved(
                            RPAREN
                        )
                    )
                )
            )
            // ---------- end of LET stuff -----------
            .then(
                attempt(
                    manyOrEmpty(
                        parseBinderNoParensRef
                    )
                )
            )
            .then(guardedDecl).`as`(ValueDeclaration)
    )
    private val parseModule = reserved(PSTokens.MODULE)
        .then(indented(moduleName).`as`(PSElements.pModuleName))
        .then(optional(parens(commaSep1(parseDeclarationRef))))
        .then(reserved(WHERE))
        .then(indentedList(decl))
        .`as`(PSElements.Module)
    val program: Parsec = indentedList(parseModule).`as`(Program)

    // Literals
    private val parseBooleanLiteral =
        reserved(TRUE).or(reserved(FALSE)).`as`(BooleanLiteral)
    private val parseNumericLiteral =
        reserved(NATURAL).or(reserved(FLOAT)).`as`(NumericLiteral)
    private val parseStringLiteral = reserved(STRING).`as`(StringLiteral)
    private val parseCharLiteral = lexeme("'").`as`(StringLiteral)
    private val parseArrayLiteral = squares(commaSep(expr)).`as`(ArrayLiteral)
    private val parseTypeHole = lexeme("?").`as`(TypeHole)
    private val parseIdentifierAndValue =
        indented(lexeme(lname).or(stringLiteral))
        .then(optional(indented(lexeme(OPERATOR).or(reserved(COMMA)))))
        .then(optional(indented(expr)))
        .`as`(ObjectBinderField)
    private val parseObjectLiteral =
        braces(commaSep(parseIdentifierAndValue)).`as`(PSElements.ObjectLiteral)
    private val typedIdent =
        optional(reserved(LPAREN))
        .then(many1(
            lexeme(idents).`as`(GenericIdentifier)
            .or(parseQualified(properName).`as`(TypeConstructor))
        ))
        .then(optional(indented(dcolon).then(indented(type))))
        .then(optional(parseObjectLiteral))
        .then(optional(reserved(RPAREN)))
    private val parseAbs =
        reserved(PSTokens.BACKSLASH)
        .then(choice(
                many1(typedIdent).`as`(Abs),
                many1(indented(ident.or(parseBinderNoParensRef).`as`(Abs)))
        ))
        .then(indented(reserved(ARROW)))
        .then(expr)
    private val parseVar =
        attempt(manyOrEmpty(attempt(
            token(PROPER_NAME).`as`(qualifiedModuleName).then(token(DOT))
        )).then(ident).`as`(Qualified)
        ).`as`(PSElements.Var)
    private val parseConstructor =
        parseQualified(properName).`as`(Constructor)
    private val parseCaseAlternative =
        commaSep1(expr.or(parseTypeWildcard))
        .then(indented(choice(
            many1(parseGuard + indented(lexeme(ARROW) + expr)),
            reserved(ARROW).then(expr)
        ))
        ).`as`(CaseAlternative)
    private val parseCase = reserved(PSTokens.CASE)
        .then(commaSep1(expr.or(parseTypeWildcard)))
        .then(indented(reserved(PSTokens.OF)))
        .then(indented(indentedList(mark(parseCaseAlternative))))
        .`as`(PSElements.Case)
    private val parseIfThenElse = reserved(PSTokens.IF)
        .then(indented(expr))
        .then(indented(reserved(PSTokens.THEN)))
        .then(indented(expr))
        .then(indented(reserved(PSTokens.ELSE)))
        .then(indented(expr))
        .`as`(PSElements.IfThenElse)
    private val parseLet = reserved(LET)
        .then(indented(indentedList1(parseLocalDeclaration)))
        .then(indented(reserved(PSTokens.IN)))
        .then(expr)
        .`as`(PSElements.Let)
    private val letBinding =
        choice(
            attempt(parseTypeDeclaration),
            optional(attempt(reserved(LPAREN)))
                .then(optional(attempt(properName).`as`(Constructor)))
                .then(optional(attempt(many1(ident))))
                .then(optional(attempt(parseArrayBinder)))
                .then(optional(attempt(
                    indented(lexeme("@"))
                    .then(indented(braces(commaSep(lexeme(idents)))))
                )).`as`(PSElements.NamedBinder))
                .then(optional(attempt(parsePatternMatchObject)))
                .then(optional(attempt(parseRowPatternBinder)))
                .then(optional(attempt(reserved(RPAREN))))
                .then(attempt(manyOrEmpty(parseBinderNoParensRef)))
                .then(choice(
                    attempt(indented(many1(
                        parseGuard + indented(eq + exprWhere)
                    ))),
                    attempt(indented(eq + (exprWhere)))
                )).`as`(ValueDeclaration)
        )
    private val parseDoNotationBind: Parsec =
        parseBinderRef
        .then(indented(reserved(PSTokens.LARROW)).then(expr))
        .`as`(PSElements.DoNotationBind)
    private val doExpr = expr.`as`(PSElements.DoNotationValue)
    private val doStatement =
        choice(
            reserved(LET).then(indented(indentedList1(letBinding))).`as`(DoNotationLet),
            attempt(parseDoNotationBind),
            attempt(doExpr)
        )
    private val doBlock =
        reserved(PSTokens.DO)
        .then(indented(indentedList(mark(doStatement))))
    private val parsePropertyUpdate =
        reserved(lname.or(stringLiteral))
        .then(optional(indented(eq)))
        .then(indented(expr))
    private val parseValueAtom = choice(
        attempt(parseTypeHole),
        attempt(parseNumericLiteral),
        attempt(parseStringLiteral),
        attempt(parseBooleanLiteral),
        attempt(
            reserved(PSTokens.TICK) +
                properName.`as`(ProperName)
                .or(many1(lexeme(idents).`as`(ProperName))) +
                reserved(PSTokens.TICK)
        ),
        parseArrayLiteral,
        parseCharLiteral,
        attempt(indented(braces(commaSep1(indented(parsePropertyUpdate))))),
        attempt(parseObjectLiteral),
        parseAbs,
        attempt(parseConstructor),
        attempt(parseVar),
        parseCase,
        parseIfThenElse,
        doBlock,
        parseLet,
        parens(expr).`as`(PSElements.Parens)
    )
    private val parseAccessor: Parsec =
        attempt(indented(token(DOT)).then(indented(lname.or(stringLiteral))))
            .`as`(PSElements.Accessor)
    private val parseIdentInfix: Parsec =
        choice(
            (reserved(PSTokens.TICK) + parseQualified(lexeme(idents)))
                .lexeme(PSTokens.TICK),
            parseQualified(lexeme(operator))
        ).`as`(PSElements.IdentInfix)
    private val indexersAndAccessors =
        parseValueAtom +
        manyOrEmpty(choice(
            parseAccessor,
            attempt(indented(braces(commaSep1(indented(parsePropertyUpdate))))),
            indented(dcolon + type)
        ))
    private val parseValuePostFix =
        indexersAndAccessors +
        manyOrEmpty(
            indented(indexersAndAccessors)
            .or(attempt(indented(dcolon) + type))
        )
    private val parsePrefixRef = ref()
    private val parsePrefix =
        choice(
            parseValuePostFix,
            indented(lexeme("-")).then(parsePrefixRef).`as`(UnaryMinus)
        ).`as`(PrefixValue)

    // Binder
    private val parseIdentifierAndBinder =
        lexeme(lname.or(stringLiteral))
        .then(indented(eq.or(lexeme(OPERATOR))))
        .then(indented(parseBinderRef))
    private val parseObjectBinder =
        braces(commaSep(parseIdentifierAndBinder))
            .`as`(PSElements.ObjectBinder)
    private val parseNullBinder = reserved("_")
        .`as`(PSElements.NullBinder)
    private val parseStringBinder =
        lexeme(STRING).`as`(StringBinder)
    private val parseBooleanBinder =
        lexeme("true").or(lexeme("false")).`as`(BooleanBinder)
    private val parseNumberBinder =
        optional(lexeme("+").or(lexeme("-")))
        .then(lexeme(NATURAL).or(lexeme(FLOAT)))
        .`as`(PSElements.NumberBinder)
    private val parseNamedBinder =
        ident
            .then(indented(lexeme("@"))
            .then(indented(parseBinderRef)))
            .`as`(PSElements.NamedBinder)
    private val parseVarBinder = ident.`as`(PSElements.VarBinder)
    private val parseConstructorBinder =
        lexeme(
            parseQualified(properName).`as`(GenericIdentifier)
            .then(manyOrEmpty(indented(parseBinderNoParensRef)))
        ).`as`(PSElements.ConstructorBinder)
    private val parseNullaryConstructorBinder =
        lexeme(parseQualified(properName.`as`(ProperName)))
            .`as`(PSElements.ConstructorBinder)
    private val parsePatternMatch =
        indented(braces(commaSep(lexeme(idents)))).`as`(Binder)
    private val parseCharBinder =
        lexeme("'").`as`(StringBinder)
    private val parseBinderAtom = choice(
        attempt(parseNullBinder),
        attempt(parseStringBinder),
        attempt(parseBooleanBinder),
        attempt(parseNumberBinder),
        attempt(parseNamedBinder),
        attempt(parseVarBinder),
        attempt(parseConstructorBinder),
        attempt(parseObjectBinder),
        attempt(parseArrayBinder),
        attempt(parsePatternMatch),
        attempt(parseCharBinder),
        attempt(parens(parseBinderRef))
    ).`as`(PSElements.BinderAtom)
    private val parseBinder =
        parseBinderAtom
        .then(optional(lexeme(OPERATOR).then(parseBinderRef))).`as`(Binder)

    private val type0 = ref()
    private val type1 = ref()
    private val type2 = ref()
    private val type3 = ref()
    private val type4 = ref()
    private val type5 = ref()
    private val arrow = reserved(ARROW)
    private val darrow = reserved(DARROW)
    private val qualOp = choice(
        operator,
        lexeme("<="),
        lexeme("-"),
        lexeme("#"),
        lexeme(":"),
    )

    init {
        type0.setRef(type1 + optional(dcolon + type0))
        type1.setRef(type2.or(forlall + many1(typeVarBinding) + dot + type1))
        type2.setRef(type3 + optional(arrow.or(darrow) + type1))
        type3.setRef(type4 + optional(qualOp + type4))
        type4.setRef(type5.or(lexeme("#") + type4))
        type5.setRef(many1(typeAtom))
        parseKindPrefixRef.setRef(parseKindPrefix)
        parseKind.setRef(
            (parseKindPrefix +
            optional(
                arrow.or(optional(
                    parseQualified(properName).`as`(TypeConstructor)
            )) + optional(parseKind))).`as`(PSElements.FunKind)
        )
        type.setRef(
            many1(typeAtom.or(lexeme(STRING)) + optional(dcolon + parseKind))
            .then(optional(
                choice(
                    reserved(ARROW),
                    reserved(DARROW),
                    reserved(PSTokens.OPTIMISTIC),
                    reserved(OPERATOR)
                )
                .then(type)
            )).`as`(PSElements.Type)
        )
        parseForAllRef.setRef(parseForAll)
        parseLocalDeclarationRef.setRef(parseLocalDeclaration)
        parsePrefixRef.setRef(parsePrefix)
        expr.setRef(
        (parsePrefix + optional(attempt(indented(parseIdentInfix)) + expr))
            .`as`(PSElements.Value)
        )
        parseBinderRef.setRef(parseBinder)
        parseBinderNoParensRef.setRef(
            choice(
                attempt(parseNullBinder),
                attempt(parseStringBinder),
                attempt(parseBooleanBinder),
                attempt(parseNumberBinder),
                attempt(parseNamedBinder),
                attempt(parseVarBinder),
                attempt(parseNullaryConstructorBinder),
                attempt(parseObjectBinder),
                attempt(parseArrayBinder),
                attempt(parsePatternMatch),
                attempt(parseCharBinder),
                attempt(parens(parseBinderRef))
            ).`as`(Binder)
        )
    }
}