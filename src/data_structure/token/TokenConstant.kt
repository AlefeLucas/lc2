package data_structure.token

import data_structure.token.DataType.*

/**
 * Subclasse de data_structure.token.Token para constantes, contendo o valor da constante e o tipo de constante
 */
class TokenConstant(override var key: String, var type: DataType) : Token(key, TokenType.CONSTANT) {

    val constant: Any

    /**
     * Inicializa o token com o respectivo tipo de constante
     */
    init {
        constant = when (type) {
            INTEGER -> key.toShort(10)
            BYTE -> getByte(key)
            STRING -> key.substring(1, key.length - 1).replace("''", "'", false)
            BOOLEAN -> key.toBoolean()
        }
    }

    private fun getByte(lexeme: String): Short {
        val s = lexeme.replace('h', 'x', true).toShort(16)
        if (s in 0..255) {
            return s
        } else {
            throw IllegalStateException("Erro")
        }
    }

    /**
     * Converte para string para permitir visualizacao do conteudo do token
     */
    override fun toString(): String = when (this.type) {
        STRING -> "<\"$key\", ${value.name}, \"$constant\", ${type.name}>"
        else -> "<\"$key\", ${value.name}, $constant, ${type.name}>"
    }

}