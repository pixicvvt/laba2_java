package minidb;

import java.util.List;

/** Превращает Result в текст: таблицу из линий и столбцов  */
public class TablePrinter {
    private TablePrinter() {
    }

    /**
     * Превращает результат в текст: таблицу из линий и столбцов
     * @param result результат запроса
     * @return готовый для печати текст
     */
    public static String format(Result result) {
        if (result.getColumns() == null) return result.getMessage();

        List<String> columns = result.getColumns();
        List<Object[]> rows = result.getRows();

        int[] width = new int[columns.size()];
        for (int i = 0; i < columns.size(); i++) {
            width[i] = columns.get(i).length();
        }
        for (Object[] row : rows) {
            for (int i = 0; i < row.length; i++) {
                width[i] = Math.max(width[i], text(row[i]).length());
            }
        }

        StringBuilder sb = new StringBuilder();
        String line = line(width);
        sb.append(line);
        sb.append(rowText(columns.toArray(), width));
        sb.append(line);
        for (Object[] row : rows) {
            sb.append(rowText(row, width));
        }
        sb.append(line);
        sb.append(result.getMessage());
        return sb.toString();
    }

    private static String text(Object value) {
        if (value == null) return "NULL";
        return value.toString();
    }

    private static String line(int[] width) {
        StringBuilder sb = new StringBuilder("+");
        for (int w : width) {
            for (int i = 0; i < w + 2; i++) sb.append('-');
            sb.append('+');
        }
        sb.append('\n');
        return sb.toString();
    }

    private static String rowText(Object[] values, int[] width) {
        StringBuilder sb = new StringBuilder("|");
        for (int i = 0; i < values.length; i++) {
            String s = text(values[i]);
            sb.append(' ').append(s);
            for (int k = s.length(); k < width[i]; k++) sb.append(' ');
            sb.append(" |");
        }
        sb.append('\n');
        return sb.toString();
    }
}