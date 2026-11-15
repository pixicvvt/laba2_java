package minidb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Запрос UPDATE таблица SET столбец=значение, ... [WHERE условие] */
public class UpdateStatement implements Statement {
    private final String tableName;
    private final Map<String, Object> changes;
    private final Condition where;

    /**
     * Создаёт запрос
     * @param tableName имя таблицы
     * @param changes новые значения: имя столбца - значение
     * @param where условие (null - изменить все строки)
     */
    public UpdateStatement(String tableName, Map<String, Object> changes, Condition where) {
        this.tableName = tableName;
        this.changes = changes;
        this.where = where;
    }

    @Override
    public Result execute(Database db) {
        Table table = db.getTable(tableName);

        Map<Integer, Object> newValues = new HashMap<>();
        for (Map.Entry<String, Object> entry : changes.entrySet()) {
            int col = table.columnIndex(entry.getKey());
            newValues.put(col, table.getColumn(col).getType().convert(entry.getValue()));
        }

        List<Row> rows = table.select(where);
        db.update(table, rows, newValues);
        return Result.message(rows.size() + " row(s) updated.");
    }
}