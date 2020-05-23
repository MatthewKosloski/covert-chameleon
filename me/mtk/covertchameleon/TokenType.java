package me.mtk.covertchameleon;

enum TokenType 
{
    // Grouping tokens
    LPAREN, RPAREN,

    // Binary arithmetic tokens
    PLUS, MINUS, STAR,
    
    // Comment and binary division token
    SLASH,

    // Literal token
    NUMBER,

    // Unidentified token,
    UNIDENTIFIED,

    // Print expression
    PRINT,

    // Let expression
    LET,

    // == and != operators
    EQUAL_TO, NOT_EQUAL_TO,

    // > and >= operators
    GREATER_THAN, GREATER_THAN_OR_EQUAL_TO,

    // < and <= operators
    LESS_THAN, LESS_THAN_OR_EQUAL_TO,

    // Booleans
    TRUE, FALSE,

    // Null
    NULL,
    
    // End of file token
    EOF
}