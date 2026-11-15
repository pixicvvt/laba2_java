package minidb;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * создаёт объект-запрос (Statement)
 * Ключевые слова (SELECT, FROM, ...) можно писать в любом регистре
 * Имена таблиц, столбцов и индексов приводятся к маленьким буквам
 */
public class Parser {
    private final ArrayList<String> tokens;
    private int pos = 0;

    /**
     * Режет текст запроса на слова, cам запрос разбирает метод {@link #parse()}.
     * @param sql текст одного запроса
     */
    public Parser(String sql) {
        tokens = tokenize(sql);
    }

    private static ArrayList<String> tokenize(String sql) {
        ArrayList<String> result = new ArrayList<>();
        int i = 0;
        while (i < sql.length()) {
            char c = sql.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
            } else if (Character.isLetter(c) || c == '_') {
                int start = i;
                while (i < sql.length() && (Character.isLetterOrDigit(sql.charAt(i)) || sql.charAt(i) == '_')) {
                    i++;
                }
                result.add(sql.substring(start, i));
            } else if (Character.isDigit(c)
                    || (c == '-' && i + 1 < sql.length() && Character.isDigit(sql.charAt(i + 1)))) {
                int start = i;
                i++;
                while (i < sql.length() && (Character.isDigit(sql.charAt(i)) || sql.charAt(i) == '.')) {
                    i++;
                }
                result.add(sql.substring(start, i));
            } else if (c == '\'') {
                int end = sql.indexOf('\'', i + 1);
                if (end == -1) throw new DbException("Syntax error: quote is not closed");
                result.add(sql.substring(i, end + 1));
                i = end + 1;
            } else if (i + 1 < sql.length() && isTwoCharOperator(sql.substring(i, i + 2))) {
                result.add(sql.substring(i, i + 2));
                i += 2;
            } else if ("(),*;=<>".indexOf(c) != -1) {
                result.add(String.valueOf(c));
                i++;
            } else {
                throw new DbException("Syntax error: unexpected character '" + c + "'");
            }
        }
        return result;
    }

    private static boolean isTwoCharOperator(String s) {
        return s.equals("<=") || s.equals(">=") || s.equals("!=") || s.equals("<>");
    }

    /** Текущий токен (пустая строка, если запрос закончился) */
    private String peek() {
        if (pos < tokens.size()) return tokens.get(pos);
        return "";
    }

    /** Если текущий токен равен word (регистр не важен), берёт его и возвращает true */
    private boolean accept(String word) {
        if (peek().equalsIgnoreCase(word)) {
            pos++;
            return true;
        }
        return false;
    }

    /** То же, но если слова нет, то ошибка */
    private void expect(String word) {
        if (!accept(word)) throw error("expected " + word);
    }

    /** Читает имя (таблицы, столбца, индекса) и приводит его к маленьким буквам */
    private String readName() {
        String t = peek();
        if (t.isEmpty() || !(Character.isLetter(t.charAt(0)) || t.charAt(0) == '_')) {
            throw error("expected a name");
        }
        pos++;
        return t.toLowerCase();
    }

    private DbException error(String what) {
        String t = peek();
        if (t.isEmpty()) return new DbException("Syntax error: " + what + " at end of query");
        return new DbException("Syntax error: " + what + " near '" + t + "'");
    }

    /**
     * Разбирает один запрос (точка с запятой в конце допускается)
     * @return объект-запрос, который можно выполнить
     * @throws DbException если запрос написан неправильно
     */
    public Statement parse() {
        Statement result;
        if (accept("CREATE")) {
            result = parseCreate();
        } else if (accept("DROP")) {
            expect("TABLE");
            result = new DropTableStatement(readName());
        } else if (accept("INSERT")) {
            result = parseInsert();
        } else if (accept("SELECT")) {
            result = parseSelect();
        } else if (accept("UPDATE")) {
            result = parseUpdate();
        } else if (accept("DELETE")) {
            result = parseDelete();
        } else {
            throw error("unknown command");
        }
        accept(";");
        if (!peek().isEmpty()) throw error("unexpected text");
        return result;
    }

    private Statement parseCreate() {
        if (accept("TABLE")) {
            String tableName = readName();
            expect("(");
            List<Column> columns = new ArrayList<>();
            do {
                String columnName = readName();
                DataType type = DataType.fromName(readName());
                boolean primaryKey = false;
                if (accept("PRIMARY")) {
                    expect("KEY");
                    primaryKey = true;
                }
                columns.add(new Column(columnName, type, primaryKey));
            } while (accept(","));
            expect(")");
            return new CreateTableStatement(tableName, columns);
        }
        if (accept("INDEX")) {
            String indexName = readName();
            expect("ON");
            String tableName = readName();
            expect("(");
            String column = readName();
            expect(")");
            return new CreateIndexStatement(indexName, tableName, column);
        }
        throw error("expected TABLE or INDEX");
    }

    private Statement parseInsert() {
        expect("INTO");
        String tableName = readName();
        List<String> names = null;
        if (accept("(")) {
            names = new ArrayList<>();
            do {
                names.add(readName());
            } while (accept(","));
            expect(")");
        }
        expect("VALUES");
        List<List<Object>> allValues = new ArrayList<>();
        do {
            expect("(");
            List<Object> values = new ArrayList<>();
            do {
                values.add(readValue());
            } while (accept(","));
            expect(")");
            allValues.add(values);
        } while (accept(","));
        return new InsertStatement(tableName, names, allValues);
    }

    private Statement parseSelect() {
        List<String> names = null;
        if (!accept("*")) {
            names = new ArrayList<>();
            do {
                names.add(readName());
            } while (accept(","));
        }
        expect("FROM");
        String tableName = readName();
        return new SelectStatement(tableName, names, readWhere());
    }

    private Statement parseUpdate() {
        String tableName = readName();
        expect("SET");
        Map<String, Object> changes = new LinkedHashMap<>();
        do {
            String column = readName();
            expect("=");
            changes.put(column, readValue());
        } while (accept(","));
        return new UpdateStatement(tableName, changes, readWhere());
    }

    private Statement parseDelete() {
        expect("FROM");
        String tableName = readName();
        return new DeleteStatement(tableName, readWhere());
    }

    /** Читает необязательное "WHERE столбец операция значение". Без WHERE возвращает null */
    private Condition readWhere() {
        if (!accept("WHERE")) return null;
        String column = readName();
        String symbol = peek();
        if (!(symbol.equals("=") || symbol.equals("!=") || symbol.equals("<>") || symbol.equals("<")
                || symbol.equals("<=") || symbol.equals(">") || symbol.equals(">="))) {
            throw error("expected a comparison sign");
        }
        pos++;
        return new Condition(column, Op.fromSymbol(symbol), readValue());
    }

    /** Читает значение: число, 'строку', TRUE или FALSE */
    private Object readValue() {
        String t = peek();
        if (t.startsWith("'")) {
            pos++;
            return t.substring(1, t.length() - 1);
        }
        if (t.equalsIgnoreCase("TRUE")) {
            pos++;
            return Boolean.TRUE;
        }
        if (t.equalsIgnoreCase("FALSE")) {
            pos++;
            return Boolean.FALSE;
        }
        if (!t.isEmpty() && (Character.isDigit(t.charAt(0)) || t.charAt(0) == '-')) {
            pos++;
            try {
                if (t.contains(".")) return Double.valueOf(t);
                return Long.valueOf(t);
            } catch (NumberFormatException e) {
                throw new DbException("Syntax error: wrong number " + t);
            }
        }
        throw error("expected a value");
    }
}