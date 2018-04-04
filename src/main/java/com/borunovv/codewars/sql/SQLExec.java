package z_codewars.sql;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Task: http://www.codewars.com/kata/simple-sql-engine/
 * @author borunovv
 */
public class SQLExec {

    public static DataSet exec(Map<String, DataSet> db, String sql) {
        Query query = new Query(new Tokenizer(sql.toLowerCase()));
        query.parse();
        return query.execute(db);
    }

    private enum TokenType {SELECT, FROM, JOIN, ON, WHERE, COMMA, COMPARISON, LITERAL, QUOTED_LITERAL, EOF}

    private static class Token {
        final TokenType type;
        final String value;

        Token(String value) {
            this.type = determineType(value);
            this.value = value;
        }

        Token(TokenType type) {
            this.type = type;
            this.value = null;
        }

        private TokenType determineType(String value) {
            String lower = value.toLowerCase();
            switch (lower) {
                case "select":
                    return TokenType.SELECT;
                case "from":
                    return TokenType.FROM;
                case "join":
                    return TokenType.JOIN;
                case "on":
                    return TokenType.ON;
                case "where":
                    return TokenType.WHERE;
                case ",":
                    return TokenType.COMMA;
                case "<":
                case "<=":
                case ">":
                case ">=":
                case "=":
                case "<>":
                    return TokenType.COMPARISON;
                default:
                    return value.startsWith("'") ?
                            TokenType.QUOTED_LITERAL :
                            TokenType.LITERAL;
            }
        }

        @Override
        public String toString() {
            return "{" + type + (value != null ? ": " + value : "") + "}";
        }
    }

    private static class Tokenizer {
        private String data;
        private int pos;

        Tokenizer(String data) {
            this.data = data;
        }

        Token next() {
            skipSpaces();
            int start = pos;
            skipNoneSpaces();
            if (pos > start) {
                String value = data.substring(start, pos);
                return new Token(value);
            } else {
                return new Token(TokenType.EOF);
            }
        }

        Token peek() {
            int curPos = pos;
            Token t = next();
            pos = curPos;
            return t;
        }

        private void skipSpaces() {
            while (!isEOF() && isSpace(data.charAt(pos))) {
                pos++;
            }
        }

        private void skipNoneSpaces() {
            if (isEOF()) return;
            boolean quoted = data.charAt(pos) == '\'';
            if (!quoted) {
                while (!isEOF() && !isSpace(data.charAt(pos))) {
                    pos++;
                }
            } else {
                while (!isEOF()) {
                    pos++;
                    char ch = data.charAt(pos);
                    if (ch == '\'') {
                        pos++;
                        if (pos < data.length() && data.charAt(pos) == '\'') {
                        } else {
                            pos++;
                            return;
                        }
                    }
                }
                throw new RuntimeException("Expected closing quote");
            }
        }


        private boolean isEOF() {
            return pos >= data.length();
        }

        private boolean isSpace(char ch) {
            return " \n\r\t,".contains("" + ch);
        }
    }

    public static class DataSet {
        private List<Map<String, Object>> rows = new ArrayList<>();

        public void addRow(Map<String, Object> row) {
            rows.add(row);
        }

        public List<Map<String, Object>> getRows() {
            return rows;
        }

        public DSRow addRow() {
            Map<String, Object> row = new LinkedHashMap<>();
            addRow(row);
            return new DSRow(row);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            if (rows.isEmpty()) {
                sb.append("[empty]");
            } else {
                for (Map.Entry<String, Object> entry : rows.get(0).entrySet()) {
                    sb.append(entry.getKey()).append("\t");
                }
                sb.append("\n");

                for (Map<String, Object> row : rows) {
                    for (Map.Entry<String, Object> entry : row.entrySet()) {
                        sb.append(entry.getValue()).append("\t");
                    }
                    sb.append("\n");
                }
            }

            return sb.toString();
        }
    }

    public static class DSRow {
        private Map<String, Object> row;

        DSRow(Map<String, Object> row) {
            this.row = row;
        }

        public DSRow with(String key, long value) {
            row.put(key, value);
            return this;
        }

        public DSRow with(String key, double value) {
            row.put(key, value);
            return this;
        }

        public DSRow with(String key, String value) {
            row.put(key, value);
            return this;
        }
    }

    private static abstract class SQLItem {
        Tokenizer tokenizer;

        private SQLItem(Tokenizer tokenizer) {
            this.tokenizer = tokenizer;
        }

        public abstract void parse();

        Token nextToken() {
            return tokenizer.next();
        }

        Token peekToken() {
            return tokenizer.peek();
        }

        protected Token expected(TokenType type) {
            Token t = tokenizer.next();
            if (t.type != type) {
                throw new RuntimeException("Expected token: " + type + ", actual: " + t);
            }
            return t;
        }

        DataSet makeFullColumnNames(String table, DataSet ds) {
            DataSet res = new DataSet();
            for (Map<String, Object> row : ds.getRows()) {
                Map<String, Object> newRow = new LinkedHashMap<>();
                for (Map.Entry<String, Object> entry : row.entrySet()) {
                    newRow.put(table.toLowerCase() + "." + entry.getKey().toLowerCase(), entry.getValue());
                }
                res.addRow(newRow);
            }

            return res;
        }
    }

    private static class Comparison extends SQLItem {
        private String leftOperand;
        private String rightOperand;
        private String operation;

        public Comparison(Tokenizer tokenizer) {
            super(tokenizer);
        }

        public Comparison(String leftOperand, String rightOperand, String operation) {
            super(null);
            this.leftOperand = leftOperand;
            this.rightOperand = rightOperand;
            this.operation = operation;
        }

        @Override
        public void parse() {
            leftOperand = parseOperand();
            operation = expected(TokenType.COMPARISON).value;
            rightOperand = parseOperand();
        }

        private String parseOperand() {
            Token t = nextToken();
            if (t.type == TokenType.LITERAL || t.type == TokenType.QUOTED_LITERAL) {
                return t.value;
            } else {
                throw new RuntimeException("Comparison: expected literal or quoted literal. Found: " + t);
            }
        }

        boolean check(Map<String, Object> row) {
            Object leftOperandValue = getOperandValue(leftOperand, row);
            Object rightOperandValue = getOperandValue(rightOperand, row);
            return performOperation(leftOperandValue, rightOperandValue, operation);
        }

        private boolean performOperation(Object left, Object right, String operation) {
            left = toCommonType(left, right);
            right = toCommonType(right, left);
            switch (operation) {
                case "<=":
                    return doLessEqual(left, right);
                case "<":
                    return doLess(left, right);
                case ">=":
                    return doGreaterEqual(left, right);
                case ">":
                    return doGreater(left, right);
                case "=":
                    return doEquals(left, right);
                case "<>":
                    return doNotEquals(left, right);
                default:
                    throw new IllegalArgumentException("Unimplemented operation: " + operation);
            }
        }

        private Object toCommonType(Object obj, Object other) {
            if (obj.getClass().equals(other.getClass())) {
                return obj;
            }
            if (obj instanceof Long) {
                if (other instanceof Long) return obj;
                if (other instanceof Double) return (double) (Long) obj;
            } else if (obj instanceof Double) {
                if (other instanceof Double) return obj;
                if (other instanceof Long) return obj;
            } else if (obj instanceof String) {
                if (other instanceof String) return obj;
            }

            throw new RuntimeException("Comparison: incompatible operand types: "
                    + obj.getClass().getSimpleName() + " and " + other.getClass().getSimpleName());
        }

        private boolean doLessEqual(Object left, Object right) {
            if (left instanceof Long) {
                return (long) left <= (long) right;
            } else if (left instanceof Double) {
                return (double) left <= (double) right;
            } else if (left instanceof String) {
                return ((String) left).compareToIgnoreCase((String) right) <= 0;
            }
            throw new RuntimeException("Unsupported type: " + left.getClass().getSimpleName());
        }

        private boolean doLess(Object left, Object right) {
            if (left instanceof Long) {
                return (long) left < (long) right;
            } else if (left instanceof Double) {
                return (double) left < (double) right;
            } else if (left instanceof String) {
                return ((String) left).compareToIgnoreCase((String) right) < 0;
            }
            throw new RuntimeException("Unsupported type: " + left.getClass().getSimpleName());
        }

        private boolean doGreaterEqual(Object left, Object right) {
            if (left instanceof Long) {
                return (long) left >= (long) right;
            } else if (left instanceof Double) {
                return (double) left >= (double) right;
            } else if (left instanceof String) {
                return ((String) left).compareToIgnoreCase((String) right) >= 0;
            }
            throw new RuntimeException("Unsupported type: " + left.getClass().getSimpleName());
        }

        private boolean doGreater(Object left, Object right) {
            if (left instanceof Long) {
                return (long) left > (long) right;
            } else if (left instanceof Double) {
                return (double) left > (double) right;
            } else if (left instanceof String) {
                return ((String) left).compareToIgnoreCase((String) right) > 0;
            }
            throw new RuntimeException("Unsupported type: " + left.getClass().getSimpleName());
        }

        private boolean doEquals(Object left, Object right) {
            if (left instanceof Long) {
                return (long) left == (long) right;
            } else if (left instanceof Double) {
                return (double) left == (double) right;
            } else if (left instanceof String) {
                return ((String) left).compareToIgnoreCase((String) right) == 0;
            }
            throw new RuntimeException("Unsupported type: " + left.getClass().getSimpleName());
        }

        private boolean doNotEquals(Object left, Object right) {
            return !doEquals(left, right);
        }

        private Object getOperandValue(String operand, Map<String, Object> row) {
            Object operandValue = isColumn(operand) ?
                    row.get(operand) :
                    isNumber(operand) ?
                            toNumber(operand) :
                            unQuote(operand);
            if (operandValue == null) {
                throw new RuntimeException("Not found column " + operand + " in result set");
            }
            return operandValue;
        }

        private String unQuote(String s) {
            if (s.startsWith("'") && s.endsWith("'")) {
                String res = s.substring(1, s.length() - 1);
                res = res.replaceAll("''", "'");
                return res;
            } else {
                throw new RuntimeException("Expected quoted literal: " + s);
            }
        }

        private boolean isColumn(String s) {
            boolean hasQuotes = s.contains("'") || s.contains("\"");
            boolean containsDot = s.indexOf('.') > 0 && s.indexOf('.') < s.length();
            boolean number = isNumber(s);
            return !hasQuotes && !number && containsDot;
        }

        private boolean isNumber(String s) {
            try {
                double d = Double.parseDouble(s);
                return true;
            } catch (Exception ignore) {
                return false;
            }
        }

        private Number toNumber(String s) {
            double d = Double.parseDouble(s);
            long l = (long) d;
            if (d != (double) l) return d;
            return l;
        }
    }


    private static class Where extends SQLItem {
        private Comparison comparison = null;

        private Where(Tokenizer tokenizer) {
            super(tokenizer);
        }

        @Override
        public void parse() {
            expected(TokenType.WHERE);
            comparison = new Comparison(tokenizer);
            comparison.parse();
        }

        private DataSet execute(DataSet ds) {
            if (comparison == null) {
                throw new RuntimeException("Where: didn't parsed properly");
            } else {
                DataSet res = new DataSet();
                for (Map<String, Object> row : ds.getRows()) {
                    if (comparison.check(row)) {
                        res.addRow(row);
                    }
                }
                return res;
            }
        }
    }

    private static class Join extends SQLItem {
        private Comparison comparison;
        private String table;

        private Join(Tokenizer tokenizer) {
            super(tokenizer);
        }

        @Override
        public void parse() {
            expected(TokenType.JOIN);
            table = expected(TokenType.LITERAL).value;
            expected(TokenType.ON);
            comparison = new Comparison(tokenizer);
            comparison.parse();
        }

        private DataSet execute(DataSet left, Map<String, DataSet> db) {
            DataSet right = db.get(table);
            if (right == null) {
                throw new RuntimeException("Table not found: " + table);
            }
            right = makeFullColumnNames(table, right);

            // Decart's product
            DataSet res = new DataSet();
            for (Map<String, Object> leftRow : left.getRows()) {
                for (Map<String, Object> rightRow : right.getRows()) {
                    Map<String, Object> merged = new LinkedHashMap<>(leftRow);
                    merged.putAll(rightRow);
                    if (comparison.check(merged)) {
                        res.addRow(merged);
                    }
                }
            }
            return res;
        }
    }

    private static class From extends SQLItem {
        private List<Join> joins = new ArrayList<>();
        private String table;

        private From(Tokenizer tokenizer) {
            super(tokenizer);
        }

        private DataSet execute(Map<String, DataSet> db) {
            DataSet res = db.get(table);
            if (res == null) {
                throw new RuntimeException("Table not found: " + table);
            }
            res = makeFullColumnNames(table, res);
            for (Join join : joins) {
                res = join.execute(res, db);
            }

            return res;
        }

        @Override
        public void parse() {
            expected(TokenType.FROM);
            table = expected(TokenType.LITERAL).value;
            while (peekToken().type == TokenType.JOIN) {
                Join join = new Join(tokenizer);
                join.parse();
                joins.add(join);
            }
        }
    }

    private static class Select extends SQLItem {
        private List<String> columns = new ArrayList<>();

        private Select(Tokenizer tokenizer) {
            super(tokenizer);
        }

        private DataSet execute(DataSet ds) {
            DataSet res = new DataSet();
            for (Map<String, Object> row : ds.getRows()) {
                Map<String, Object> selectedColumns = filter(row);
                res.addRow(selectedColumns);
            }
            return res;
        }

        private Map<String, Object> filter(Map<String, Object> row) {
            Map<String, Object> res = new LinkedHashMap<>();
            for (String column : columns) {
                if (!row.containsKey(column)) {
                    throw new RuntimeException("Undefined column name '" + column + "'");
                }
                res.put(column, row.get(column));
            }

            return res;
        }

        @Override
        public void parse() {
            expected(TokenType.SELECT);
            while (peekToken().type == TokenType.LITERAL) {
                Token t = nextToken();
                columns.add(parseColumnName(t.value));
            }
            if (columns.size() == 0) {
                throw new RuntimeException("Select: expected at least 1 column name");
            }
        }

        private String parseColumnName(String value) {
            String[] items = value.split("\\.");
            if (items.length != 2 || items[0].trim().length() == 0 || items[1].trim().length() == 0) {
                throw new RuntimeException("Bad column name: " + value + ". Expected format: table.column");
            }
            return value;
        }

    }

    private static class Query extends SQLItem {
        private Select select;
        private From from;
        private Where where;

        public Query(Tokenizer tokenizer) {
            super(tokenizer);
        }

        public DataSet execute(Map<String, DataSet> db) {
            DataSet ds = from.execute(db);
            if (where != null) {
                ds = where.execute(ds);
            }
            ds = select.execute(ds);
            return ds;
        }

        @Override
        public void parse() {
            select = new Select(tokenizer);
            select.parse();
            from = new From(tokenizer);
            from.parse();
            if (peekToken().type == TokenType.WHERE) {
                where = new Where(tokenizer);
                where.parse();
            }
        }
    }
}
