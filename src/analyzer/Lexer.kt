package analyzer

import data_structure.registry.LexicalRegister
import data_structure.registry.SymbolTable
import data_structure.token.DataType
import data_structure.token.Token
import data_structure.token.TokenConstant
import data_structure.token.TokenID
import kotlin.system.exitProcess

/**
 * Analisador Lexico - implementacao do automato;
 * 15 estados;
 * Dado o comportamento iterativo do analisador lexico, eh implementado como um Iterator de data_structure.token.Token;
 */
class Lexer(source: String) : Iterator<Token?> {

    companion object {
        const val SYMBOLS = "!\"&'(*)+,-./:;<=>?[]_{} \n\r\t"
        const val INITIAL = 0
        const val FINAL = 3
    }

    private var source: String = source.trim().replace("\r\n", "\n")
    var line: Int = 1
    private var index: Int = 0

    /**
     * Diz se existe um proximo token
     *
     * @return true se houve um proximo token, false caso contrario
     */
    override fun hasNext(): Boolean {
        return index < source.length
    }

    /**
     * Realiza os passos do automato, do estado inicial ate o final, para retornar o proximo token.
     *
     * @return o proximo token
     */
    override fun next(): Token? {
        var state = INITIAL
        val lex = StringBuilder()
        var token: Token? = null

        while (state != FINAL && index <= source.length) {
            val c = getCurrentChar()
            assertValidChar(c)

            when (state) {
                INITIAL -> if (c == '!') {
                    lex.append(c)
                    index++
                    state = 11
                } else if ("<>=".contains(c)) {
                    lex.append(c)
                    index++
                    state = 10
                } else if ("+-*,;()".contains(c)) {
                    lex.append(c)
                    index++
                    token = SymbolTable[lex.toString()]
                    state = FINAL
                } else if (c == '\'') {
                    lex.append(c)
                    index++
                    state = 8
                } else if (c == '_') {
                    lex.append(c)
                    index++
                    state = 1
                } else if (c.isLetter()) {
                    lex.append(c)
                    index++
                    state = 2
                } else if (c == '0') {
                    lex.append(c)
                    index++
                    state = 5
                } else if (c.isDigit()) {
                    lex.append(c)
                    index++
                    state = 4
                } else if ("\t\n\r".contains(c) || c.isWhitespace()) {
                    index++
                    if ("\n\r".contains(c)) {
                        line++
                    }
                } else if (c == '/') {
                    lex.append(c)
                    index++
                    state = 12
                } else {
                    lex.append(c)
                    errorLexemeNotIdentified(lex)
                }
                1 -> when {
                    c == '_' -> {
                        lex.append(c)
                        index++
                    }
                    c.isLetterOrDigit() -> {
                        lex.append(c)
                        index++
                        state = 2
                    }
                    else -> {
                        errorLexemeNotIdentified(lex)
                    }
                }
                2 -> if (c == '_' || c.isLetterOrDigit()) {
                    lex.append(c)
                    index++
                } else {
                    state = FINAL
                    val t = SymbolTable[lex.toString()]
                    if (t == null) {
                        token = TokenID(lex.toString())
                        SymbolTable[lex.toString()] = token
                    } else {
                        token = t
                    }
                }
                4 -> if (c.isDigit()) {
                    lex.append(c)
                    index++
                } else {
                    token = TokenConstant(lex.toString(), DataType.INTEGER)
                    LexicalRegister[lex.toString()] = token
                    state = FINAL
                }
                5 -> when {
                    c.isDigit() -> {
                        lex.append(c)
                        index++
                        state = 4
                    }
                    "H".contains(c, true) -> {
                        lex.append(c)
                        index++
                        state = 6
                    }
                    else -> {
                        token = TokenConstant(lex.toString(), DataType.INTEGER)
                        LexicalRegister[lex.toString()] = token
                        state = FINAL
                    }
                }
                6 -> if (c.isHexadecimal()) {
                    lex.append(c)
                    index++
                    state = 7
                } else {
                    errorLexemeNotIdentified(lex)
                }
                7 -> if (c.isHexadecimal()) {
                    lex.append(c)
                    token = TokenConstant(lex.toString(), DataType.BYTE)
                    LexicalRegister[lex.toString()] = token
                    index++
                    state = FINAL
                } else {
                    errorLexemeNotIdentified(lex)
                }
                8 -> if (c == '\'') {
                    lex.append(c)
                    index++
                    state = 9
                } else if (!"\r\n".contains(c)) {
                    lex.append(c)
                    index++
                } else {
                    errorLexemeNotIdentified(lex)
                }
                9 -> if (c == '\'') {
                    lex.append(c)
                    index++
                    state = 8
                } else {
                    token = TokenConstant(lex.toString(), DataType.STRING)
                    LexicalRegister[lex.toString()] = token
                    state = FINAL
                }
                10 -> {
                    if (c == '=') {
                        lex.append(c)
                        index++
                    }
                    token = SymbolTable[lex.toString()]
                    state = FINAL
                }
                11 -> if(c == '='){
                    lex.append(c)
                    index++
                    token = SymbolTable[lex.toString()]
                    state = FINAL
                } else {
                    errorLexemeNotIdentified(lex)
                }
                12 -> if(c == '*'){
                    lex.setLength(0)
                    index++
                    state = 13
                } else {
                    token = SymbolTable[lex.toString()]
                    state = FINAL
                }
                13 -> if(c == '*'){
                    index++
                    state = 14
                } else {
                    if("\n\r".contains(c)){
                        line++
                    }
                    index++
                }
                14 -> when (c) {
                    '/' -> {
                        index++
                        state = INITIAL
                    }
                    '*' -> {
                        index++
                    }
                    else -> {
                        if("\n\r".contains(c)){
                            line++
                        }
                        index++
                        state = 13
                    }
                }
            }
        }
        return token
    }

    private fun errorLexemeNotIdentified(lex: StringBuilder) {
        println("$line:lexema nao identificado [$lex]")
        exitProcess(1)
    }

    /**
     * Obtem o caractere atual; E necessario avancar um caractere alem do ultimo do
     * arquivo para identificar o ultimo lexema, portanto um espaco e adicionado.
     *
     * @return caractere atual
     */
    private fun getCurrentChar(): Char {
        return if (index < source.length) source[index] else ' '
    }

    /**
     * Se o caractere nao for permitido em um arquivo fonte, reporta erro e finaliza o programa
     *
     * @param c caractere
     */
    private fun assertValidChar(c: Char) {
        if (!isValidChar(c)) {
            println("$line: caractere invalido.")
            exitProcess(1)
        }
    }

    private fun isValidChar(c: Char): Boolean {
        return c.isLetterOrDigit() || SYMBOLS.contains(c)
    }

    private fun Char.isHexadecimal(): Boolean {
        return this.toLowerCase() in 'a'..'f' || this.isDigit()
    }

}