package data_structure.token

/**
 * Enumera todos os tipos de token de acordo com o alfabeto da linguagem
 */
enum class TokenType {
    ID,
    CONSTANT,
    MAIN,
    CONST,
    INTEGER,
    BYTE,
    STRING,
    BOOLEAN,
    WHILE,
    IF,
    ELSE,
    BEGIN,
    END,
    THEN,
    READLN,
    WRITE,
    WRITELN,
    AND,
    OR,
    NOT,
    LESS_OR_EQUAL,
    GREATER_OR_EQUAL,
    NOT_EQUAL,
    EQUAL,
    LESS,
    GREATER,
    PLUS,
    MINUS,
    MULTIPLY,
    DIVIDE,
    COMMA,
    SEMICOLON,
    OPEN_BRACE,
    CLOSE_BRACE,
    ASSIGN
}