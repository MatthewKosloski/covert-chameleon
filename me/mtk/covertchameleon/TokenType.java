package me.mtk.covertchameleon;

enum TokenType 
{
    // Grouping tokens
    LPAREN, RPAREN, LBRACKET, RBRACKET,

    // Binary arithmetic tokens
    PLUS, MINUS, STAR,

    // Unary logical negation operator
    NOT,
    
    // Comment and binary division token
    SLASH,

    // Literal token
    NUMBER,

    // Unidentified token,
    UNIDENTIFIED,

    // Print expressions
    PRINT, PRINTLN,

    // Let expression
    LET,

    // equal? and nequal? operators
    EQUAL_TO, NOT_EQUAL_TO,

    // > and >= operators
    GREATER_THAN, GREATER_THAN_OR_EQUAL_TO,

    // < and <= operators
    LESS_THAN, LESS_THAN_OR_EQUAL_TO,

    // Booleans
    TRUE, FALSE,

    // Null
    NULL,

    // Identifier (e.g., variable name)
    IDENTIFIER,
    
    // End of file token
    EOF
}