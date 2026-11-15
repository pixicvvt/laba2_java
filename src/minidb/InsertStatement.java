package minidb;

import java.util.ArrayList;
import java.util.List;

/**
 * Запрос INSERT INTO таблица [(столбцы)] VALUES (...), (...).
 * Столбцы, которых нет в списке остаются пустыми (null).
 */
public class InsertStatement implements Statement {
    private final String tableName;
    private final List<String> names;
    private final List<List<Object>> allValues;

    /**
     * Создаёт запрос
     * @param tableName имя таблицы
     * @param names имена столбцов 
     * @param allValues значения по одному списку на каждую вставляемую строку
     */
    public InsertStatement(String tableName, List<String> names, List<List<Object>> allValues) {
        this.tableName = tableName;
        this.names = names;
        this.allValues = allValues;
    }

    @Override
    public Result execute(Database db) {
        Table table = db.getTable(tableName);
        int columnCount = table.getColumns().size();

        int[] cols;
        if (names == null) {
            cols = new int[columnCount];
            for (int i = 0; i < columnCount; i++) cols[i] = i;
        } else {
            cols = new int[names.size()];
            for (int i = 0; i < cols.length; i++) cols[i] = table.columnIndex(names.get(i));
        }

        List<Row> rows = new ArrayList<>();
        for (List<Object> values : allValues) {
            if (values.size() != cols.length) {
                throw new DbException("Expected " + cols.length + " values but got " + values.size());
            }
            Object[] cells = new Object[columnCount];
            for (int i = 0; i < cols.length; i++) {
                DataType type = table.getColumn(cols[i]).getType();
                cells[cols[i]] = type.convert(values.get(i));
            }
            rows.add(new Row(cells));
        }
        db.insertRows(table, rows);
        return Result.message(rows.size() + " row(s) inserted.");
    }
}