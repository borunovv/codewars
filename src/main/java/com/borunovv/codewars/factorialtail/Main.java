package com.borunovv.codewars.factorialtail;

import java.util.HashMap;
import java.util.Map;

/**
 * Challenge URL: https://www.codewars.com/kata/55c4eb777e07c13528000021
 *
 * @author borunovv
 */
public class Main {

    public static void main(String[] args) {
        System.out.println(countTailZeroes(16, 16));
    }


    private static int countTailZeroes(int factorial, int radix) {
        Map<Integer, Integer> radixFactors = factorize(radix);

        int[] factorValues = new int[radixFactors.size()];
        int[] factorCount = new int[radixFactors.size()];

        int index = 0;
        for (Integer factor : radixFactors.keySet()) {
            factorValues[index] = factor;
            factorCount[index] = 0;
            index++;
        }

        for (int i = 2; i <= factorial; ++i) {
            for (int j = 0; j < factorValues.length; ++j) {
                int factor = factorValues[j];
                int x = i;
                while (x > 1 && x % factor == 0) {
                    x /= factor;
                    factorCount[j]++;
                }
            }
        }

        int minRadixCount = -1;
        for (int i = 0; i < factorValues.length; i++) {
            int factor = factorValues[i];
            int factorCountPerRadix = radixFactors.get(factor);
            int radixCount = factorCount[i] / factorCountPerRadix;
            minRadixCount = minRadixCount < 0 ?
                    radixCount :
                    Math.min(minRadixCount, radixCount);
        }

        return minRadixCount;
    }


    private static Map<Integer, Integer> factorize(int x) {
        Map<Integer, Integer> factors = new HashMap<>();
        if (x <= 1) {
            factors.put(x, 1);
            return factors;
        }
        while (x > 1) {
            int bound = (int) (Math.sqrt(x) + 1);
            boolean found = false;
            for (int i = 2; i <= bound && !found; ++i) {
                if (x % i == 0) {
                    Integer count = factors.get(i);
                    if (count == null) {
                        count = 0;
                    }
                    factors.put(i, count + 1);
                    x /= i;
                    found = true;
                }
            }
            if (!found) {
                Integer count = factors.get(x);
                if (count == null) {
                    count = 0;
                }
                factors.put(x, count + 1);
                break;
            }
        }

        return factors;
    }
}
