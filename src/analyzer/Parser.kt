package analyzer

import data_structure.token.*
import utils.Wrapper
import kotlin.system.exitProcess

/**
 * Analisador sintatico - Implementa a gramatica da linguagem. Cada simbolo nao terminal
 * tem seu metodo e cada simbolo terminal eh casado com o matchToken.
 *
 *
 * Analisador semântico - performa os esquemas de traducao enumerados e denotados por r#
 */
class Parser(source: String) {
    private val lexer: Lexer = Lexer(source)
    private var token: Token? = null
    private var matchedToken: Token? = null

    companion object {
        private val FIRST_D =
            arrayOf<TokenType?>(TokenType.BOOLEAN, TokenType.BYTE, TokenType.CONST, TokenType.INTEGER, TokenType.STRING)
        private val FIRST_C = arrayOf<TokenType?>(
            TokenType.ID,
            TokenType.IF,
            TokenType.READLN,
            TokenType.SEMICOLON,
            TokenType.WHILE,
            TokenType.WRITE,
            TokenType.WRITELN
        )
        private val FIRST_E = arrayOf<TokenType?>(
            TokenType.CONSTANT,
            TokenType.ID,
            TokenType.MINUS,
            TokenType.NOT,
            TokenType.OPEN_BRACE,
            TokenType.PLUS
        )
        private val INTEGER_CONSTANTS = arrayOf<DataType?>(DataType.BYTE, DataType.INTEGER)

    }

    /**
     * "Programa principal" do analisador sintatico - obtem o primeiro
     * token do analisador lexico e chama o simbolo inicial.
     */
    fun parse() {
        try {
            token = lexer.next()
            s()
            if (token != null) {
                errorTokenUnexpected()
            }
        } catch (ex: NullPointerException) {
            errorEndOfFile()
        }
    }

    /**
     * "Casa token" - verifica se o token atual eh o token esperado pela gramatica.
     *
     * @param expectedToken token esperado
     */
    private fun matchToken(expectedToken: TokenType) {
        if (expectedToken == token!!.value) {
            matchedToken = token
            token = lexer.next()
        } else {
            errorTokenUnexpected()
        }
    }

    /**
     * S  =>  {D}main {C} end
     */
    private fun s() {
        while (FIRST_D.contains(token!!.value)) {
            d()
        }
        matchToken(TokenType.MAIN)
        while (FIRST_C.contains(token!!.value)) {
            c()
        }
        matchToken(TokenType.END)
    }

    /**
     * D  =>  integer J|
     * boolean J|
     * string J|
     * byte J|
     * const id P;
     */
    private fun d() {
        when {
            token!!.value === TokenType.INTEGER -> {
                matchToken(TokenType.INTEGER)
                j(DataType.INTEGER) //1
            }
            token!!.value === TokenType.STRING -> {
                matchToken(TokenType.STRING)
                j(DataType.STRING) //2
            }
            token!!.value === TokenType.BOOLEAN -> {
                matchToken(TokenType.BOOLEAN)
                j(DataType.BOOLEAN) //3
            }
            token!!.value === TokenType.BYTE -> {
                matchToken(TokenType.BYTE)
                j(DataType.BYTE) //4
            }
            else -> {
                matchToken(TokenType.CONST)
                matchToken(TokenType.ID)
                //5
                val id = matchedToken as TokenID?
                r5(id)
                val pType = Wrapper<DataType>()
                p(pType)
                //8
                r8(id, pType)
                matchToken(TokenType.SEMICOLON)
            }
        }
    }

    /**
     * P  =>  =(constant | - constant)
     */
    private fun p(pType: Wrapper<DataType>) {
        matchToken(TokenType.ASSIGN)
        val constant: TokenConstant?
        if (token!!.value === TokenType.CONSTANT) {
            matchToken(TokenType.CONSTANT)
            //6
            constant = matchedToken as TokenConstant?
            r6(constant)
        } else {
            matchToken(TokenType.MINUS)
            matchToken(TokenType.CONSTANT)
            //7
            constant = matchedToken as TokenConstant?
            r7(constant)
        }
        //41
        r41(pType, constant)
    }

    /**
     * J  =>  M{,M};
     */
    private fun j(jType: DataType) {
        m(jType) //9
        while (token!!.value === TokenType.COMMA) {
            matchToken(TokenType.COMMA)
            m(jType) //10
        }
        matchToken(TokenType.SEMICOLON)
    }

    /**
     * M  =>  id[ P ]
     */
    private fun m(mType: DataType) {
        matchToken(TokenType.ID)
        val id = matchedToken as TokenID?
        //11
        r11(mType, id)
        if (token!!.value === TokenType.ASSIGN) {
            val pType = Wrapper<DataType>()
            p(pType)
            //12
            r12(id, pType)
        }
    }

    /**
     * C  =>  id=E;|
     * write K|
     * writeln K|
     * readln"("id")";|
     * while N L|
     * if N then L [else L]|
     * ;
     */
    private fun c() {
        if (token!!.value === TokenType.ID) {
            matchToken(TokenType.ID)
            //13
            val id = matchedToken as TokenID?
            r13(id)
            matchToken(TokenType.ASSIGN)
            val eType = Wrapper<DataType>()
            e(eType)
            //14
            r14(id, eType)
            matchToken(TokenType.SEMICOLON)
        } else if (token!!.value === TokenType.WRITE) {
            matchToken(TokenType.WRITE)
            k()
        } else if (token!!.value === TokenType.WRITELN) {
            matchToken(TokenType.WRITELN)
            k()
        } else if (token!!.value === TokenType.READLN) {
            matchToken(TokenType.READLN)
            matchToken(TokenType.OPEN_BRACE)
            matchToken(TokenType.ID)
            //43
            val id = matchedToken as TokenID?
            r43(id)
            matchToken(TokenType.CLOSE_BRACE)
            matchToken(TokenType.SEMICOLON)
        } else if (token!!.value === TokenType.WHILE) {
            matchToken(TokenType.WHILE)
            n()
            l()
        } else if (token!!.value === TokenType.IF) {
            matchToken(TokenType.IF)
            n()
            matchToken(TokenType.THEN)
            l()
            if (token!!.value === TokenType.ELSE) {
                matchToken(TokenType.ELSE)
                l()
            }
        } else {
            matchToken(TokenType.SEMICOLON)
        }
    }

    /**
     * N  =>  "("E")"
     */
    private fun n() {
        matchToken(TokenType.OPEN_BRACE)
        val eType = Wrapper<DataType>()
        e(eType)
        //28
        r28(eType)
        matchToken(TokenType.CLOSE_BRACE)
    }

    /**
     * K  =>  "("[E{,E}]")");
     */
    private fun k() {
        matchToken(TokenType.OPEN_BRACE)
        if (FIRST_E.contains(token!!.value)) {
            val e1Type = Wrapper<DataType>()
            e(e1Type)
            r42(e1Type)
            while (token!!.value === TokenType.COMMA) {
                matchToken(TokenType.COMMA)
                val e2Type = Wrapper<DataType>()
                e(e2Type)
                r42(e2Type)
            }
        }
        matchToken(TokenType.CLOSE_BRACE)
        matchToken(TokenType.SEMICOLON)
    }

    /**
     * E  =>  F{(== F|!= F|< F|> F|<= F|>= F)}
     */
    private fun e(eType: Wrapper<DataType>) {
        val f1Type = Wrapper<DataType>()
        f(f1Type)
        //30
        r30(eType, f1Type)
        val logicOp = arrayOf<TokenType?>(
            TokenType.EQUAL,
            TokenType.GREATER,
            TokenType.GREATER_OR_EQUAL,
            TokenType.LESS,
            TokenType.LESS_OR_EQUAL,
            TokenType.NOT_EQUAL
        )

        while (logicOp.contains(token!!.value)) {
            when {
                token!!.value === TokenType.EQUAL -> {
                    matchToken(TokenType.EQUAL)
                    val f2Type = Wrapper<DataType>()
                    f(f2Type)
                    //17
                    r17(eType, f2Type)
                }
                token!!.value === TokenType.NOT_EQUAL -> {
                    matchToken(TokenType.NOT_EQUAL)
                    val f2Type = Wrapper<DataType>()
                    f(f2Type)
                    //18
                    r18(eType, f2Type)
                }
                token!!.value === TokenType.LESS -> {
                    matchToken(TokenType.LESS)
                    val f2Type = Wrapper<DataType>()
                    f(f2Type)
                    r19(eType, f2Type)
                }
                token!!.value === TokenType.LESS_OR_EQUAL -> {
                    matchToken(TokenType.LESS_OR_EQUAL)
                    val f2Type = Wrapper<DataType>()
                    f(f2Type)
                    r19(eType, f2Type)
                }
                token!!.value === TokenType.GREATER -> {
                    matchToken(TokenType.GREATER)
                    val f2Type = Wrapper<DataType>()
                    f(f2Type)
                    r19(eType, f2Type)
                }
                else -> {
                    matchToken(TokenType.GREATER_OR_EQUAL)
                    val f2Type = Wrapper<DataType>()
                    f(f2Type)
                    r19(eType, f2Type)
                }
            }
            //{E.tipo := BOOLEAN}
            r20(eType)
        }
    }

    /**
     * F  =>  (+ G|- G| G){(+ G|- G|or G)}
     */
    private fun f(fType: Wrapper<DataType>) {
        val g1Type: Wrapper<DataType>
        when {
            token!!.value === TokenType.PLUS -> {
                matchToken(TokenType.PLUS)
                g1Type = Wrapper()
                g(g1Type)
                //21
                r21(g1Type)
            }
            token!!.value === TokenType.MINUS -> {
                matchToken(TokenType.MINUS)
                g1Type = Wrapper()
                g(g1Type)
                //22
                r22(g1Type)
            }
            else -> {
                g1Type = Wrapper()
                g(g1Type)
            }
        }
        //30
        r30(fType, g1Type)
        val op = arrayOf<TokenType?>(TokenType.MINUS, TokenType.OR, TokenType.PLUS)
        while (op.contains(token!!.value)) {
            var g2Type: Wrapper<DataType>
            when {
                token!!.value === TokenType.PLUS -> {
                    matchToken(TokenType.PLUS)
                    //24
                    r24(fType)
                    g2Type = Wrapper()
                    g(g2Type)
                    //25
                    r25(fType, g2Type)
                }
                token!!.value === TokenType.MINUS -> {
                    matchToken(TokenType.MINUS)
                    //21
                    r21(fType)
                    g2Type = Wrapper()
                    g(g2Type)
                    //27
                    r27(fType, g2Type)
                }
                else -> {
                    matchToken(TokenType.OR)
                    //28
                    r28(fType)
                    g2Type = Wrapper()
                    g(g2Type)
                    //28
                    r28(g2Type)
                }
            }
        }
    }

    /**
     * G  =>  H{(* H|/ H|and H)}
     */
    private fun g(gType: Wrapper<DataType>) {
        val h1Type = Wrapper<DataType>()
        h(h1Type)
        //30
        r30(gType, h1Type)
        val op = arrayOf<TokenType?>(TokenType.AND, TokenType.DIVIDE, TokenType.MULTIPLY)
        while (op.contains(token!!.value)) {
            when {
                token!!.value === TokenType.MULTIPLY -> {
                    matchToken(TokenType.MULTIPLY)
                    //21
                    r21(gType)
                    val h2Type = Wrapper<DataType>()
                    h(h2Type)
                    //27
                    r27(gType, h2Type)
                }
                token!!.value === TokenType.DIVIDE -> {
                    matchToken(TokenType.DIVIDE)
                    //33
                    r33(gType)
                    val h2Type = Wrapper<DataType>()
                    h(h2Type)
                    //33
                    r33(h2Type)
                }
                else -> {
                    matchToken(TokenType.AND)
                    //28
                    r28(gType)
                    val h2Type = Wrapper<DataType>()
                    h(h2Type)
                    //28
                    r28(h2Type)
                }
            }
        }
    }

    /**
     * H  =>  id|
     * constant|
     * "("E")"|
     * not H
     */
    private fun h(hType: Wrapper<DataType>) {
        when {
            token!!.value === TokenType.ID -> {
                matchToken(TokenType.ID)
                //37
                val id = matchedToken as TokenID?
                r37(hType, id)
            }
            token!!.value === TokenType.CONSTANT -> {
                matchToken(TokenType.CONSTANT)
                //6
                val constant = matchedToken as TokenConstant?
                r6(constant)
                //38
                r38(hType, constant)
            }
            token!!.value === TokenType.OPEN_BRACE -> {
                matchToken(TokenType.OPEN_BRACE)
                val eType = Wrapper<DataType>()
                e(eType)
                //30
                r30(hType, eType)
                matchToken(TokenType.CLOSE_BRACE)
            }
            else -> {
                matchToken(TokenType.NOT)
                val h1Type = Wrapper<DataType>()
                h(h1Type)
                //40
                r40(hType, h1Type)
            }
        }
    }

    /**
     * L  =>  C|
     * begin {C} end
     */
    private fun l() {
        if (token!!.value === TokenType.BEGIN) {
            matchToken(TokenType.BEGIN)
            while (FIRST_C.contains(token!!.value)) {
                c()
            }
            matchToken(TokenType.END)
        } else {
            c()
        }
    }

    private fun r5(id: TokenID?) {
        if (id!!.klass == null) {
            id.klass = Class.CONST
        } else {
            errorIdDuplicated()
        }
    }

    private fun r6(constant: TokenConstant?) {
        if (constant!!.type === DataType.INTEGER) {
            val constantVal = constant!!.constant as Short
            if (constantVal in 0..255) {
                constant.type = DataType.BYTE
            }
        }
    }

    private fun r7(constant: TokenConstant?) {
        if (!INTEGER_CONSTANTS.contains(constant!!.type)) {
            errorIncompatibleType()
        } else {
            constant.type = DataType.INTEGER
        }
    }

    private fun r8(id: TokenID?, pType: Wrapper<DataType>) {
        id!!.type = pType.value
    }

    private fun r11(mType: DataType, id: TokenID?) {
        id!!.type = mType
        if (id.klass == null) {
            id.klass = Class.VAR
        } else {
            errorIdDuplicated()
        }
    }

    private fun r12(id: TokenID?, pType: Wrapper<DataType>) {
        if (id!!.type !== pType.value && (id!!.type !== DataType.INTEGER || DataType.BYTE !== pType.value)) {
            errorIncompatibleType()
        }
    }

    private fun r13(id: TokenID?) {
        when {
            id!!.klass == null -> {
                errorIdNotDeclared()
            }
            id.klass === Class.CONST -> {
                errorIncompatibleClass()
            }
        }
    }

    private fun r14(id: TokenID?, eType: Wrapper<DataType>) {
        if (id!!.type !== eType.value && (id!!.type !== DataType.INTEGER || eType.value !== DataType.BYTE)) {
            errorIncompatibleType()
        }
    }

    private fun r17(eType: Wrapper<DataType>, f2Type: Wrapper<DataType>) {
        when (eType.value) {
            DataType.INTEGER, DataType.BYTE -> if (f2Type.value !== DataType.INTEGER && f2Type.value !== DataType.BYTE) {
                errorIncompatibleType()
            }
            DataType.STRING -> if (f2Type.value !== DataType.STRING) {
                errorIncompatibleType()
            }
            DataType.BOOLEAN -> if (f2Type.value !== DataType.BOOLEAN) {
                errorIncompatibleType()
            }
        }
    }

    private fun r18(eType: Wrapper<DataType>, f2Type: Wrapper<DataType>) {
        when (eType.value) {
            DataType.INTEGER, DataType.BYTE -> if (f2Type.value !== DataType.INTEGER && f2Type.value !== DataType.BYTE) {
                errorIncompatibleType()
            }
            DataType.STRING -> errorIncompatibleType()
            DataType.BOOLEAN -> if (f2Type.value !== DataType.BOOLEAN) {
                errorIncompatibleType()
            }
        }
    }

    private fun r19(eType: Wrapper<DataType>, f2Type: Wrapper<DataType>) {
        when (eType.value) {
            DataType.INTEGER, DataType.BYTE -> if (f2Type.value !== DataType.INTEGER && f2Type.value !== DataType.BYTE) {
                errorIncompatibleType()
            }
            DataType.STRING, DataType.BOOLEAN -> errorIncompatibleType()
        }
    }

    private fun r20(eType: Wrapper<DataType>) {
        eType.value = DataType.BOOLEAN
    }

    private fun r21(type: Wrapper<DataType>) {
        if (!INTEGER_CONSTANTS.contains(type.value)) {
            errorIncompatibleType()
        }
    }

    private fun r22(g1Type: Wrapper<DataType>) {
        if (g1Type.value === DataType.BYTE) {
            g1Type.value = DataType.INTEGER
        } else if (g1Type.value !== DataType.INTEGER) {
            errorIncompatibleType()
        }
    }

    private fun r24(fType: Wrapper<DataType>) {
        if (!INTEGER_CONSTANTS.contains(fType.value) && fType.value !== DataType.STRING) {
            errorIncompatibleType()
        }
    }

    private fun r25(fType: Wrapper<DataType>, g2Type: Wrapper<DataType>) {
        if (INTEGER_CONSTANTS.contains(fType.value)) {
            if (!INTEGER_CONSTANTS.contains(g2Type.value)) {
                errorIncompatibleType()
            } else if (g2Type.value === DataType.INTEGER) {
                fType.value = DataType.INTEGER
            }
        } else if (g2Type.value !== DataType.STRING) {
            errorIncompatibleType()
        }
    }



    private fun r27(type1: Wrapper<DataType>, type2: Wrapper<DataType>) {
        if (!INTEGER_CONSTANTS.contains(type2.value)) {
            errorIncompatibleType()
        } else if (type2.value === DataType.INTEGER) {
            type1.value = DataType.INTEGER
        }
    }

    private fun r28(type: Wrapper<DataType>) {
        if (type.value !== DataType.BOOLEAN) {
            errorIncompatibleType()
        }
    }

    private fun r30(type1: Wrapper<DataType>, type2: Wrapper<DataType>) {
        type1.value = type2.value
    }

    private fun r33(type: Wrapper<DataType>) {
        if (type.value === DataType.BYTE) {
            type.value = DataType.INTEGER
        } else if (type.value !== DataType.INTEGER) {
            errorIncompatibleType()
        }
    }

    private fun r37(hType: Wrapper<DataType>, id: TokenID?) {
        if (id!!.klass == null) {
            errorIdNotDeclared()
        } else {
            hType.value = id.type
        }
    }

    private fun r38(hType: Wrapper<DataType>, constant: TokenConstant?) {
        hType.value = constant!!.type
    }

    private fun r40(hType: Wrapper<DataType>, h1Type: Wrapper<DataType>) {
        if (h1Type.value !== DataType.BOOLEAN) {
            errorIncompatibleType()
        } else {
            hType.value = DataType.BOOLEAN
        }
    }

    private fun r41(pType: Wrapper<DataType>, constant: TokenConstant?) {
        pType.value = constant!!.type
    }

    private fun r42(eType: Wrapper<DataType>){
        if(eType.value === DataType.BOOLEAN){
            errorIncompatibleType()
        }
    }

    private fun r43(id: TokenID?) {
        when {
            id!!.klass == null -> {
                errorIdNotDeclared()
            }
            id.klass === Class.CONST -> {
                errorIncompatibleClass()
            }
            id.type === DataType.BOOLEAN -> {
                errorIncompatibleType()
            }
        }
    }



    private fun errorEndOfFile() {
        println("${lexer.line}:fim de arquivo nao esperado.")
        exitProcess(1)
    }

    private fun errorIncompatibleClass() {
        println("${lexer.line}:classe de identificador incompativel [${matchedToken!!.key}]")
        exitProcess(1)
    }

    private fun errorTokenUnexpected() {
        println("${lexer.line}:token nao esperado [${token!!.key}]")
        exitProcess(1)
    }

    private fun errorIdDuplicated() {
        println("${lexer.line}:identificador ja declarado [${matchedToken!!.key}]\n")
        exitProcess(1)
    }

    private fun errorIncompatibleType() {
        println("${lexer.line}:tipos incompativeis ")
        exitProcess(1)
    }

    private fun errorIdNotDeclared() {
        println("${lexer.line}:identificador nao declarado [${matchedToken!!.key}]")
        exitProcess(1)
    }

}