package com.borunovv.codewars.mathexpr;

/**
 * Challenge URL: https://www.codewars.com/kata/52a78825cdfc2cfc87000005
 *
 * @author borunovv
 */
public class Main {

    public static void main(String[] args) {
        double result = new MathEvaluator().calculate("1 + 2 * (3 - 4)");
        System.out.println(result);
    }
}
