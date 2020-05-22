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

    // End of file token
    EOF
}