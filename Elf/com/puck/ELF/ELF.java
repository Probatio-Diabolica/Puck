package com.puck.ELF;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import com.puck.ELF.Expr.Statement;

class ELF{
    static List<Token> tokens = new java.util.ArrayList<>(List.of());
    private static final Interpreter machine = new Interpreter();
    private static boolean hadError=false;
    private static boolean hadRuntimeError = false;
    private static Grammar grammar = new Grammar();
    private static Keywords keywords = new Keywords();
    
    private static void run(String source)
    {
        List<Statement> statements = parseSourceFile(source, true);

        Interpreter machine = new Interpreter();
        
        machine.interpret(statements, true);
    }

    public static void  runPrompt() throws IOException
    {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);
        for(;;)
        {
            System.out.print(">");
            String  line = reader.readLine();
            if(line==null) break;
            run(line);
            hadError=false;
        }

    }
    
    public static void runFile(String fileString) throws IOException
    {
        byte[] bytes = Files.readAllBytes(Paths.get(fileString));
        run(new String(bytes,Charset.defaultCharset()));
        if(hadError) System.exit(65); // 65 shall be the syntax error code
        if(hadRuntimeError) System.exit(70); // 75 being the runTime code 
    }

    static void error(int line,String  message)
    {
        report(line,"",message);
    }
    
    private  static void report(int line , String Location,String Message)
    {
        System.err.println(
            "[ line : "+line +"] error "+ Location + ": " + Message
        );
        hadError = true;
    } 

    
    public static void main(String[] args) throws IOException
    {
        if(args.length<2)
        {
            System.out.println("[usage] elf <options> <filename>");
            System.exit(64);
        }

        String command = args[0];
        String filename = args[1];
        
        if (!filename.endsWith(".pck")) {
            System.err.println("[FILE ERROR] File Format not recognized. Must end with [.pck]. \n Exited with code 1");
            System.exit(1);
        }
          
        switch (command) {
            case "tokenize":
            tokenizeFile(filename, true);
            break;
            case "parse":
            parseSourceFile(filename, true);
            break;
            case "evaluate":
            interpretFileNew(filename, true);
            if (hadRuntimeError) System.exit(70);
            break;
            case "run":
            run(filename);
            if (hadRuntimeError) System.exit(70);
            break;
            default:
            System.err.println("Unknown command: " + command);
            System.exit(1);
        }
    }

    private static void interpretFileNew(String filename, boolean print) {

        List<Statement> statements = parseSourceFile(filename, false);
    
        Interpreter interpreter = new Interpreter();
        interpreter.interpret(statements, print);
    }
    private static List<Statement> parseSourceFile(String filename, boolean print) {

        boolean hadError = tokenizeFile(filename, false);
        // Stop if there was a syntax error.
        if (hadError) return null;
        ListParser parser = new ListParser(tokens);
        //    Expr expression = parser.parse();
    
        List<Statement> statements = parser.parse();
        if(statements.getFirst() == null) System.exit(65);
    
            return statements;
        }

        private static boolean tokenizeFile(String filename, boolean print) {
        String fileContents = "";

        try {
            fileContents = Files.readString(Path.of(filename));
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(1);
        }


        if (!fileContents.isEmpty()) {

            boolean syntaxError = false;
            String token;
            TokenType tokenType;
            Character curChar;

            int lineNo = 1;

            for(int curInd = 0; curInd<fileContents.length(); curInd++){
                curChar = fileContents.charAt(curInd);

                if(Character.isWhitespace(curChar) ||
                        Character.isSpaceChar(curChar)){
                if(curChar.equals('\n')) lineNo++;
                continue;
                }

                token = String.valueOf(curChar);

            if(grammar.grammar.containsKey(curChar))
            {

                tokenType = grammar.grammar.get(curChar);

                if (fileContents.startsWith("//", curInd)){
                    if(fileContents.indexOf("\n", curInd)!=-1){
                        curInd = fileContents.indexOf("\n", curInd);
                        lineNo+=1;
                        continue;
                    }
                    break;
                }

                Token twoCharToken = checkForTwoCharToken(fileContents, curInd, lineNo);

                if(twoCharToken!=null){
                    tokens.add(twoCharToken);
                    curInd++;
                }
                else if(curChar.equals('"')){

                        int strLiteralEndInd = fileContents.indexOf('"', curInd+1);
                    if(strLiteralEndInd == -1){
                        System.err.println("[line "+lineNo+"] Error: Unterminated string.");
                        syntaxError = true;
                        break;
                    }

                    tokens.add(new Token(TokenType.STRING,
                            fileContents.substring(curInd, strLiteralEndInd+1),
                            fileContents.substring(curInd+1, strLiteralEndInd),
                            lineNo));
                    curInd = strLiteralEndInd;

                }
                else{
                        tokens.add(new Token(tokenType, token, null, lineNo));
                }
            }

            else if(Character.isDigit(curChar)){
                curInd = tokenizeDigits(curInd, curChar, fileContents, lineNo);
            }
            
            else if(Character.isAlphabetic(curChar) || curChar.equals('_')){
                curInd = tokenizeWords(curInd, curChar, fileContents, lineNo);
            }
            
            else{
                System.err.println("[line "+lineNo+"] Error: Unexpected character: "+token);
                syntaxError = true;
            }
        }
        
            // Print the current tokens
            for(Token curToken: tokens){
                if(print) System.out.println(curToken.toString());
            }

            tokens.add(new Token(TokenType.EOF, "", null, lineNo));
            
            if(print) System.out.println("EOF  null");

            if(syntaxError) System.exit(65);
            
            return syntaxError;
        } 
        else 
        {
            if(print) System.out.println("EOF  null"); // Placeholder, remove this line when implementing the scanner
            return true;
        }

    }

    private static int tokenizeDigits(int curInd, Character curChar, String fileContents, int lineNo) {
        int startInd = curInd;
        while(Character.isDigit(curChar) || curChar.equals('.')){
            curInd++;
            if(curInd>=fileContents.length()) break;
            curChar = fileContents.charAt(curInd);
        }
    
        String num = fileContents.substring(startInd, curInd);
        tokens.add(new Token(TokenType.NUMBER,num, Double.parseDouble(num), lineNo) );
        curInd--;
        return curInd;
    }

    private static int tokenizeWords(int curInd, Character curChar, String fileContents, int lineNo) {
        int startInd = curInd;
        while(Character.isAlphabetic(curChar) || curChar.equals('_') || Character.isDigit(curChar)){
          curInd++;
          if(curInd>=fileContents.length()) break;
          curChar = fileContents.charAt(curInd);
        }
        String variable = fileContents.substring(startInd, curInd);
        tokens.add(new Token(keywords.keywords.getOrDefault(variable, TokenType.IDENTIFIER),
                variable, null, lineNo));
        curInd--;
        return curInd;
      }

    private static Token checkForTwoCharToken(String fileContents, int curIdx, int lineNo) {
        if((curIdx+1) < fileContents.length()){

            if(fileContents.startsWith("!=", curIdx)){
                return new Token(TokenType.BANG_EQUAL, "!=", null, lineNo);
            } else if (fileContents.startsWith("<=", curIdx)){
                return new Token(TokenType.LESS_EQUAL, "<=", null, lineNo);
            }
            else if (fileContents.startsWith(">=", curIdx)){
                return new Token(TokenType.GREATER_EQUAL, ">=", null, lineNo);
            }
            else if (fileContents.startsWith("==", curIdx)){
                return new Token(TokenType.EQUAL_EQUAL, "==", null, lineNo);
            }
        }
            return null;
    }

    public static void error(Token token, String message) {
        if(token.type == TokenType.EOF){
            report(token.line, "at end" , message);
        }
        else{
            report(token.line," at '" + token.lexem + "'", message);
        }
    }

    public static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() + "\n[line"+error.token.line + "]");
        hadRuntimeError = true;
    }
}