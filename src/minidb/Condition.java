package minidb;

/** Условие WHERE вида "столбец операция значение", например: price > 20000. */
public class Condition {
    /**
     * Имя столбца из условия.
     */
    private final String column;
    /**
     * Операция сравнения.
     */
    private final Op op;
    /**
     * Значение, с которым сравниваем.
     */
    private final Object value;

    /**
     * Создаёт условие.
     * @param column имя столбца
     * @param op операция сравнения
     * @param value значение из запроса
     */
    public Condition(String column, Op op, Object value) {
        this.column = column;
        this.op = op;
        this.value = value;
    }

    /**
     * Возвращает: имя столбца.
     * @return имя столбца
     */
    public String getColumn() {
        return column;
    }

    /**
     * Возвращает: операция сравнения.
     * @return операция сравнения
     */
    public Op getOp() {
        return op;
    }

    /**
     * Возвращает: значение из запроса (ещё не приведённое к типу столбца).
     * @return значение из запроса (ещё не приведённое к типу столбца)
     */
    public Object getValue() {
        return value;
    }

    /**
     * Проверяет, подходит ли значение ячейки под условие.
     * @param cell значение из строки таблицы
     * @param other значение из запроса, уже приведённое к типу столбца
     * @param type тип столбца
     * @return true, если условие выполняется
     */
    public boolean isTrueFor(Object cell, Object other, DataType type) {
        if (cell == null) return false;
        int c = type.compare(cell, other);
        if (op == Op.EQ) return c == 0;
        if (op == Op.NE) return c != 0;
        if (op == Op.LT) return c < 0;
        if (op == Op.LE) return c <= 0;
        if (op == Op.GT) return c > 0;
        return c >= 0;
    }
}
