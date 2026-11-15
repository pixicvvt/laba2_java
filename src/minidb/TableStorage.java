package minidb;

import java.util.List;

/**
 * Хранилище таблиц (паттерн DAO). Database работает только с этим интерфейсом
 * и не знает, где и в каком формате лежат данные. Сейчас данные хранятся в файлах
 * (FileTableStorage), но можно написать другую реализацию, например с JSON.
 */
public interface TableStorage {
    /**
     * Загружает все сохранённые таблицы.
     * @return список таблиц
     */
    List<Table> loadAll();

    /**
     * Сохраняет таблицу вместе с индексами.
     * @param table таблица
     */
    void save(Table table);

    /**
     * Удаляет сохранённую таблицу.
     * @param tableName имя таблицы
     */
    void delete(String tableName);
}
