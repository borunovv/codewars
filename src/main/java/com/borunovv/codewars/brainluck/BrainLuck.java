package com.borunovv.codewars.mathexpr;

import java.util.*;

/**
 * Challenge URL: https://www.codewars.com/kata/526156943dfe7ce06200063e
 *
 * @author borunovv
 */
public class BrainLuck {
    private String code;
    private List<Character> memory = new ArrayList<Character>(1024);

    public BrainLuck(String code) {
        this.code = code;
        memory.add((char) 0);
    }

    public String process(String input) {
        StringBuilder out = new StringBuilder();
        int inputPtr = 0;
        int dataPtr = 0;
        int ip = 0;
        try {
            while (ip < code.length()) {
                char ch = code.charAt(ip);
                switch (ch) {
                    case '>': {
                        dataPtr++;
                        if (dataPtr == memory.size()) {
                            memory.add((char) 0);
                        }
                        break;
                    }
                    case '<': {
                        dataPtr--;
                        if (dataPtr < 0) throw new RuntimeException("Bad memory index: " + dataPtr);
                        break;
                    }
                    case '+': {
                        memory.set(dataPtr, (char) (((int) memory.get(dataPtr) + 1) % 256));
                        break;
                    }
                    case '-': {
                        memory.set(dataPtr, (char) (((int) memory.get(dataPtr) + 256 - 1) % 256));
                        break;
                    }
                    case '.': {
                        out.append(memory.get(dataPtr));
                        break;
                    }
                    case ',': {
                        if (inputPtr >= input.length()) throw new RuntimeException("Out of input.");
                        memory.set(dataPtr, input.charAt(inputPtr++));
                        break;
                    }
                    case '[': {
                        if (memory.get(dataPtr) == 0) {
                            ip = findMatchingCloseBlock(ip);
                        }
                        break;
                    }
                    case ']': {
                        if (memory.get(dataPtr) != 0) {
                            ip = findMatchingOpenBlock(ip);
                        }
                        break;
                    }
                    default: {
                        throw new RuntimeException("Unrecognized symbol '" + ch + "'");
                    }
                }

                ip++;
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage() + "\nCode: '" + code + "'\nInput: '" + input + "'", e);
        }
        return out.toString();
    }

    private int findMatchingCloseBlock(int from) {
        int result = -1;
        int level = 0;
        int index = from + 1;
        while (index < code.length()) {
            if (code.charAt(index) == ']') {
                if (level == 0) {
                    result = index;
                    break;
                } else {
                    level--;
                    if (level < 0) throw new RuntimeException("Error in code: '[' and ']' not matched");
                }
            } else if (code.charAt(index) == '[') {
                level++;
            }

            index++;
        }

        if (result == -1) {
            throw new RuntimeException("Can't find matching close block for '[' at position " + from);
        }
        return result;
    }

    private int findMatchingOpenBlock(int from) {
        int result = -1;
        int level = 0;
        int index = from - 1;
        while (index >= 0) {
            if (code.charAt(index) == '[') {
                if (level == 0) {
                    result = index;
                    break;
                } else {
                    level--;
                    if (level < 0) throw new RuntimeException("Error in code: '[' and ']' not matched");
                }
            } else if (code.charAt(index) == ']') {
                level++;
            }

            index--;
        }

        if (result == -1) {
            throw new RuntimeException("Can't find matching open block for ']' at position " + from);
        }
        return result;
    }
}