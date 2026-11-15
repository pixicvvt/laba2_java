package minidb;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
/**
 * Типы данных столбцов. В Java значения хранятся так:
 * INT - Long, DOUBLE - Double, STRING - String, DATE - LocalDate, BOOLEAN - Boolean.
 */
public enum DataType {
    /** целое числ */
    INT,
    /** число с плавающей точкой */
    DOUBLE,
    /** строка */
    STRING,
    /** дата */
    DATE,
    /** true или false */
    BOOLEAN;

    /**
     * Находит тип по имени из запроса CREATE TABLE 
     * @param name имя типа, например "int" или "STRING"
     * @return найденный тип
     */
    public static DataType fromName(String name) {
        String n = name.toUpperCase();
        if (n.equals("INT")) return INT;
        if (n.equals("DOUBLE")) return DOUBLE;
        if (n.equals("STRING")) return STRING;
        if (n.equals("DATE")) return DATE;
        if (n.equals("BOOLEAN")) return BOOLEAN;
        throw new DbException("Unknown data type: " + name + " (use INT, DOUBLE, STRING, DATE or BOOLEAN)");
    }

    /**
     * проверяет, что значение подходит к типу, и при необходимости преобразует его:
     * целое число превращается в дробное для DOUBLE, строка 'гггг-мм-дд' - в дату для DATE.
     * Значение null (пустая ячейка) подходит к любому типу.
     * @param value значение из запроса
     * @return значение нужного Java-типа
     */
    public Object convert(Object value) {
        if (value == null) return null;
        if (this == INT && value instanceof Long) return value;
        if (this == DOUBLE && value instanceof Double) return value;
        if (this == DOUBLE && value instanceof Long) return Double.valueOf(((Long) value).doubleValue());
        if (this == STRING && value instanceof String) return value;
        if (this == BOOLEAN && value instanceof Boolean) return value;
        if (this == DATE && value instanceof String) {
            try {
                return LocalDate.parse((String) value);
            } catch (DateTimeParseException e) {
                throw new DbException("Wrong date (use yyyy-MM-dd): " + value);
            }
        }
        throw new DbException("Value " + value + " does not fit type " + this);
    }

    /**
     * сравнивает два значения этого типа.
     * @param a первое значение
     * @param b второе значение
     * @return число меньше нуля, если a меньше b; ноль, если равны; больше нуля, если a больше b
     */
    public int compare(Object a, Object b) {
        if (this == INT) return ((Long) a).compareTo((Long) b);
        if (this == DOUBLE) return ((Double) a).compareTo((Double) b);
        if (this == STRING) return ((String) a).compareTo((String) b);
        if (this == DATE) return ((LocalDate) a).compareTo((LocalDate) b);
        return ((Boolean) a).compareTo((Boolean) b);
    }
}
