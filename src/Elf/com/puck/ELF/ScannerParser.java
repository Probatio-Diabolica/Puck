package com.puck.ELF;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// SCANNER class for scanning all the inputs b
class ScannerParser {
    private final String source;
    private final List<Token> tokens=new ArrayList<>();
    private static final Map<String, TokenType> keywords;
    
    static{
        keywords = new HashMap<>();
        keywords.put("and"      , TokenType.AND );
        keywords.put("class"    , TokenType.CLASS );
        keywords.put("else"     , TokenType.ELSE );
        keywords.put("false"    , TokenType.FALSE );
        keywords.put("for"      , TokenType.FOR );
        keywords.put("fun"      , TokenType.FUNC );
        keywords.put("if"       , TokenType.IF );
        keywords.put("nil"      , TokenType.NIL );
        keywords.put("or"       , TokenType.OR );
        keywords.put("print"    , TokenType.PRINT );
        keywords.put("return"   , TokenType.RETURN );
        keywords.put("super"    , TokenType.SUPER );
        keywords.put("this"     , TokenType.THIS );
        keywords.put("true"     , TokenType.TRUE );
        keywords.put("var"      , TokenType.VAR );
        keywords.put("while"    , TokenType.WHILE );
    }

    private int start   = 0;
    private int current = 0;
    private int line    = 0;

    ScannerParser(String source){
        this.source = source;
    }

    List<Token> scanTokensFromList(){
        
        while(isAtEnd()){
            start=current;
            scanTokensFromList();
        }
        
        tokens.add(new Token(TokenType.EOF,"",null,line));
        return tokens;
    }
        
    public List<Token> getScans(){
        return this.tokens;
    }

    private void scanTokens(){
        char c= advance();
        switch(c){
            case '(':
                addToken(TokenType.LEFT_PAREN);
                break;
            case ')':
                addToken(TokenType.RIGHT_PAREN);
                break;
            case '{':
                addToken(TokenType.LEFT_BRACE);
                break;
            case '}':
                addToken(TokenType.RIGHT_BRACE);
                break;
            case ',':
                addToken(TokenType.COMMA);
                break;
            case '.':
                addToken(TokenType.DOT);
                break;
            case '-':
                addToken(TokenType.MINUS);
                break;
            case '+':
                addToken(TokenType.PLUS);
                break;
            case ';':
                addToken(TokenType.SEMICOLON);
                break;
            case '*':
                addToken(TokenType.STAR);
                break;
            case '!': addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
                break;
            case '=': addToken(match('=') ? TokenType.EQUAL_EQUAL  : TokenType.EQUAL);
                break;
            case '<': addToken(match('=') ? TokenType.LESS_EQUAL  : TokenType.EQUAL); 
                break;
            case '>': addToken(match('>') ? TokenType.GREATER_EQUAL : TokenType.GREATER);		  
                    break;
            case '/':
                if(match('/')){
                    while(peek() != '\n' && !isAtEnd()) advance();
                }else{
                    addToken(TokenType.SLASH);
                }
                break;
            case ' '  :
            case '\r' :
            case '\t' :
                break;
            case '\n':
                line++;
                break;
            case '"': string();break;
            default:
                if(isDigit(c)){
                    number();
                }else if(isAlpha(c)){
                    identifier();
                }
                
                else{
                    ELF.error(line,"unexpected error");
                }
                break;
        }
    }

    //// Looking for identifiers
    private void identifier(){

        //!! scanning the identifier
        while(isAlphaNumeric(peek())) advance();

        //!! checking if it exists in the map or not
        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if(type == null) type=TokenType.IDENTIFIER;
        addToken(TokenType.IDENTIFIER);
    }
    ////Alphabets
    private boolean isAlpha(char c){
        return (c>='a' && c<='z') // checks for small caps `
            || (c>='A' && c<='Z') //` checks for big caps
            || (c=='_');          // 
    }
    //// Alpha numerics aka either numeric or alphabet
    private boolean isAlphaNumeric(char c){
        return isDigit(c) || isAlpha(c);
    }
    
    ////STRING CHECKER
    private void string(){
        while(peek() != '"' && !isAtEnd() ){
            if(peek() == '\n') line++;
            advance();
        }
        //!! If STRING is unterminated, or doesn't have " " , then it means we have missed one double quote 
        if(isAtEnd()){
            ELF.error(line,"Unterminated string");
            return;
        }
        advance();
        String value = source.substring(start + 1, current -1);
        addToken(TokenType.STRING, value); // STRING is an ENUM of ENUM-Type	
    }

    ////Number checker
    private void number(){
        while(isDigit(peek())) advance();

        //looking for fractional part
        if(peek()=='.' && isDigit(peekNext())){
            advance();
            while(isDigit(peek())) advance();
        }
        addToken(TokenType.NUMBER,Double.parseDouble(source.substring(start,current)));
    }
    ////checks if the a character is a digit
    private boolean isDigit(char c)
    {
        return c<='9' && c >='0';
    } 


    private char peekNext(){
        if(current+1 >= source.length()) return '\0';
        return source.charAt(current+1);
    }

    

    //Peek will look into our code in ELF
    private char peek(){
    	if(isAtEnd()) return '\0';
        return source.charAt(current);
    }

    // match will 
    private boolean match(char expected){
    	if(isAtEnd()) return false;
        if(source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private void addToken(TokenType type){
        addToken(type,null);
    }

    private void addToken(TokenType type,Object literal){
        String text = this.source.substring(start,current);
        tokens.add(new Token(type,text,literal,line));
    }

    private char advance(){
        return 'a';
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

}


