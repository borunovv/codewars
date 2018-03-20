package z_codewars.skyscrapers;

import java.util.*;

/**
 * Task on codewars: http://www.codewars.com/kata/7x7-skyscrapers/
 *
 * @author borunovv
 */
public class SkyScrapers7x7 {
    private static int SIZE = 7;

    private static int sumPowers = 0;

    static {
        for (int i = 0; i < SIZE; ++i) {
            sumPowers += 1 << i;
        }
    }

    private static Map<Integer, Pair<Set<Integer>>> cache = new HashMap<>();

    private static int[] clues = new int[]{ 7,0,0,0,2,2,3, 0,0,3,0,0,0,0, 3,0,3,0,0,5,0, 0,0,0,0,5,0,4 };

    private static int[][] expected = new int[][]{ new int[] { 1,5,6,7,4,3,2 },
            new int[] { 2,7,4,5,3,1,6 },
            new int[] { 3,4,5,6,7,2,1 },
            new int[] { 4,6,3,1,2,7,5 },
            new int[] { 5,3,1,2,6,4,7 },
            new int[] { 6,2,7,3,1,5,4 },
            new int[] { 7,1,2,4,5,6,3 }};


    public static void main(String[] args) {
        test();
    }

    private static void test() {
        assertEquals(SkyScrapers7x7.solvePuzzle(clues), expected);
    }


    public static int[][] solvePuzzle(int[] clues) {
        List<Set<Integer>> rowSets = new ArrayList<>(SIZE);
        List<Set<Integer>> colSets = new ArrayList<>(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            Set<Integer> leftToRight = getCombinationsSet(clues[4 * SIZE - 1 - i]).first;
            Set<Integer> rightToLeft = getCombinationsSet(clues[SIZE + i]).second;
            rowSets.add(intersect(leftToRight, rightToLeft));

            Set<Integer> topToBottom = getCombinationsSet(clues[i]).first;
            Set<Integer> bottomToTop = getCombinationsSet(clues[3 * SIZE - 1 - i]).second;
            colSets.add(intersect(topToBottom, bottomToTop));
        }

        int[][] board = new int[SIZE][SIZE];
        int[][] variants = new int[SIZE * SIZE][];
        long variantCount;
        while (true) {
            variantCount = 1;
            for (int r = 0; r < SIZE; ++r) {
                for (int c = 0; c < SIZE; ++c) {
                    int ind = r * SIZE + c;
                    int[] arr;
                    Set<Integer> rowVariants = getVariants(rowSets.get(r), c);
                    Set<Integer> colVariants = getVariants(colSets.get(c), r);
                    Set<Integer> merged = intersect(rowVariants, colVariants);
                    if (merged.size() == 0) {
                        throw new RuntimeException("merged is empty");
                    }
                    arr = new int[merged.size()];
                    int i = 0;
                    for (Integer val : merged) {
                        arr[i++] = val;
                    }
                    variants[ind] = arr;
                    board[r][c] = variants[ind][0];
                    variantCount *= arr.length;
                }
            }

            boolean changed = false;
            for (int i = 0; i < SIZE * SIZE; ++i) {
                int[] legal = variants[i];
                int row = i / SIZE;
                int col = i % SIZE;
                Set<Integer> values = rowSets.get(row);
                Set<Integer> fixed = excludeNotInPosition(values, legal, col);
                if (fixed.size() < values.size()) {
                    rowSets.set(row, fixed);
                    changed = true;
                }

                values = colSets.get(col);
                fixed = excludeNotInPosition(values, legal, row);
                if (fixed.size() < values.size()) {
                    colSets.set(col, fixed);
                    changed = true;
                }
            }

            if (! changed) break;
        }

        // We assume all tests will cause only single solution (i.e. variantCount == 1).
        // But just in case I do check all possibilities.
        if (variantCount > 1) {
            System.out.println("Variants to more then 1: " + variantCount);

            Line[] lines = getLines(clues, board);

            int[] counter = new int[SIZE * SIZE];
            boolean changed = true;
            while (changed && !isGood(lines)) {
                changed = false;
                for (int i = SIZE * SIZE - 1; i >= 0; i--) {
                    int limit = variants[i].length - 1;
                    if (counter[i] == limit) {
                        counter[i] = 0;
                        board[i / SIZE][i % SIZE] = variants[i][0];
                    } else {
                        counter[i]++;
                        board[i / SIZE][i % SIZE] = variants[i][counter[i]];
                        changed = true;
                        break;
                    }
                }
            }

            if (!changed) {
                throw new RuntimeException("Not found solution");
            }
        }

        System.out.println("Solution:\n" + toString(board));
        return board;
    }

    private static Set<Integer> excludeNotInPosition(Set<Integer> values, int[] legal, int digitIndex) {
        Set<Integer> result = new HashSet<>();
        for (Integer val : values) {
            int digit = (val >> (digitIndex * 3)) & 7;
            boolean isLegal = false;
            for (int i = 0; i < legal.length; ++i) {
                if (digit == legal[i]) {
                    isLegal = true;
                    break;
                }
            }
            if (isLegal) {
                result.add(val);
            }
        }
        return result;
    }

    private static Set<Integer> getVariants(Set<Integer> integers, int index) {
        Set<Integer> result = new HashSet<>();
        for (Integer val : integers) {
            int digit = (val >> (index * 3)) & 7;
            result.add(digit);
        }
        return result;
    }

    private static boolean isGood(Line[] lines) {
        for (int i = 0; i < lines.length; i++) {
            if (!lines[i].isValid()) return false;
        }
        return true;
    }

    private static Line[] getLines(int[] clues, int[][] board) {
        Line[] lines = new Line[4 * SIZE];
        int index = 0;
        for (int c = 0; c < SIZE; ++c) {
            lines[index] = new Line(board, clues[index]);
            lines[3 * SIZE - 1 - index] = new Line(board, clues[3 * SIZE - 1 - index]);
            for (int r = 0; r < SIZE; ++r) {
                lines[index].setCell(r, r, c);
                lines[3 * SIZE - 1 - index].setCell(r, SIZE - 1 - r, c);
            }
            index++;
        }
        for (int r = 0; r < SIZE; ++r) {
            lines[index] = new Line(board, clues[index]);
            lines[5 * SIZE - 1 - index] = new Line(board, clues[5 * SIZE - 1 - index]);
            for (int c = 0; c < SIZE; ++c) {
                lines[index].setCell(c, r, SIZE - 1 - c);
                lines[5 * SIZE - 1 - index].setCell(c, r, c);
            }
            index++;
        }
        return lines;
    }

    private static Set<Integer> intersect(Set<Integer> a, Set<Integer> b) {
        Set<Integer> result = new LinkedHashSet<>();
        for (Integer val : a) {
            if (b.contains(val)) {
                result.add(val);
            }
        }
        return result;
    }

    private static Pair<Set<Integer>> getCombinationsSet(int skyScrapers) {
        if (cache.containsKey(skyScrapers)) return cache.get(skyScrapers);
        Pair<ArrayList<Integer>> combinations = calcCombinations(skyScrapers);
        cache.put(skyScrapers, new Pair<>(new LinkedHashSet<>(combinations.first), new LinkedHashSet<>(combinations.second)));
        return cache.get(skyScrapers);
    }

    private static Pair<ArrayList<Integer>> calcCombinations(int skyScrapers) {
        ArrayList<Integer> result = new ArrayList<>();
        ArrayList<Integer> resultInv = new ArrayList<>();

        int[] temp3 = new int[SIZE];
        int[] temp = new int[SIZE];

        while (true) {
            for (int i = 0; i < SIZE; ++i) {
                temp[i] = i + 1;
            }

            int value = 0;
            int valueInv = 0;
            int max = 0;
            int visible = 0;
            for (int i = 0; i < SIZE && (visible <= skyScrapers || skyScrapers == 0); ++i) {
                int index = temp3[i];
                for (int j = 0; j < SIZE; ++j) {
                    if (temp[j] > 0) {
                        if (index == 0) {
                            if (temp[j] > max) {
                                max = temp[j];
                                visible++;
                                if (skyScrapers > 0 && visible > skyScrapers) {
                                    break;
                                }
                            }
                            value |= temp[j] << (3 * i);
                            valueInv |= temp[j] << (3 * (SIZE - 1 - i));
                            temp[j] = 0;
                            break;
                        } else {
                            index--;
                        }
                    }
                }
            }

            if (skyScrapers == 0 || visible == skyScrapers) {
                result.add(value);
                resultInv.add(valueInv);
            }

            boolean advanced = false;
            for (int i = SIZE - 1; i >= 0; i--) {
                int limit = SIZE - 1 - i;
                if (temp3[i] < limit) {
                    temp3[i]++;
                    advanced = true;
                    break;
                } else {
                    temp3[i] = 0;
                }
            }

            if (!advanced) break;
        }

        return new Pair<>(result, resultInv);
    }

    private static class Line {
        private int[] cells = new int[SIZE];
        private int skyScrapers;
        private int[][] board;

        public Line(int[][] board, int skyScrapers) {
            this.skyScrapers = skyScrapers;
            this.board = board;
        }

        public void setCell(int index, int row, int column) {
            cells[index] = row * 10 + column;
        }

        public int get(int cellIndex) {
            return board[getRow(cells[cellIndex])][getColumn(cells[cellIndex])];
        }

        public void set(int cellIndex, int val) {
            board[getRow(cells[cellIndex])][getColumn(cells[cellIndex])] = val;
        }

        private int getRow(int index) {
            return index / 10;
        }

        private int getColumn(int index) {
            return index % 10;
        }

        public boolean isValid() {
            int max = 0;
            int visible = 0;
            int magicSum = 0;

            for (int i = 0; i < SIZE; ++i) {
                int height = get(i);
                if (height > max) {
                    visible++;
                    max = height;
                }
                magicSum += 1 << (height - 1);
            }

            return magicSum == sumPowers && (skyScrapers == 0 || visible == skyScrapers);
        }
    }

    private static void assertEquals(int[][] actual, int[][] expected) {
        if (actual.length != expected.length) throw new RuntimeException("Different lengths");
        for (int r = 0; r < actual.length; ++r) {
            if (actual[r].length != expected[r].length) throw new RuntimeException("Different row lengths, row: " + r);
            for (int c = 0; c < actual[r].length; ++c) {
                if (actual[r][c] != expected[r][c]) {
                    throw new RuntimeException("Arrays are different!\nActual:\n"
                            + toString(actual) + "\nExpected:\n" + toString(expected));
                }
            }
        }
    }

    private static String toString(int[][] a) {
        StringBuilder sb = new StringBuilder();

        for (int r = 0; r < a.length; ++r) {
            for (int c = 0; c < a[r].length; ++c) {
                if (c > 0) sb.append(" ");
                sb.append(a[r][c]);
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private static class Pair<T> {
        public T first;
        public T second;

        public Pair(T first, T second) {
            this.first = first;
            this.second = second;
        }
    }
}
