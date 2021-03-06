package org.purescript.lexer

import com.intellij.lexer.Lexer
import com.intellij.testFramework.LexerTestCase

abstract class PSLexerTestBase(val folder: String): LexerTestCase() {
    override fun createLexer(): Lexer {
        return LayoutLexer(PSLexer())
    }

    override fun getDirPath(): String = folder
    fun getTestDataPath(): String = "test-data"

    override fun getPathToTestDataFile(extension: String?): String {
        return getTestDataPath() + "/" + dirPath + "/" + super.getTestName(false) + extension
    }

    override fun getExpectedFileExtension(): String {
        return ".lex.txt"
    }
}