package utils

/**
 * Classe usada para passar por referencia os atributos do esquema de traducao;
 * Guarda um valor dentro.
 *
 * @param <T> tipo do dado
 */
data class Wrapper<T>(var value: T? = null)