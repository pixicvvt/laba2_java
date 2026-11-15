package minidb;

import java.util.List;

/** Запрос CREATE TABLE имя  */
public class CreateTableStatement implements Statement {
    private final String tableName;
    private final List<Column> columns;

    /**
     * создаёт запрос
     * @param tableName имя новой таблицы
     * @param columns описание столбцов
     */
    public CreateTableStatement(String tableName, List<Column> columns) {
        this.tableName = tableName;
        this.columns = columns;
    }

    @Override
    public Result execute(Database db) {
        db.createTable(new Table(tableName, columns));
        return Result.message("Table '" + tableName + "' created.");
    }
}