package minidb;

import java.util.List;

/** Запрос DELETE FROM таблица [WHERE условие] */
public class DeleteStatement implements Statement {
    private final String tableName;
    private final Condition where;

    /**
     * Создаёт запрос
     * @param tableName имя таблицы
     * @param where условие (null - удалить все строки)
     */
    public DeleteStatement(String tableName, Condition where) {
        this.tableName = tableName;
        this.where = where;
    }

    @Override
    public Result execute(Database db) {
        Table table = db.getTable(tableName);
        List<Row> rows = table.select(where);
        db.delete(table, rows);
        return Result.message(rows.size() + " row(s) deleted.");
    }
}