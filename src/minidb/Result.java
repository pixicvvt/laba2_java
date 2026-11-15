package minidb;

import java.util.List;

/** результат запроса: либо таблица (названия столбцов и строки), либо просто сообщение */
public class Result {
    /**
     * названия столбцов 
     */
    private final List<String> columns;
    /**
     * строки результата
     */
    private final List<Object[]> rows;
    /**
     * сообщение под таблицей или вместо неё
     */
    private final String message;

    private Result(List<String> columns, List<Object[]> rows, String message) {
        this.columns = columns;
        this.rows = rows;
        this.message = message;
    }

    /**
     * результат без таблицы, например "3 row(s) deleted"
     * @param message текст сообщения
     * @return результат
     */
    public static Result message(String message) {
        return new Result(null, null, message);
    }

    /**
     * результат с таблицей (для SELECT).
     * @param columns названия столбцов
     * @param rows строки
     * @param message сообщение под таблицей
     * @return результат
     */
    public static Result table(List<String> columns, List<Object[]> rows, String message) {
        return new Result(columns, rows, message);
    }

    /**
     * возвращает названия столбцов или null, если таблицы нет
     * @return названия столбцов или null, если таблицы нет
     */
    public List<String> getColumns() {
        return columns;
    }

    /**
     * возвращает строки результата или null, если таблицы нет
     * @return строки результата или null, если таблицы нет
     */
    public List<Object[]> getRows() {
        return rows;
    }

    /**
     * возвращает текст сообщения
     * @return текст сообщения
     */
    public String getMessage() {
        return message;
    }
}