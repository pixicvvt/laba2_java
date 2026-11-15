package minidb;

import java.io.Serializable;

/** Описание одного столбца таблицы: имя, тип и признак первичного ключа. */
public class Column implements Serializable {
    /**
     * Номер версии формата файла (нужен для сериализации).
     */
    private static final long serialVersionUID = 1L;

    /**
     * Имя столбца.
     */
    private final String name;
    /**
     * Тип данных столбца.
     */
    private final DataType type;
    /**
     * true, если столбец - первичный ключ.
     */
    private final boolean primaryKey;

    /**
     * Создаёт описание столбца.
     * @param name имя столбца
     * @param type тип данных
     * @param primaryKey true, если это первичный ключ
     */
    public Column(String name, DataType type, boolean primaryKey) {
        this.name = name;
        this.type = type;
        this.primaryKey = primaryKey;
    }

    /**
     * Возвращает: имя столбца.
     * @return имя столбца
     */
    public String getName() {
        return name;
    }

    /**
     * Возвращает: тип данных столбца.
     * @return тип данных столбца
     */
    public DataType getType() {
        return type;
    }

    /**
     * Возвращает: true, если столбец - первичный ключ.
     * @return true, если столбец - первичный ключ
     */
    public boolean isPrimaryKey() {
        return primaryKey;
    }
}
