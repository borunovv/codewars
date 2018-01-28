package com.borunovv.codewars.mathexpr;

/**
 * Challenge URL: https://www.codewars.com/kata/52a78825cdfc2cfc87000005
 *
 * @author borunovv
 */
public class MathEvaluator {

    private interface Node {
        double evaluate();
    }

    private class NodeNumber implements Node {
        private double value;

        NodeNumber(double value) {
            this.value = value;
        }

        public double evaluate() {
            return value;
        }
    }

    private class MulOpNode implements Node {
        private char operation;
        private Node leftOperand;
        private Node rightOperand;

        public MulOpNode(char operation, Node leftOperand, Node rightOperand) {
            this.operation = operation;
            this.leftOperand = leftOperand;
            this.rightOperand = rightOperand;
        }

        public double evaluate() {
            switch (operation) {
                case '*':
                    return leftOperand.evaluate() * rightOperand.evaluate();
                case '/':
                    return leftOperand.evaluate() / rightOperand.evaluate();
                default:
                    throw new RuntimeException("Unsupported multiplication operation: '" + operation + "'");
            }
        }
    }

    private class AddOpNode implements Node {
        private char operation;
        private Node leftOperand;
        private Node rightOperand;

        public AddOpNode(char operation, Node leftOperand, Node rightOperand) {
            this.operation = operation;
            this.leftOperand = leftOperand;
            this.rightOperand = rightOperand;
        }

        public double evaluate() {
            switch (operation) {
                case '+':
                    return leftOperand.evaluate() + rightOperand.evaluate();
                case '-':
                    return leftOperand.evaluate() - rightOperand.evaluate();
                default:
                    throw new RuntimeException("Unsupported add operation: '" + operation + "'");
            }
        }
    }

    private class NegateNode implements Node {
        private Node child;

        public NegateNode(Node child) {
            this.child = child;
        }

        public double evaluate() {
            return 0 - child.evaluate();
        }
    }

    private class CharStream {
        private String source;
        private int next = 0;

        public CharStream(String source) {
            this.source = source;
        }

        public boolean eof() {
            return next >= source.length();
        }

        public char poll() {
            ensureNotEOF();
            return source.charAt(next++);
        }

        public char peek() {
            ensureNotEOF();
            return source.charAt(next);
        }

        public char current() {
            if (next < 1) {
                throw new RuntimeException("Position in stream if before start");
            }
            return source.charAt(next - 1);
        }

        private void ensureNotEOF() {
            if (eof()) {
                throw new RuntimeException("End of stream");
            }
        }
    }

    private enum TokenType {
        OPEN_PAREN,
        CLOSE_PAREN,
        PLUS,
        MINUS,
        MULTIPLY,
        DIVIDE,
        NUMBER,
        EOF
    }

    private class Token {
        public final TokenType type;
        public final double value;

        public Token(TokenType type) {
            this.type = type;
            this.value = 0;
        }

        public Token(TokenType type, double value) {
            this.type = type;
            this.value = value;
        }

        public double getValue() {
            return value;
        }

        @Override
        public String toString() {
            return "Token{" +
                    type + (type == TokenType.NUMBER ? ", value=" + value : "") +
                    '}';
        }
    }

    private class Tokenizer {
        private CharStream stream;
        private Token lastPeeked = null;

        public Tokenizer(CharStream stream) {
            this.stream = stream;
        }

        public Token poll() {
            Token result;
            if (lastPeeked != null) {
                result = lastPeeked;
                lastPeeked = null;
            } else {
                result = next();
            }
            return result;
        }

        public Token peek() {
            if (lastPeeked == null) {
                lastPeeked = next();
            }
            return lastPeeked;
        }

        private boolean eof() {
            return peek().type == TokenType.EOF;
        }

        private Token next() {
            // Skip spaces
            while (!stream.eof() && stream.peek() == ' ') {
                stream.poll();
            }

            if (stream.eof()) {
                return new Token(TokenType.EOF);
            }

            char ch = stream.poll();
            switch (ch) {
                case '(':
                    return new Token(TokenType.OPEN_PAREN);
                case ')':
                    return new Token(TokenType.CLOSE_PAREN);
                case '*':
                    return new Token(TokenType.MULTIPLY);
                case '/':
                    return new Token(TokenType.DIVIDE);
                case '+':
                    return new Token(TokenType.PLUS);
                case '-': {
                    return new Token(TokenType.MINUS);
                }
                default: {
                    if (ch == '.' || (ch >= '0' && ch <= '9')) {
                        double number = parseNumber();
                        return new Token(TokenType.NUMBER, number);
                    } else {
                        throw new IllegalArgumentException("Unexpected character: '" + ch + "'");
                    }
                }
            }
        }

        private double parseNumber() {
            StringBuilder sb = new StringBuilder();
            sb.append(stream.current());
            while (!stream.eof() && (stream.peek() == '.' || (stream.peek() >= '0' && stream.peek() <= '9'))) {
                sb.append(stream.poll());
            }
            return Double.parseDouble(sb.toString());
        }
    }

    public double calculate(String expr) {
        System.out.println("Input: '" + expr + "'");
        Node tree = parse(expr);
        return tree.evaluate();
    }

    private Node parse(String expr) {
        Tokenizer tokenizer = new Tokenizer(new CharStream(expr));
        Node root = expression(tokenizer);
        if (!tokenizer.eof()) {
            throw new IllegalArgumentException("Parse error: expected EOF");
        }
        return root;
    }

    // < expression > ::= < term > + < expression > | < term > - < expression > | < term >
    private Node expression(Tokenizer tokenizer) {
        Node root = term(tokenizer);
        while (!tokenizer.eof()) {
            Token op = tokenizer.peek();
            if (op.type == TokenType.PLUS || op.type == TokenType.MINUS) {
                tokenizer.poll();
                Node right = term(tokenizer);
                root = new AddOpNode(op.type == TokenType.PLUS ? '+' : '-', root, right);
            } else {
                break;
            }
        }

        return root;
    }

    // < term > ::= < factor > * < term > | < factor > / < term > | < factor >
    private Node term(Tokenizer tokenizer) {
        Node root = factor(tokenizer);
        while (!tokenizer.eof()) {
            Token op = tokenizer.peek();
            if (op.type == TokenType.MULTIPLY || op.type == TokenType.DIVIDE) {
                tokenizer.poll();
                Node right = factor(tokenizer);
                root = new MulOpNode(op.type == TokenType.MULTIPLY ? '*' : '/', root, right);
            } else {
                break;
            }
        }
        return root;
    }

    // <factor> ::= [-]<positive_factor>
    private Node factor(Tokenizer tokenizer) {
        Token token = tokenizer.peek();
        boolean needNegate = false;
        if (token.type == TokenType.MINUS) {
            tokenizer.poll();
            needNegate = true;
        }
        return needNegate ?
                new NegateNode(positiveFactor(tokenizer)) :
                positiveFactor(tokenizer);
    }

    // < positive_factor > ::= (< expression >) | < float >
    private Node positiveFactor(Tokenizer tokenizer) {
        Token next = tokenizer.peek();
        if (next.type == TokenType.OPEN_PAREN) {
            tokenizer.poll();
            Node result = expression(tokenizer);
            expected(tokenizer, TokenType.CLOSE_PAREN);
            return result;
        } else {
            Token number = expected(tokenizer, TokenType.NUMBER);
            return new NodeNumber(number.getValue());
        }
    }

    private Token expected(Tokenizer tokenizer, TokenType type) {
        Token token = tokenizer.poll();
        if (token.type != type) {
            throw new IllegalArgumentException("Expected token: " + type + ", but actual is: " + token.type);
        }
        return token;
    }
}