package com.craftinginterpreters.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
    static boolean hadError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            System.out.println("No file provided. Running prompt.");
            runPrompt();
        }
    }

    // Runs program from a file
    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path)); // Read all bytes from file
        run(new String(bytes, Charset.defaultCharset())); // Run the program

        // Indicate error in the code
        if (hadError)
            System.exit(65);
    }

    // Runs program line-by-line from a REPL
    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print("lox > ");
            String line = reader.readLine();
            if (line == null)
                break;
            run(line);

            // Don't kill session if there is an error
            hadError = false;
        }
    }

    // Runs program for real :)
    private static void run(String source) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        Parser parser = new Parser(tokens);
        Expr expression = parser.parse();

        // Stop if syntax error
        if (hadError) return;

        System.out.println(new AstPrinter().print(expression));
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }

    static void error(Token token, String message) {
        if (token.type == TokenType.EOF) {
            report(token.line, " at end", message);
        } else {
            report(token.line, " at '" + token.lexeme + "'", message);
        }
    }
}