package data_structure.token

import data_structure.token.TokenType
import kotlin.collections.Map.Entry

/**
 * data_structure.token.Token - associacao entre o lexema e o tipo de token correspondente
 */
open class Token(override var key: String,  override var value: TokenType) : Entry<String, TokenType> {

    /**
     * Converte para string para permitir visualizacao do conteudo do token
     */
    override fun toString(): String = "<\"$key\", ${value.name}>"

}