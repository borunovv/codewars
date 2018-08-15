package com.borunovv.codewars.screenlock;

import java.util.HashMap;
import java.util.Map;

/**
 * Challenge URL: http://www.codewars.com/kata/screen-locking-patterns
 *
 * @author borunovv
 */
public class ScreenLock {

    public static void main(String[] args) {
        int total = 0;
        for (char ch = 'A'; ch <= 'I'; ++ch) {
            for (int steps = 4; steps <= 9; ++steps) {
                StringBuilder sb = new StringBuilder();
                int count = new ScreenLock().calculateCombinations(ch, steps);
                total += count;
                sb.append(ch).append("(").append(steps).append(")=").append(count);
                System.out.println(sb.toString());
            }
        }
        System.out.println("Total: " + total);
    }


    private static Map<Character, char[]> board = new HashMap<>();
    private static Map<Character, char[]> passOver = new HashMap<>();

    static {
        board.put('A', new char[]{'B', 'D', 'E', 'F', 'H'});
        board.put('C', new char[]{'B', 'F', 'E', 'D', 'H'});
        board.put('I', new char[]{'F', 'H', 'E', 'D', 'B'});
        board.put('G', new char[]{'D', 'H', 'E', 'F', 'B'});
        board.put('B', new char[]{'A', 'C', 'E', 'D', 'F', 'G', 'I'});
        board.put('F', new char[]{'C', 'I', 'E', 'B', 'H', 'G', 'A'});
        board.put('H', new char[]{'G', 'I', 'E', 'D', 'F', 'C', 'A'});
        board.put('D', new char[]{'A', 'G', 'E', 'B', 'H', 'C', 'I'});
        board.put('E', new char[]{'A', 'B', 'C', 'D', 'F', 'G', 'H', 'I'});

        passOver.put('A', new char[]{'C', 'B',  'I', 'E',  'G', 'D'});
        passOver.put('C', new char[]{'A', 'B',  'G', 'E',  'I', 'F'});
        passOver.put('I', new char[]{'C', 'F',  'A', 'E',  'G', 'H'});
        passOver.put('G', new char[]{'A', 'D',  'C', 'E',  'I', 'H'});
        passOver.put('B', new char[]{'H', 'E'});
        passOver.put('H', new char[]{'B', 'E'});
        passOver.put('D', new char[]{'F', 'E'});
        passOver.put('F', new char[]{'D', 'E'});
    }

    public int calculateCombinations(char start, int length) {
        if (length <= 0 || length > 9) return 0;
        boolean[] visited = new boolean[9];
        return calc(visited, start, length);
    }

    private int calc(boolean[] visited, char start, int length) {
        if (length == 0) return 0;
        if (length == 1) return 1;
        int result = 0;
        visited[toIndex(start)] = true;
        char[] neighbours = board.get(start);
        for (int i = 0; i < neighbours.length; ++i) {
            if (!visited[toIndex(neighbours[i])]) {
                result += calc(visited, neighbours[i], length - 1); // recursion
            }
        }
        char[] passOvers = passOver.get(start);
        if (passOvers != null) {
            for (int i = 0; i < passOvers.length / 2; i++) {
                char target = passOvers[i * 2];
                char condition = passOvers[i * 2 + 1];
                if (visited[toIndex(condition)] && !visited[toIndex(target)]) {
                    result += calc(visited, target, length - 1); // recursion
                }
            }
        }

        visited[toIndex(start)] = false;
        return result;
    }

    private int toIndex(char ch) {
        return ch - 'A';
    }
}
