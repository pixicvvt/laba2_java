package minidb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * База данных: набор таблиц (паттерн Repository). Выполняет изменения таблиц
 * и после каждого изменения сохраняет таблицу через TableStorage.
 */
public class Database {
    /**
     * Хранилище, через которое таблицы сохраняются на диск.
     */
    private final TableStorage storage;
    /**
     * Все таблицы: имя таблицы -> таблица.
     */
    private final HashMap<String, Table> tables = new HashMap<>();

    /**
     * Открывает базу, хранящуюся в папке, и загружает все таблицы.
     * @param directory путь к папке с данными (создаётся, если её нет)
     */
    public Database(String directory) {
        this(new FileTableStorage(directory));
    }

    /**
     * Открывает базу на основе любого хранилища и загружает все таблицы.
     * @param storage хранилище таблиц
     */
    public Database(TableStorage storage) {
        this.storage = storage;
        for (Table table : storage.loadAll()) {
            tables.put(table.getName(), table);
        }
    }

    /**
     * Находит таблицу по имени.
     * @param name имя таблицы
     * @return таблица
     * @throws DbException если такой таблицы нет
     */
    public Table getTable(String name) {
        Table table = tables.get(name);
        if (table == null) throw new DbException("No such table: " + name);
        return table;
    }

    /**
     * Добавляет новую таблицу и сохраняет её.
     * @param table созданная таблица
     * @throws DbException если таблица с таким именем уже есть
     */
    public void createTable(Table table) {
        if (tables.containsKey(table.getName())) {
            throw new DbException("Table already exists: " + table.getName());
        }
        tables.put(table.getName(), table);
        storage.save(table);
    }

    /**
     * Удаляет таблицу вместе с её файлом.
     * @param name имя таблицы
     * @throws DbException если такой таблицы нет
     */
    public void dropTable(String name) {
        getTable(name);
        tables.remove(name);
        storage.delete(name);
    }

    /**
     * Создаёт индекс по столбцу таблицы и сохраняет таблицу.
     * @param indexName имя индекса (уникально во всей базе)
     * @param tableName имя таблицы
     * @param column имя столбца
     * @throws DbException если индекс уже есть или нет такой таблицы или столбца
     */
    public void createIndex(String indexName, String tableName, String column) {
        for (Table t : tables.values()) {
            if (t.hasIndex(indexName)) throw new DbException("Index already exists: " + indexName);
        }
        Table table = getTable(tableName);
        table.createIndex(indexName, column);
        storage.save(table);
    }

    /**
     * Вставляет строки. Сначала проверяются ВСЕ строки (ключ не пустой, не повторяется
     * ни в таблице, ни внутри самого запроса), и только потом они добавляются.
     * Поэтому при ошибке не добавляется ни одной строки.
     * @param table таблица
     * @param rows новые строки
     * @throws DbException если первичный ключ пустой или повторяется
     */
    public void insertRows(Table table, List<Row> rows) {
        HashSet<Object> keys = new HashSet<>();
        for (Row row : rows) {
            Object pk = row.get(table.getPkIndex());
            if (pk == null) {
                throw new DbException("Primary key '" + table.getColumn(table.getPkIndex()).getName()
                        + "' must not be empty");
            }
            if (table.hasKey(pk) || !keys.add(pk)) {
                throw new DbException("Duplicate primary key value: " + pk);
            }
        }
        for (Row row : rows) {
            table.insertRow(row);
        }
        storage.save(table);
    }

    /**
     * Присваивает новые значения указанным строкам.
     * @param table таблица
     * @param rows строки, которые нужно изменить
     * @param changes номер столбца -> новое значение
     * @throws DbException если пытаются изменить первичный ключ
     */
    public void update(Table table, List<Row> rows, Map<Integer, Object> changes) {
        if (changes.containsKey(table.getPkIndex())) {
            throw new DbException("Primary key column cannot be updated");
        }
        for (Row row : rows) {
            for (Map.Entry<Integer, Object> entry : changes.entrySet()) {
                table.setValue(row, entry.getKey(), entry.getValue());
            }
        }
        storage.save(table);
    }

    /**
     * Удаляет указанные строки и сохраняет таблицу.
     * @param table таблица
     * @param rows строки, которые нужно удалить
     */
    public void delete(Table table, List<Row> rows) {
        for (Row row : new ArrayList<>(rows)) {
            table.deleteRow(row);
        }
        storage.save(table);
    }
}
