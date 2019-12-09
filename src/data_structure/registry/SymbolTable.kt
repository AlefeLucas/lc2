package data_structure.registry

import data_structure.token.TokenType.*
import data_structure.token.DataType
import data_structure.token.Token
import data_structure.token.TokenConstant
import data_structure.token.TokenType

/**
 * Tabela de simbolos - implementada como um hash que mapeia o lexema no token.
 */
object SymbolTable : HashMap<String, Token>(values().size - 2) {

    init {
        put("main", MAIN)
        put("const", CONST)
        put("integer", INTEGER)
        put("byte", BYTE)
        put("string", STRING)
        put("boolean", BOOLEAN)
        put("while", WHILE)
        put("if", IF)
        put("else", ELSE)
        put("begin", BEGIN)
        put("end", END)
        put("then", THEN)
        put("readln", READLN)
        put("write", WRITE)
        put("writeln", WRITELN)
        put("true", TokenConstant("true", DataType.BOOLEAN))
        put("false", TokenConstant("false", DataType.BOOLEAN))
        put("and", AND)
        put("or", OR)
        put("not", NOT)
        put("<=", LESS_OR_EQUAL)
        put(">=", GREATER_OR_EQUAL)
        put("!=", NOT_EQUAL)
        put("==", EQUAL)
        put("<", LESS)
        put(">", GREATER)
        put("=", ASSIGN)
        put("+", PLUS)
        put("-", MINUS)
        put("*", MULTIPLY)
        put("/", DIVIDE)
        put(",", COMMA)
        put(";", SEMICOLON)
        put("(", OPEN_BRACE)
        put(")", CLOSE_BRACE)
    }

    /**
     * Adiciona um item a tabela de simbolos pelo lexema e tipo de token
     *
     * @return o token previamente associado com aquele lexema, se houver
     */
    private fun put(lexeme: String, type: TokenType) {
        this[lexeme] = Token(lexeme, type)
    }

    /**
     * Converte a tabela de simbolos para string - para permitir a visualizacao dos conteudos do mesmo.
     */
    override fun toString(): String {
        val s = StringBuilder("${this::class.simpleName} {\n")
        this.forEach { _, token -> s.appendln("$token") }
        s.appendln("}\n")
        return s.toString()
    }


}