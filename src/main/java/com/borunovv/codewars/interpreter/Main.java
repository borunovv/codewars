package com.borunovv.codewars.interpreter;

/**
 * Simple Interactive Interpreter
 * Challenge URL: https://www.codewars.com/kata/52ffcfa4aff455b3c2000750
 *
 * @author borunovv
 */
public class Main {

    public static void main(String[] args) {
        Interpreter interpreter = new Interpreter();
        while (true) {
            System.out.print("> ");
            String line = System.console().readLine();
            if (line.equals("exit"))
                break;

            try {
                Double result = interpreter.input(line);
                if (result != null) {
                    System.out.println(result);
                }
            } catch (Exception e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        }
    }
}
