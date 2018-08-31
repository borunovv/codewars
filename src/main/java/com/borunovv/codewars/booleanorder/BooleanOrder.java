import java.math.BigInteger;

public class BooleanOrder {

// Kata URL: https://www.codewars.com/kata/59eb1e4a0863c7ff7e000008

private boolean[] operands;
    private char[] operators;
    private BigInteger[][] cacheT;
    private BigInteger[][] cacheF;

    public BooleanOrder(final String operands, final String operators) {
        this.operands = new boolean[operands.length()];
        this.operators = new char[operators.length()];
        this.cacheT = new BigInteger[operands.length()][operands.length()];
        this.cacheF = new BigInteger[operands.length()][operands.length()];

        for (int i = 0; i < operands.length(); ++i) {
            this.operands[i] = operands.charAt(i) == 't';
            if (i < operators.length()) {
                this.operators[i] = operators.charAt(i);
            }
        }
    }

    public BigInteger solve() {
        int n = operands.length - 1;
        for (int s = 0; s <= n; ++s) {
            for (int i = 0; i <= n - s; ++i) {
                int j = s + i;
                BigInteger[] tf = calcTF(i, j);
                cacheT[i][j] = tf[0];
                cacheF[i][j] = tf[1];
            }
        }
        return cacheT[0][n];
    }

    private BigInteger[] calcTF(int i, int j) {
        BigInteger[] res = new BigInteger[2];

        if (i == j) {
            res[0] = operands[i] ? BigInteger.ONE : BigInteger.ZERO;
            res[1] = operands[i] ? BigInteger.ZERO : BigInteger.ONE;
            return res;
        }

        res[0] = res[1] = BigInteger.ZERO;
        for (int k = i; k < j; ++k) {
            int leftI = i;
            int leftJ = k;
            int rightI = k + 1;
            int rightJ = j;

            BigInteger leftT = cacheT[leftI][leftJ];
            BigInteger leftF = cacheF[leftI][leftJ];
            BigInteger rightT = cacheT[rightI][rightJ];
            BigInteger rightF = cacheF[rightI][rightJ];

            char op = operators[k];
            switch (op) {
                case '&': {
                    res[0] = res[0].add(leftT.multiply(rightT));
                    res[1] = res[1].add(leftF.multiply(rightF)
                            .add(leftF.multiply(rightT))
                            .add(leftT.multiply(rightF)));
                    break;
                }
                case '|': {
                    res[0] = res[0].add(leftT.multiply(rightT)
                            .add(leftF.multiply(rightT))
                            .add(leftT.multiply(rightF)));
                    res[1] = res[1].add(leftF.multiply(rightF));
                    break;
                }
                case '^': {
                    res[0] = res[0].add(leftT.multiply(rightF)
                            .add(leftF.multiply(rightT)));
                    res[1] = res[1].add(leftT.multiply(rightT)
                            .add(leftF.multiply(rightF)));
                    break;
                }
                default:
                    throw new RuntimeException("Undefined operation: " + op);
            }
        }

        return res;
    }
}