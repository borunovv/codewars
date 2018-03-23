package z_codewars.break_the_pieces;

import java.util.*;

/**
 * Task description: https://www.codewars.com/kata/break-the-pieces-evilized-edition/java
 *
 * @author borunovv
 */
public class BreakEvilPieces {

    public static void main(String[] args) {
        String shape = "" +
                "         +-+                +-+                +-+                +-+                +-+         \n" +
                "         | |                +-+                +-+                +-+                +-+         \n" +
                "         | |            +------+                                                                 \n" +
                "         | +-----+      |+----+|                                                                 \n" +
                "         |    +-+|      ||+--+||                                                                 \n" +
                "         +-+  | ||      |||++|||              +----+                                             \n" +
                "+-----+    +--+ |+--+   |||++|||      ++      |+--+|  ++----------------------------------------+\n" +
                "|     +--+      |   |   |||+-+||      ||      ||++||  ||                                        |\n" +
                "+----+   +---+  +---+   ||+---+|      ++      ||++||  ++--------+     +------------+     +------+\n" +
                "     |       |          |+-++--+              |+--+|            |     |            |     |       \n" +
                "     +---+ +-+      +---+--+|                 +----+            +-+ +-+            +-+ +-+       \n" +
                "         | |        +-------+                                     | |                | |         \n" +
                "         | |                                                      | |                | |         \n" +
                "         | |                +-+                +-+                | |                | |         \n" +
                "         | |                +-+                | |                +-+                +-+         \n" +
                "         | |                                   | |                             +-----+ |    ++   \n" +
                "   +-----+ |                             +-----+ |                             +-++----+    ++   \n" +
                "   |+-+    |                             |+-+    |                               ++              \n" +
                "   || |  +-+               +----+        || |  +-+                               ||              \n" +
                "+--+| +--+    +-----+      |+--+|  ++----+| +--+    +------------------------+   |+-------------+\n" +
                "|   |      +--+     |      ||++||  ||     |      +--+                        |   |              |\n" +
                "+---+  +---+   +----+      ||++||  ++-----+  +---+   +----------+     +------+   +---+ +--------+\n" +
                "       |       |           |+--+|            |       |          |     |              | |         \n" +
                "       +-+ +---+           +----+            +-+ +---+          +-+ +-+              | |         \n" +
                "         | |                                   | |                | |                | |         \n" +
                "         | |                                   | |                | |                | |         \n" +
                "         | |                +-+                | |                | |                | |         \n" +
                "         +-+                +-+                +-+                +-+                +-+         ";
        System.out.println("Input:\n" + shape);
        long start = System.currentTimeMillis();
        render(solve(shape));
        System.out.println("time: " + (System.currentTimeMillis() - start) + " ms");
    }

    private static void render(List<String> shapes) {
        int i = 0;
        for (String shape : shapes) {
            i++;
            System.out.println("Shape #" + i);
            System.out.println(shape);
            System.out.println();
        }
    }

    public static List<String> solve(String shape) {
        byte[][] arr = trim(to2DArray(shape));
        int rows = arr.length;
        int columns = rows > 0 ? arr[0].length : 0;
        if (rows < 2 || columns < 2) return Collections.emptyList();

        byte[][] work = new byte[rows + 1][columns + 1];
        for (int r = 1; r < rows; ++r) {
            for (int c = 1; c < columns; ++c) {
                work[r][c] = 1;
            }
        }

        List<String> shapes = new ArrayList<>();
        for (int r = 1; r < rows; ++r) {
            for (int c = 1; c < columns; ++c) {
                byte a = work[r][c];
                if (a == 1) {
                    List<Point> area = findArea(r, c, work, arr);
                    if (area.size() > 0) {
                        String s = areaToString(area);
                        shapes.add(s);
                    }
                }
            }
        }

        return shapes;
    }

    private static byte[][] to2DArray(String shape) {
        String[] lines = shape.split("\n");
        int rows = lines.length;
        int columns = 0;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            columns = Math.max(columns, line.length());
        }
        byte[][] result = new byte[rows][columns];
        for (int r = 0; r < rows; r++) {
            String line = lines[r];
            for (int c = 0; c < columns; ++c) {
                result[r][c] = c < line.length() ?
                        (byte) line.charAt(c) :
                        (byte) ' ';
            }
        }
        return result;
    }

    private static byte[][] trim(byte arr[][]) {
        int rows = arr.length;
        int columns = rows > 0 ? arr[0].length : 0;

        int minRow = rows;
        int minCol = columns;
        int maxRow = -1;
        int maxCol = -1;

        for (int r = 0; r < rows; ++r) {
            for (int c = 0; c < columns; ++c) {
                if (isBorder(arr[r][c])) {
                    minRow = Math.min(minRow, r);
                    maxRow = Math.max(maxRow, r);
                    minCol = Math.min(minCol, c);
                    maxCol = Math.max(maxCol, c);
                }
            }
        }
        if (maxRow - minRow < 1 || maxCol - minCol < 1) {
            return new byte[0][0];
        }

        int newRows = maxRow - minRow + 1;
        int newColumns = maxCol - minCol + 1;

        if (newRows != rows || newColumns != columns) {
            byte[][] newArr = new byte[newRows][newColumns];
            for (int r = 0; r < newRows; ++r) {
                for (int c = 0; c < newColumns; ++c) {
                    newArr[r][c] = arr[r + minRow][c + minCol];
                }
            }
            return newArr;
        }
        return arr;
    }

    private static String areaToString(List<Point> workPoints) {
        int minRow = Integer.MAX_VALUE;
        int minCol = Integer.MAX_VALUE;
        int maxRow = 0;
        int maxCol = 0;
        for (Point p : workPoints) {
            minRow = Math.min(minRow, p.row);
            maxRow = Math.max(maxRow, p.row);
            minCol = Math.min(minCol, p.column);
            maxCol = Math.max(maxCol, p.column);
        }

        int rows = maxRow - minRow + 1;
        int columns = maxCol - minCol + 1;

        byte[][] work = new byte[rows][columns];
        for (Point p : workPoints) {
            work[p.row - minRow][p.column - minCol] = (byte) (p.tag + 1);
        }

        int[][] map = new int[rows + 1][columns + 1];
        for (int r = 0; r < rows; ++r) {
            for (int c = 0; c < columns; ++c) {
                int tag = work[r][c];
                if (tag == 0) continue;
                tag--;

                byte top = tryGet(r - 1, c, work);
                byte bottom = tryGet(r + 1, c, work);
                byte left = tryGet(r, c - 1, work);
                byte right = tryGet(r, c + 1, work);

                if (top == 0 || (tag & 1) == 0) {
                    map[r][c] |= (1 << 7);
                    map[r][c + 1] |= (1 << 6);
                }
                if (bottom == 0 || (tag & 2) == 0) {
                    map[r + 1][c] |= (1 << 5);
                    map[r + 1][c + 1] |= (1 << 4);
                }
                if (left == 0 || (tag & 4) == 0) {
                    map[r][c] |= (1 << 3);
                    map[r + 1][c] |= (1 << 1);
                }
                if (right == 0 || (tag & 8) == 0) {
                    map[r][c + 1] |= (1 << 2);
                    map[r + 1][c + 1] |= (1 << 0);
                }
            }
        }

        StringBuilder sb = new StringBuilder(1024);
        for (int r = 0; r < rows + 1; ++r) {
            if (r > 0) sb.append('\n');
            StringBuilder line = new StringBuilder();
            int lastNonEmpty = 0;
            for (int c = 0; c < columns + 1; ++c) {
                //byte ch = (byte) map[r][c];
                int ch = maskToChar(map[r][c]);
                if (ch != 0) {
                    line.append((char) ch);
                    lastNonEmpty = c;
                } else {
                    line.append(' ');
                }
            }
            sb.append(line.substring(0, lastNonEmpty + 1));
        }

        return sb.toString();
    }

    private static int maskToChar(int mask) {
        boolean horiz = (mask & (15 << 4)) > 0;
        boolean vert = (mask & 15) > 0;

        if (horiz && vert) {
            return '+';
        } else if (horiz) {
            return '-';
        } else if (vert) {
            return '|';
        } else {
            return 0;
        }
    }

    private static List<Point> findArea(int r, int c, byte[][] work, byte[][] arr) {
        List<Point> visited = new ArrayList<>(1024);
        Queue<Point> queue = new LinkedList<>();
        queue.add(new Point(r, c));

        boolean areaHasBorder = true;
        while (!queue.isEmpty()) {
            Point p = queue.poll();
            if (work[p.row][p.column] == 2) continue;
            work[p.row][p.column] = 2;

            visited.add(p);

            byte nTop = work[p.row - 1][p.column];
            byte nBottom = work[p.row + 1][p.column];
            byte nLeft = work[p.row][p.column - 1];
            byte nRight = work[p.row][p.column + 1];

            byte a1 = arr[p.row - 1][p.column - 1];
            byte a2 = arr[p.row - 1][p.column];
            byte a3 = arr[p.row][p.column - 1];
            byte a4 = arr[p.row][p.column];

            boolean topReachable = !((a1 == '+' | a1 == '-') && (a2 == '+' | a2 == '-'));
            if (topReachable) {
                if (nTop == 1) {
                    queue.add(new Point(p.row - 1, p.column));
                } else if (nTop == 0) {
                    areaHasBorder = false;
                }
            }

            boolean bottomReachable = !((a3 == '+' | a3 == '-') && (a4 == '+' | a4 == '-'));
            if (bottomReachable) {
                if (nBottom == 1) {
                    queue.add(new Point(p.row + 1, p.column));
                } else if (nBottom == 0) {
                    areaHasBorder = false;
                }
            }

            boolean leftReachable = !((a1 == '+' | a1 == '|') && (a3 == '+' | a3 == '|'));
            if (leftReachable) {
                if (nLeft == 1) {
                    queue.add(new Point(p.row, p.column - 1));
                } else if (nLeft == 0) {
                    areaHasBorder = false;
                }
            }

            boolean rightReachable = !((a2 == '+' | a2 == '|') && (a4 == '+' | a4 == '|'));
            if (rightReachable) {
                if (nRight == 1) {
                    queue.add(new Point(p.row, p.column + 1));
                } else if (nRight == 0) {
                    areaHasBorder = false;
                }
            }
            p.setTag(topReachable, bottomReachable, leftReachable, rightReachable);
        }
        markBad(visited, work);
        if (!areaHasBorder) {
            visited.clear();
        }

        return visited;
    }


    private static void markBad(List<Point> points, byte[][] work) {
        for (Point p : points) {
            work[p.row][p.column] = 0;
        }
    }

    private static boolean inside(int r, int c, byte[][] arr) {
        int rows = arr.length;
        int columns = rows > 0 ? arr[0].length : 0;
        return (0 <= r && r < rows)
                && (0 <= c && c < columns);
    }

    private static byte tryGet(int r, int c, byte[][] arr) {
        return inside(r, c, arr) ? arr[r][c] : 0;
    }

    private static boolean isBorder(byte c) {
        return c == '+' || c == '|' || c == '-';
    }

    private static class Point {
        public final int row;
        public final int column;
        public int tag;

        public Point(int row, int column) {
            this.row = row;
            this.column = column;
        }

        public void setTag(boolean topReachable, boolean bottomReachable, boolean leftReachable, boolean rightReachable) {
            this.tag = (topReachable ? 1 << 0 : 0)
                    | (bottomReachable ? 1 << 1 : 0)
                    | (leftReachable ? 1 << 2 : 0)
                    | (rightReachable ? 1 << 3 : 0);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Point point = (Point) o;

            if (row != point.row) return false;
            return column == point.column;
        }

        @Override
        public int hashCode() {
            int result = row;
            result = 31 * result + column;
            return result;
        }

        @Override
        public String toString() {
            return "(" + row + ", " + column + ")";
        }
    }
}