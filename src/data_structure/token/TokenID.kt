package data_structure.token

import data_structure.token.Class
import data_structure.token.DataType
import data_structure.token.Token
import data_structure.token.TokenType

/**
 * Subclasse de data_structure.token.Token para identificadores, contendo o tipo e a classe do ID
 */
class TokenID(key: String, var type: DataType? = null, var klass: Class? = null) : Token(key, TokenType.ID) {
    override fun toString(): String = "<\"$key\", ${value.name}, ${klass?.name}, ${type?.name}>"
}