package data_structure.registry

import data_structure.token.TokenConstant

/**
 * Registro lexico. Implementado como uma hash que mapeia o lexema no token de constante.
 */
object LexicalRegister : HashMap<String, TokenConstant>(){

    /**
     * Converte o registro lexico para string - para permitir a visualizacao dos conteudos do mesmo.
     */
    override fun toString(): String {
        val s = StringBuilder("${this::class.simpleName} {\n")
        this.forEach { _, token -> s.appendln("$token") }
        s.appendln("}\n")
        return s.toString()
    }
}