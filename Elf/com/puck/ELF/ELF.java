package com.puck.ELF;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

class ELF{
    static boolean hadError=false;
    private static void run(String source)
    {
        ScannerParser scanner = new ScannerParser(source);
        List<Token> tokens = scanner.getScans();
        for(Token token : tokens)
        {
            System.out.println(token);
        }
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
        if(hadError) System.exit(65);
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
}