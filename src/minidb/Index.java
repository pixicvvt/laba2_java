package minidb;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Индекс по одному столбцу. Внутри - TreeMap (отсортированная карта):
 * значение столбца -> список первичных ключей строк с этим значением.
 * Благодаря сортировке можно быстро искать и по равенству, и по диапазону (&gt;, &lt;, &gt;=, &lt;=),
 * не просматривая всю таблицу.
 */
public class Index implements Serializable {
    /**
     * Номер версии формата файла (нужен для сериализации).
     */
    private static final long serialVersionUID = 1L;

    /**
     * Имя индекса.
     */
    private final String name;
    /**
     * Имя столбца, по которому построен индекс.
     */
    private final String column;
    /**
     * Отсортированная карта: значение столбца -> первичные ключи строк.
     */
    private final TreeMap<Object, ArrayList<Object>> map = new TreeMap<>();

    /**
     * Создаёт пустой индекс.
     * @param name имя индекса
     * @param column имя столбца
     */
    public Index(String name, String column) {
        this.name = name;
        this.column = column;
    }

    /**
     * Возвращает: имя индекса.
     * @return имя индекса
     */
    public String getName() {
        return name;
    }

    /**
     * Возвращает: имя столбца, по которому построен индекс.
     * @return имя столбца, по которому построен индекс
     */
    public String getColumn() {
        return column;
    }

    /**
     * Индекс умеет отвечать на все операции, кроме "не равно".
     * @param op операция сравнения из условия
     * @return true, если индекс подходит для этой операции
     */
    public static boolean canUse(Op op) {
        return op != Op.NE;
    }

    /**
     * Запоминает, что у строки с первичным ключом pk в этом столбце значение key.
     * @param key значение столбца
     * @param pk первичный ключ строки
     */
    public void add(Object key, Object pk) {
        if (key == null) return;
        ArrayList<Object> list = map.get(key);
        if (list == null) {
            list = new ArrayList<>();
            map.put(key, list);
        }
        list.add(pk);
    }

    /**
     * Забывает строку pk со значением key.
     * @param key значение столбца
     * @param pk первичный ключ строки
     */
    public void remove(Object key, Object pk) {
        if (key == null) return;
        ArrayList<Object> list = map.get(key);
        if (list != null) {
            list.remove(pk);
            if (list.isEmpty()) map.remove(key);
        }
    }

    /**
     * Возвращает первичные ключи всех строк, у которых выполняется условие "значение операция key".
     * @param op операция сравнения (=, &lt;, &lt;=, &gt;, &gt;=)
     * @param key значение из запроса
     * @return список первичных ключей подходящих строк
     */
    public ArrayList<Object> find(Op op, Object key) {
        ArrayList<Object> result = new ArrayList<>();
        if (op == Op.EQ) {
            ArrayList<Object> list = map.get(key);
            if (list != null) result.addAll(list);
            return result;
        }
        Map<Object, ArrayList<Object>> part;
        if (op == Op.LT) {
            part = map.headMap(key, false);
        } else if (op == Op.LE) {
            part = map.headMap(key, true);
        } else if (op == Op.GT) {
            part = map.tailMap(key, false);
        } else {
            part = map.tailMap(key, true);
        }
        for (ArrayList<Object> list : part.values()) {
            result.addAll(list);
        }
        return result;
    }
}
