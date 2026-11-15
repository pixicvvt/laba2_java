package minidb;

/** Запрос CREATE INDEX имя_индекса ON таблица (столбец) */
public class CreateIndexStatement implements Statement {
    private final String indexName;
    private final String tableName;
    private final String column;

    /**
     * Создаёт запрос
     * @param indexName имя нового индекса
     * @param tableName имя таблицы
     * @param column столбец, по которому строится индекс
     */
    public CreateIndexStatement(String indexName, String tableName, String column) {
        this.indexName = indexName;
        this.tableName = tableName;
        this.column = column;
    }

    @Override
    public Result execute(Database db) {
        db.createIndex(indexName, tableName, column);
        return Result.message("Index '" + indexName + "' created.");
    }
}