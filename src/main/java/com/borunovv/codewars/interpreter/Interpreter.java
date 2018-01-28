package com.borunovv.codewars.interpreter;

import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * function        ::= fn-keyword fn-name { identifier } fn-operator expression
 * fn-name         ::= identifier
 * fn-operator     ::= '=>'
 * fn-keyword      ::= 'fn'
 * <p>
 * expression      ::= factor | expression operator expression
 * factor          ::= number | identifier | assignment | '(' expression ')' | function-call
 * assignment      ::= identifier '=' expression
 * function-call   ::= fn-name { expression }
 * <p>
 * operator        ::= '+' | '-' | '*' | '/' | '%'
 * <p>
 * identifier      ::= letter | '_' { identifier-char }
 * identifier-char ::= '_' | letter | digit
 * <p>
 * number          ::= { digit } [ '.' digit { digit } ]
 * <p>
 * letter          ::= 'a' | 'b' | ... | 'y' | 'z' | 'A' | 'B' | ... | 'Y' | 'Z'
 * digit           ::= '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9'
 */
public class Interpreter {

    public enum TokenType {
        FN_KEYWORD, FN_OPERATOR, IDENTIFIER, ASSIGNMENT_OP, ADD_OP, MUL_OP, NUMBER, OPEN_PAREN,
        CLOSE_PAREN
    }

    public static class Token {
        public final TokenType type;
        public final String value;

        public Token(String value, TokenType type) {
            this.value = value;
            this.type = type;
        }

        @Override
        public String toString() {
            return '\'' + value + "' (" + type + ')';
        }
    }

    public enum NodeType {IDENTIFIER, NUMBER, ASSIGNMENT, MATH_OPERATION, FUNCTION_CALL}

    public static abstract class Node {
        public final NodeType type;
        public final List<Node> children = new ArrayList<>();

        public Node(NodeType type) {
            this.type = type;
        }

        public void add(Node child) {
            children.add(child);
        }

        public abstract Double evaluate(Map<String, Double> variables);
    }

    public class VariableNode extends Node {
        private final String name;

        public VariableNode(String name) {
            super(NodeType.IDENTIFIER);
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public Double evaluate(Map<String, Double> context) {
            if (!context.containsKey(name)) {
                throw new IllegalArgumentException("Unknown identifier: '" + name + "'");
            }
            return context.get(name);
        }
    }

    public static class OperationNode extends Node {
        private final String operation;

        public OperationNode(String operation) {
            super(NodeType.MATH_OPERATION);
            this.operation = operation;
        }

        @Override
        public Double evaluate(Map<String, Double> context) {
            if (children.size() != 2) {
                throw new IllegalArgumentException("Expected 2 operands for operator: '" + operation + "'");
            }

            double operand1 = children.get(0).evaluate(context);
            double operand2 = children.get(1).evaluate(context);

            switch (operation) {
                case "+":
                    return operand1 + operand2;
                case "-":
                    return operand1 - operand2;
                case "*":
                    return operand1 * operand2;
                case "/":
                    return operand1 / operand2;
                case "%":
                    return operand1 % operand2;
                default:
                    throw new IllegalArgumentException("Unrecognized operator: '" + operation + "'");
            }
        }
    }

    public static class NumberNode extends Node {
        private final double value;

        public NumberNode(String strValue) {
            super(NodeType.NUMBER);
            this.value = Double.parseDouble(strValue);
        }

        @Override
        public Double evaluate(Map<String, Double> context) {
            return value;
        }
    }

    public class AssignmentNode extends Node {
        private final String variableName;

        public AssignmentNode(String variableName) {
            super(NodeType.ASSIGNMENT);
            if (variableName == null) {
                throw new IllegalArgumentException("Bad variable name: '" + variableName + "'");
            }
            this.variableName = variableName;
        }

        @Override
        public Double evaluate(Map<String, Double> context) {
            if (children.size() != 1) {
                throw new IllegalArgumentException("Expected 1 argument for assignment");
            }

            double operand1 = children.get(0).evaluate(context);
            variables.put(variableName, operand1);

            return operand1;
        }
    }

    public class FunctionCallNode extends Node {
        private final String name;

        public FunctionCallNode(String name) {
            super(NodeType.FUNCTION_CALL);
            if (!functions.containsKey(name)) {
                throw new IllegalArgumentException("Unknown function '" + name + "'");
            }

            this.name = name;
        }

        @Override
        public Double evaluate(Map<String, Double> context) {
            List<String> argNames = functions.get(name).argNames;
            if (argNames.size() != children.size()) {
                throw new IllegalArgumentException("Expected " + argNames.size()
                                                   + " arguments for function '" + name + "'");
            }

            Map<String, Double> functionContext = new HashMap<>();
            int index = 0;
            for (String argName : argNames) {
                functionContext.put(argName, children.get(index++).evaluate(context));
            }

            return functions.get(name).root.evaluate(functionContext);
        }
    }

    public static class FunctionInfo {
        public final Node root;
        public final List<String> argNames;

        public FunctionInfo(Node root, List<String> argNames) {
            this.root = root;
            this.argNames = argNames;
        }
    }

    private Map<String, Double> variables = new HashMap<>();
    private Map<String, FunctionInfo> functions = new HashMap<>();


    public Double input(String input) {
        Deque<Token> tokens = tokenize(input);
        return program(tokens);
    }

    private Double program(Deque<Token> tokens) {
        if (tokens.isEmpty()) {
            return null;
        }

        Token next = tokens.peek();
        if (next.type == TokenType.FN_KEYWORD) {
            return functionDeclaration(tokens);
        } else {
            Node root = expression(tokens, true);
            if (!tokens.isEmpty()) {
                throw new IllegalArgumentException("Unexpected token: '" + tokens.peek() + "'");
            }
            return root != null ?
                    root.evaluate(variables) :
                    null;
        }
    }

    /**
     * function        ::= fn-keyword fn-name { identifier } fn-operator expression
     * fn-name         ::= identifier
     * fn-operator     ::= '=>'
     * fn-keyword      ::= 'fn'
     */
    private Double functionDeclaration(Deque<Token> tokens) {
        expect(TokenType.FN_KEYWORD, tokens);
        Token nameToken = expect(TokenType.IDENTIFIER, tokens);
        if (variables.containsKey(nameToken.value)) {
            throw new IllegalArgumentException("Can't declare function: variable with name '" + nameToken + "' " +
                                               "already exists.");
        }

        List<String> argNames = new ArrayList<>();
        while (!tokens.isEmpty() && tokens.peek().type == TokenType.IDENTIFIER) {
            if (argNames.contains(tokens.peek().value)) {
                throw new IllegalArgumentException("Duplicated function arg name: '" + tokens.peek().value + "'");
            }
            argNames.add(tokens.poll().value);
        }
        expect(TokenType.FN_OPERATOR, tokens);

        Node root = expression(tokens, true);

        // Убедимся, что в функции не используются неизвестные переменные
        Set<String> allVariables = new HashSet<>();
        traverse(root, (Node n) -> {
            if (n instanceof VariableNode) {
                allVariables.add(((VariableNode) n).getName());
            }
        });

        for (String variable : allVariables) {
            if (!argNames.contains(variable)) {
                throw new IllegalArgumentException("Unexpected variable '" + variable + "' in function body (not " +
                                                   "declared in signature)");
            }
        }
        functions.put(nameToken.value, new FunctionInfo(root, argNames));

        return null;
    }

    private void traverse(Node n, Consumer<Node> consumer) {
        consumer.accept(n);
        for (Node child : n.children) {
            traverse(child, consumer);
        }
    }

    /**
     * expression      ::= factor | factor {add_op factor}+
     * factor          ::= number | identifier | assignment | '(' expression ')' | function-call | factor {[*\] factor}+
     * assignment      ::= identifier '=' expression
     * function-call   ::= fn-name { expression }
     */
    private Node expression(Deque<Token> tokens, boolean allowMultipleAddOp) {
        if (tokens.isEmpty()) {
            throw new RuntimeException("Expected expression, but tokens list is empty");
        }

        Node first = factor(tokens, true);
        if (tokens.isEmpty()) {
            return first;
        }

        while (allowMultipleAddOp && !tokens.isEmpty() && tokens.peek().type == TokenType.ADD_OP) {
            Token addOperator = expect(TokenType.ADD_OP, tokens);
            Node second = expression(tokens, false);

            Node addOperation = new OperationNode(addOperator.value);
            addOperation.add(first);
            addOperation.add(second);
            first = addOperation;
        }

        return first;
    }

    // factor ::= number | function-call  | assignment | identifier | '(' expression ')' | factor mul_op factor
    private Node factor(Deque<Token> tokens, boolean allowMultipleMulOp) {
        if (tokens.isEmpty()) {
            throw new RuntimeException("Expected factor, but tokens list is empty");
        }

        Node first;
        if (tokens.peek().type == TokenType.NUMBER) {
            first = new NumberNode(tokens.poll().value);
        } else if (tokens.peek().type == TokenType.IDENTIFIER) {
            Token cur = tokens.poll();
            if (functions.containsKey(cur.value)) {
                first = functionCall(cur.value, tokens);

            } else if (!tokens.isEmpty() && tokens.peek().type == TokenType.ASSIGNMENT_OP) {
                Node node = new AssignmentNode(cur.value);
                tokens.poll();
                node.add(expression(tokens, true));
                first = node;

            } else {
                first = new VariableNode(cur.value);
            }
        } else {
            expect(TokenType.OPEN_PAREN, tokens);
            first = expression(tokens, true);
            expect(TokenType.CLOSE_PAREN, tokens);
        }

        while (allowMultipleMulOp && !tokens.isEmpty() && tokens.peek().type == TokenType.MUL_OP) {
            Token operator = tokens.poll();
            Node second = factor(tokens, false);

            Node mulOperation = new OperationNode(operator.value);
            mulOperation.add(first);
            mulOperation.add(second);
            first = mulOperation;
        }

        return first;
    }

    // function-call ::= fn-name { expression }
    private Node functionCall(String name, Deque<Token> tokens) {
        Node funcNode = new FunctionCallNode(name);

        for (String argName : functions.get(name).argNames) {
            funcNode.add(expression(tokens, true));
        }

        return funcNode;
    }

    private Token expect(TokenType type, Deque<Token> tokens) {
        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("Expected token: " + type + ", but tokens list is empty");
        }

        Token next = tokens.poll();
        if (next.type != type) {
            throw new IllegalArgumentException("Expected token: " + type + ", but actual is: " + next.type);
        }
        return next;
    }


    protected static Deque<Token> tokenize(String input) {
        Deque<Token> tokens = new LinkedList<>();
        input = input != null ? input.trim() : null;
        if (input == null || input.isEmpty()) {
            return tokens;
        }

        Pattern pattern = Pattern.compile("=>|[-+*/%=\\(\\)]|[A-Za-z_][A-Za-z0-9_]*|[0-9]*(\\.?[0-9]+)");
        Matcher m = pattern.matcher(input);
        while (m.find()) {
            String token = m.group();
            tokens.add(new Token(token, getTokenType(token)));
        }
        return tokens;
    }

    protected static TokenType getTokenType(String token) {
        if (token.equals("fn")) {
            return TokenType.FN_KEYWORD;
        }
        if (token.equals("=>")) {
            return TokenType.FN_OPERATOR;
        }
        if (token.equals("=")) {
            return TokenType.ASSIGNMENT_OP;
        }
        if (token.equals("(")) {
            return TokenType.OPEN_PAREN;
        }
        if (token.equals(")")) {
            return TokenType.CLOSE_PAREN;
        }
        if (Arrays.asList("+", "-").contains(token)) {
            return TokenType.ADD_OP;
        }
        if (Arrays.asList("*", "/", "%").contains(token)) {
            return TokenType.MUL_OP;
        }
        if (isDigit(token.charAt(0))) {
            return TokenType.NUMBER;
        }
        if (isLetter(token.charAt(0))) {
            return TokenType.IDENTIFIER;
        }

        throw new RuntimeException("Unrecognized token: '" + token + "'");
    }

    private static boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    private static boolean isLetter(char ch) {
        return ch == '_' || (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z');
    }
}