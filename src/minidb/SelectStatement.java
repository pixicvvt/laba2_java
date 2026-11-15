package minidb;

import java.util.ArrayList;
import java.util.List;

/** Запрос SELECT * FROM таблица [WHERE условие] */
public class SelectStatement implements Statement {
    private final String tableName;
    private final List<String> names;
    private final Condition where;

    /**
     * Создаёт запрос
     * @param tableName имя таблицы
     * @param names имена столбцов (null - все столбцы)
     * @param where условие (null - все строки)
     */
    public SelectStatement(String tableName, List<String> names, Condition where) {
        this.tableName = tableName;
        this.names = names;
        this.where = where;
    }

    @Override
    public Result execute(Database db) {
        Table table = db.getTable(tableName);

        List<String> titles = new ArrayList<>();
        List<Integer> numbers = new ArrayList<>();
        if (names == null) {
            for (int i = 0; i < table.getColumns().size(); i++) {
                titles.add(table.getColumn(i).getName());
                numbers.add(i);
            }
        } else {
            for (String name : names) {
                titles.add(name);
                numbers.add(table.columnIndex(name));
            }
        }

        List<Object[]> result = new ArrayList<>();
        for (Row row : table.select(where)) {
            Object[] cells = new Object[numbers.size()];
            for (int i = 0; i < cells.length; i++) {
                cells[i] = row.get(numbers.get(i));
            }
            result.add(cells);
        }
        return Result.table(titles, result, result.size() + " row(s)");
    }
}