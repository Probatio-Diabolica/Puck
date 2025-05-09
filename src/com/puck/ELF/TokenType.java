package com.puck.ELF;

public enum TokenType {
    // Single-character tokens.
    LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
    COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,

    // Special Operators
    



    
    // One or two character tokens. 
    BANG, BANG_EQUAL /* equivalent ot  !=  */,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,
    
    // Literals.
    IDENTIFIER, STRING, NUMBER,
    
    // Keywords.
    AND, OR, 
    WHILE, FOR, 
    CLASS, SUPER, THIS, 
    VAR, FUNC, 
    IF, ELSE,  
    RETURN,

    // reverved Vals
    TRUE, FALSE , NIL,
    //statements
    PRINT,  
 
    //END of FILE
    EOF
}
