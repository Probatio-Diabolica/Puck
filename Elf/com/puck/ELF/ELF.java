package com.puck.ELF;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import com.puck.ELF.Expr.AstPrinter;
import com.puck.ELF.Expr.Expr;

class ELF{
    private static final Interpreter machine = new Interpreter();
    private static boolean hadError=false;
    private static boolean hadRuntimeError = false;

    private static void run(String source)
    {
        // scanning
        ScannerParser scanner = new ScannerParser(source);
        List<Token> tokens = scanner.getScans();
        Parser parser = new Parser(tokens);
        Expr expression = parser.parse();
        
        
        //check for the existence of an error, if not then interpret
        if(hadError) return ;

        machine.interpret(expression);
        
        System.out.println(new AstPrinter().print(expression));
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
        if(args.length<1)
        {
            System.out.println("[usage] pck - script");
            System.exit(64);
        }
        else if(args.length == 1){
            runFile(args[0]);
        }
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