package com.puck.ELF;
import java.util.HashMap;

public class Grammar {
    public final HashMap<Character, TokenType> grammar = new HashMap<>();
    
    Grammar(){
        //parens
        grammar.put('{', TokenType.LEFT_BRACE);
        grammar.put('}', TokenType.RIGHT_BRACE);
        grammar.put('(', TokenType.LEFT_PAREN);
        grammar.put(')', TokenType.RIGHT_PAREN);
        
        //ops
        grammar.put('-', TokenType.MINUS);
        grammar.put('+', TokenType.PLUS);
        grammar.put('/', TokenType.SLASH);
        grammar.put('*', TokenType.STAR);
        grammar.put(';', TokenType.SEMICOLON);

        //compares
        grammar.put('!', TokenType.BANG);
        grammar.put('=', TokenType.EQUAL);
        grammar.put('>', TokenType.GREATER);
        grammar.put('<', TokenType.LESS);
        
        //string
        grammar.put('"', TokenType.STRING);

        //others
        grammar.put(',', TokenType.COMMA);
        grammar.put('.', TokenType.DOT);
    }
}
