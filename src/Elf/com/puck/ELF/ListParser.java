package com.puck.ELF;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.puck.ELF.Expr.Expr;
import com.puck.ELF.Expr.Statement;

import static com.puck.ELF.TokenType.*;
import com.puck.ELF.Expr.*;


public class ListParser {
    private static class ParseError extends RuntimeException {}
    private final List<Token> tokens;
    private int current = 0;

    ListParser(List<Token> tokens) {
        this.tokens = tokens;
    }


     List<Statement> parse() {
        List<Statement> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }


    //Each method here is a rule in the grammar
    private Expr expression() {
        return assignment();
    }


    private Statement.Function function(String kind)
    {
        Token name = consume(IDENTIFIER, "Expect "+ kind + " name.");
        consume(LEFT_PAREN,"Expect '(' after" + kind + "name.");
        List<Token> params = new ArrayList<>();
        
        //!! PARSING PARAMETERS
        if(!check(RIGHT_PAREN)){
            do{
                if(params.size() >=255){
                    error(peek(), "PARAM LIMIT EXCEEDED");
                }
                params.add(
                consume(IDENTIFIER,"EXPECT PARAM NAME")
                );
            }while(match(COMMA));
        }
        consume(RIGHT_PAREN,"EXPECT ')' after parameters" );
        
        //PARSING the body
        consume(LEFT_BRACE,"Expect '{' before " + kind + " body.");
        List<Statement> body = block();
        return new Statement.Function(name,params,body);

    }

    private Expr assignment() {
        Expr expr = or();
//        Expr expr = equality();

        if (match(TokenType.EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable)expr).name;
                return new Expr.Assign(name, value);
            }

            error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr or() {
        Expr expr = and();

        while (match(TokenType.OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr and() {
        Expr expr = equality();

        while (match(TokenType.AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Statement declaration() {
        try {
            if (match(TokenType.VAR)) return varDeclaration();
            if(match(TokenType.FUNC)) return function("function");
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }
    private Statement varDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if (match(TokenType.EQUAL)) {
            initializer = expression();
        }

        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.");
        return new Statement.Var(name, initializer);
    }


    private Statement statement() {
        if (match(TokenType.FOR)) return forStatement();
        if (match(TokenType.IF)) return ifStatement();
        if (match(TokenType.PRINT)) return printStatement();
        if (match(TokenType.RETURN)) return returnStatement();  
        if (match(TokenType.WHILE)) return whileStatement();
        if (match(TokenType.LEFT_BRACE)) return new Statement.Block(block());
        return expressionStatement();
    }

    private Statement forStatement() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'for'.");
        Statement initializer;
        if (match(TokenType.SEMICOLON)) {
            initializer = null;
        } else if (match(TokenType.VAR)) {
            initializer = varDeclaration();
        } else {
            initializer = expressionStatement();
        }
        Expr condition = null;
        if (!check(TokenType.SEMICOLON)) {
            condition = expression();
        }
        consume(TokenType.SEMICOLON, "Expect ';' after loop condition.");
        Expr increment = null;
        if (!check(TokenType.RIGHT_PAREN)) {
            increment = expression();
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after for clauses.");

        Statement body = statement();

        if (increment != null) {
            body = new Statement.Block(
                    Arrays.asList(
                            body,
                            new Statement.Expression(increment)));
        }

        if (condition == null) condition = new Expr.Literal(true);
        body = new Statement.Loop(condition, body);

        if (initializer != null) {
            body = new Statement.Block(Arrays.asList(initializer, body));
        }


        return body;
    }

    private Statement whileStatement() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'while'.");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expect ')' after condition.");
        Statement body = statement();

        return new Statement.Loop(condition, body);
    }

    private Statement ifStatement() {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'if'.");
        Expr condition = expression();
        consume(TokenType.RIGHT_PAREN, "Expect ')' after if condition.");

        Statement thenBranch = statement();
        Statement elseBranch = null;
        if (match(TokenType.ELSE)) {
            elseBranch = statement();
        }

        return new Statement.Conditionals(condition, thenBranch, elseBranch);
    }

    private Statement expressionStatement() {
        Expr expr = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after expression.");
        return new Statement.Expression(expr);
    }

    private List<Statement> block() {
        List<Statement> statements = new ArrayList<>();

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    private Statement printStatement() {
        Expr value = expression();
        consume(TokenType.SEMICOLON, "Expect ';' after value.");
        return new Statement.Print(value);
    }

    private Statement returnStatement() {
        Token keyword = previous();
        Expr value = null;
        if (!check(SEMICOLON)) {
        value = expression();
        }
        consume(SEMICOLON, "Expect ';' after return value.");
        return new Statement.Return(keyword, value);
    }



    // equality -> comparison ( ( "!=" | "==" ) comparison )*
    private Expr equality() {
        Expr expr = comparison();

        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    // comparison  -> term ( ( ">" | ">=" | "<" | "<=" ) term )*
    private Expr comparison() {
        Expr expr = term();

        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(TokenType.MINUS, TokenType.PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(TokenType.SLASH, TokenType.STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return call();
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

    private Expr primary() {
        if (match(TokenType.FALSE)) return new Expr.Literal(false);
        if (match(TokenType.TRUE)) return new Expr.Literal(true);
        if (match(TokenType.NIL)) return new Expr.Literal(null);

        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return new Expr.Literal(previous().literal);
        }
        if (match(TokenType.IDENTIFIER)) {
            return new Expr.Variable(previous());
        }
        if (match(TokenType.LEFT_PAREN)) {
            Expr expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }
//        return null;
        throw error(peek(), "Expect expression.");
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
//        Lox.error(token, message);
        if (token.type == TokenType.EOF) {
            System.err.println("[line "+token.line+ "] at end"+ message);
//            System.exit(70);
        } else {
            System.err.println("[line "+token.line+ "] at '" + token.lexem + "'"+ message);
        }
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == TokenType.SEMICOLON) return;

            switch (peek().type) {
                case CLASS:
                case FUNC:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }


    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }
}
