package com.puck.ELF;

import java.util.ArrayList;
import java.util.List;
import static com.puck.ELF.TokenType.*;
import com.puck.ELF.Expr.*;
// import com.puck.ELF.Expr.Statement;

// This parser really has two jobs:
// 1. Given a valid sequence of tokens, produce a corresponding syntax tree.
// 2. Given an invalid sequence of tokens, detect any errors and tell the user about their mistakes.
public class Parser {

    private static class ParseError extends RuntimeException{}; //return the parseError

    private final List<Token> tokens;
    private int current = 0;
    
    private Statement declaration() {
        try {
            if (match(FUNC)) return function("function");
            if (match(VAR)) return varDeclaration();
    
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }
    Parser(List<Token> tokens){
        this.tokens = tokens;
    }

    Expr parse(){
        try{
            return expression();
        }catch(ParseError error){
            return null;
        }
    }

    private Expr expression()
    {
        return equality();
    }

    private Statement statement(){
        if(match(PRINT)) return printStatement();
        return expressionStatement();
    }

    // equality -> comparison ( ( "!=" | "==" ) comparison )* ;
    private Expr equality()
    {
        Expr expr = comparasion();
        while (match(BANG_EQUAL,EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparasion();
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
        Expr expr = term();

        while(match(GREATER,GREATER_EQUAL,LESS,LESS_EQUAL))
        {
            Token operToken = previous();
            Expr right = term();
            expr = new Expr.Binary(expr,operToken,right); 
        }
        return expr;
    }

    ////helper functions/////////////////////////////////


    private Token peek() {
        return tokens.get(current);
    }

    private boolean isAtEnd() {
        return peek().type==EOF;
    }

    //previous token
    private Token previous() {
        return tokens.get(current-1);
    }

    // order of precedence for addition and subtractions
    private Expr term(){
        Expr expr = factor();

        while(match(MINUS,PLUS)){
            Token opToken = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr,opToken,right);
        }
        return expr;
    }

    //order of precedence for multiplication and division
    private Expr factor(){
        Expr expr = unary();

        while(match(SLASH,STAR)){
            Token opToken = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr,opToken,right);
        }
        return expr;
    }

    private Expr unary(){
        //*  BANG = '!' || MINUS = '-' 
        if(match(BANG,MINUS)){
            Token opToken = previous();
            // Expr left = unary();
            Expr right = unary();
            return new Expr.Unary(opToken,right); 
        }

        return call();
        // return primary();
    }

    private Expr call() {
        Expr expr = primary();
        while(true){
            if(match(LEFT_PAREN)){
                expr = finishCall(expr);
            }else{
                break;
            }
        }
        return expr;
    }

    private Expr finishCall(Expr callee) {
        List<Expr> args = new ArrayList<>();
        if(!check(RIGHT_PAREN)){
            
            do{
                if(args.size()>=255) error(peek(),"Can't have more than 255 params");
                args.add(expression());
            }while(match(COMMA));
        }

        Token paren = consume(RIGHT_PAREN, "Missing ( after args");
        return Expr.Call(callee,paren,args);
    }

    //R- values only
    private Expr primary(){
        if(match(FALSE)) return new Expr.Literal(false);
        if(match(TRUE))  return new Expr.Literal(true);
        if(match(NIL))   return new Expr.Literal(null);

        if(match(NUMBER,STRING)){
            return new Expr.Literal(previous().literal);
        }

        if(match(LEFT_PAREN)){
            Expr expr = expression();
            consume(RIGHT_PAREN,"Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
        throw error(peek(),"Expect expression.");
    }

    //!! Error Handlers
    private Token consume(TokenType type, String message){
        if(check(type)) return advance();
        throw error(peek(),message);
    }

    private ParseError error(Token token, String message){
        ELF.error(token, message);
        return new ParseError();
    }
    
    // It discards tokens until it thinks it found a statement boundary.
    private void synchronize(){
        advance();
        while(!isAtEnd()){
            if(previous().type== SEMICOLON) return;
            switch(peek().type){
                case CLASS:
                case FUNC:
                case VAR: 
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
                default:
            }
            advance();
        }
    }


    private Statement printStatement(){
        Expr value = expression();
        consume(SEMICOLON,"Expect ; after value.");
        return new Statement.Print(value);
    }

    private Statement expressionStatement(){
        Expr expr = expression();
        consume(SEMICOLON,"Expect ':' after expression");
        return new Statement.Expression(expr);
    }

}
