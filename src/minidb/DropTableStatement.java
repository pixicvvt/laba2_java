package minidb;

/** Запрос DROP TABLE имя */
public class DropTableStatement implements Statement {
    private final String tableName;

    /**
     * Создаёт запрос
     * @param tableName имя удаляемой таблицы
     */
    public DropTableStatement(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public Result execute(Database db) {
        db.dropTable(tableName);
        return Result.message("Table '" + tableName + "' dropped.");
    }
}