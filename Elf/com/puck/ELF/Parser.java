package com.puck.ELF;

import java.util.List;
import static com.puck.ELF.TokenType.*;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;
    
    Parser(List<Token> tokens){
        this.tokens = tokens;
    }

    private Expr expression()
    {
        return equality();
    }

    // equality -> comparison ( ( "!=" | "==" ) comparison )* ;
    private Expr equality()
    {
        Expr expr = comparasion();
        while (match(BANG_EQUAL,EQUAL_EQUAL)) {
            token operator = previous();
            Exps right = comparasion();
            expr = new Expr.Binary(expr,operator,right);
        }
        return expr;
    }

    // This checks to see if the current token has any of the given types. If so, it
    // consumes the token and returns true . Otherwise, it returns false and leaves
    // the current token alone
    private boolean match(TokenType... types){
        for(TokenType type :types){
            if(check(type)){
                advance();
                return true;
            }
        }
        return false;
    }

    // returns true if the current token is of the given type.
    private boolean check(TokenType type) {
        if(isAtEnd()) return false;
        return peek().type == type;
    }



    // consumes the current token and returns it
    private Token advance() {
        if(!isAtEnd()) current++;
        return previous();
    }



    private Expr comparasion() {
        Expr expr;
        return expr;
    }

    //helper functions
    private Token peek() {
        return tokens.get(current);
    }

    private boolean isAtEnd() {
        return peek().type==EOF;
    }

    private Token previous() {
        return tokens.get(current-1);
    }
}
