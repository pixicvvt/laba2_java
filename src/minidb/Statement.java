package minidb;

/** Разобранный SQL-запрос, который можно выполнить */
public interface Statement {
    /**
     * выполняет запрос
     * @param db база данных
     * @return результат выполнения
     * @throws DbException если запрос нельзя выполнить
     */
    Result execute(Database db);
}