package com.borunovv.codewars.brainluck;

import com.borunovv.codewars.mathexpr.BrainLuck;

/**
 * Challenge URL: https://www.codewars.com/kata/526156943dfe7ce06200063e
 *
 * @author borunovv
 */
public class Main {

    public static void main(String[] args) {
        String out = new BrainLuck(",.,.,.,.").process("abcd");
        System.out.println(out);
    }
}
