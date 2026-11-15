package minidb;

import java.io.Serializable;

/** Одна строка таблицы: массив значений, по одному на каждый столбец. Пустая ячейка - null. */
public class Row implements Serializable {
    /**
     * Номер версии формата файла (нужен для сериализации).
     */
    private static final long serialVersionUID = 1L;

    /**
     * Значения ячеек строки по порядку столбцов.
     */
    private final Object[] values;

    /**
     * Создаёт строку.
     * @param values значения по одному на каждый столбец (пустая ячейка - null)
     */
    public Row(Object[] values) {
        this.values = values;
    }

    /**
     * Возвращает значение ячейки.
     * @param column номер столбца
     * @return значение или null
     */
    public Object get(int column) {
        return values[column];
    }

    /**
     * Записывает значение в ячейку.
     * @param column номер столбца
     * @param value новое значение
     */
    public void set(int column, Object value) {
        values[column] = value;
    }
}
