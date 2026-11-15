package minidb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Таблица: описание столбцов, строки и индексы.
 * Строки лежат в LinkedHashMap, где ключ - значение первичного ключа. Так проверка
 * "такой ключ уже есть?" выполняется мгновенно, а порядок строк сохраняется как при вставке.
 * Индексы хранятся вместе с таблицей и записываются в тот же файл.
 */
public class Table implements Serializable {
    /**
     * Номер версии формата файла (нужен для сериализации).
     */
    private static final long serialVersionUID = 1L;

    /**
     * Имя таблицы.
     */
    private final String name;
    /**
     * Описание столбцов.
     */
    private final ArrayList<Column> columns;
    /**
     * Номер столбца с первичным ключом.
     */
    private final int pkIndex;
    /**
     * Строки: значение первичного ключа -> строка.
     */
    private final LinkedHashMap<Object, Row> rows = new LinkedHashMap<>();
    /**
     * Индексы: имя индекса -> индекс.
     */
    private final HashMap<String, Index> indexes = new HashMap<>();

    /**
     * Создаёт пустую таблицу.
     * @param name имя таблицы
     * @param columns описание столбцов (ровно один должен быть PRIMARY KEY)
     * @throws DbException если имена столбцов повторяются или нет первичного ключа
     */
    public Table(String name, List<Column> columns) {
        this.name = name;
        this.columns = new ArrayList<>(columns);
        int pk = -1;
        for (int i = 0; i < columns.size(); i++) {
            for (int j = 0; j < i; j++) {
                if (columns.get(j).getName().equals(columns.get(i).getName())) {
                    throw new DbException("Duplicate column name: " + columns.get(i).getName());
                }
            }
            if (columns.get(i).isPrimaryKey()) {
                if (pk != -1) throw new DbException("Only one PRIMARY KEY column is allowed");
                pk = i;
            }
        }
        if (pk == -1) throw new DbException("Table '" + name + "' needs a PRIMARY KEY column");
        this.pkIndex = pk;
    }

    /**
     * Возвращает: имя таблицы.
     * @return имя таблицы
     */
    public String getName() {
        return name;
    }

    /**
     * Возвращает: описание всех столбцов.
     * @return описание всех столбцов
     */
    public List<Column> getColumns() {
        return columns;
    }

    /**
     * Возвращает описание столбца.
     * @param i номер столбца
     * @return столбец
     */
    public Column getColumn(int i) {
        return columns.get(i);
    }

    /**
     * Возвращает: номер столбца с первичным ключом.
     * @return номер столбца с первичным ключом
     */
    public int getPkIndex() {
        return pkIndex;
    }

    /**
     * Находит номер столбца по имени.
     * @param columnName имя столбца
     * @return номер столбца
     * @throws DbException если такого столбца нет
     */
    public int columnIndex(String columnName) {
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).getName().equals(columnName)) return i;
        }
        throw new DbException("Unknown column '" + columnName + "' in table '" + name + "'");
    }

    /**
     * Проверяет, есть ли в таблице индекс с таким именем.
     * @param indexName имя индекса
     * @return true, если есть
     */
    public boolean hasIndex(String indexName) {
        return indexes.containsKey(indexName);
    }

    /**
     * Создаёт индекс и сразу заполняет его существующими строками.
     * @param indexName имя индекса
     * @param columnName имя столбца
     * @throws DbException если нет такого столбца или индекс уже есть
     */
    public void createIndex(String indexName, String columnName) {
        int col = columnIndex(columnName);
        if (indexes.containsKey(indexName)) throw new DbException("Index already exists: " + indexName);
        Index index = new Index(indexName, columnName);
        for (Row row : rows.values()) {
            index.add(row.get(col), row.get(pkIndex));
        }
        indexes.put(indexName, index);
    }

    /** Ищет индекс по столбцу. Если индекса нет, возвращает null. */
    private Index findIndex(String columnName) {
        for (Index index : indexes.values()) {
            if (index.getColumn().equals(columnName)) return index;
        }
        return null;
    }

    /**
     * Возвращает строки, подходящие под условие (при where == null - все строки).
     * Если по столбцу из условия есть индекс, строки берутся из индекса,
     * иначе просматривается вся таблица.
     * @param where условие или null
     * @return подходящие строки
     * @throws DbException если столбца нет или значение не подходит по типу
     */
    public ArrayList<Row> select(Condition where) {
        ArrayList<Row> result = new ArrayList<>();
        if (where == null) {
            result.addAll(rows.values());
            return result;
        }
        int col = columnIndex(where.getColumn());
        DataType type = columns.get(col).getType();
        Object value = type.convert(where.getValue());
        if (type == DataType.BOOLEAN && where.getOp() != Op.EQ && where.getOp() != Op.NE) {
            throw new DbException("BOOLEAN can only be compared with = or !=");
        }
        Index index = findIndex(where.getColumn());
        if (index != null && Index.canUse(where.getOp())) {
            for (Object pk : index.find(where.getOp(), value)) {
                result.add(rows.get(pk));
            }
        } else {
            for (Row row : rows.values()) {
                if (where.isTrueFor(row.get(col), value, type)) {
                    result.add(row);
                }
            }
        }
        return result;
    }

    /**
     * Проверяет, есть ли строка с таким первичным ключом.
     * @param pk значение первичного ключа
     * @return true, если есть
     */
    public boolean hasKey(Object pk) {
        return rows.containsKey(pk);
    }

    /**
     * Добавляет строку (проверка уникальности ключа делается в Database) и обновляет индексы.
     * @param row новая строка
     */
    public void insertRow(Row row) {
        Object pk = row.get(pkIndex);
        rows.put(pk, row);
        for (Index index : indexes.values()) {
            index.add(row.get(columnIndex(index.getColumn())), pk);
        }
    }

    /**
     * Удаляет строку и убирает её из индексов.
     * @param row удаляемая строка
     */
    public void deleteRow(Row row) {
        Object pk = row.get(pkIndex);
        rows.remove(pk);
        for (Index index : indexes.values()) {
            index.remove(row.get(columnIndex(index.getColumn())), pk);
        }
    }

    /**
     * Меняет одно значение в строке и обновляет индекс этого столбца.
     * @param row строка
     * @param col номер столбца
     * @param newValue новое значение
     */
    public void setValue(Row row, int col, Object newValue) {
        Object pk = row.get(pkIndex);
        Object oldValue = row.get(col);
        for (Index index : indexes.values()) {
            if (columnIndex(index.getColumn()) == col) {
                index.remove(oldValue, pk);
                index.add(newValue, pk);
            }
        }
        row.set(col, newValue);
    }
}
