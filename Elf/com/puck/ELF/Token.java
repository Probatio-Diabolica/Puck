package com.puck.ELF;

//// class for tokens
public class Token {
    final   TokenType type;
    public final   String lexem;
    final   Object literal ;
    final   int line;

    Token(TokenType type,String lexems,Object literal,int line){
        this.type=type;
        this.lexem=lexems;
        this.literal=literal;
        this.line=line;
    }
    
    public String toString(){
        return type+" " +lexem+" "+literal;
    }
}

