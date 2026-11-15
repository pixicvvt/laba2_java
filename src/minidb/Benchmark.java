package minidb;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Замер скорости SELECT без индекса и с индексом
 * Запуск: java -cp out minidb.Benchmark [строк] [повторов]
 */
public class Benchmark {
    private Benchmark() {
    }

    private static final int WARMUP = 10;

    /**
     * Запускает замеры
     * @param args необязательно: число строк и число повторов
     * @throws IOException если не удалось создать временную папку
     */
    public static void main(String[] args) throws IOException {
        int count = 200000;
        int times = 100;
        try {
            if (args.length > 0) count = Integer.parseInt(args[0]);
            if (args.length > 1) times = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Usage: Benchmark [rows] [times] (whole numbers)");
            return;
        }
        if (count < 1000 || times < 1) {
            System.out.println("rows must be at least 1000 and times at least 1");
            return;
        }

        Database db = new Database(Files.createTempDirectory("minidb-bench").toString());
        run(db, "CREATE TABLE students (id INT PRIMARY KEY, name STRING, tuition DOUBLE)");
        Random random = new Random(42);
        List<Row> rows = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            double tuition = Math.round(random.nextDouble() * 10000000) / 100.0;
            rows.add(new Row(new Object[]{Long.valueOf(i), "name_" + (i % 5000), Double.valueOf(tuition)}));
        }
        db.insertRows(db.getTable("students"), rows);

        String[] titles = {"name = 'name_777' (equality)", "tuition > 99900 (range)"};
        String[] queries = {
                "SELECT * FROM students WHERE name = 'name_777'",
                "SELECT * FROM students WHERE tuition > 99900"};
        double[] slow = new double[2];
        double[] fast = new double[2];
        int[] found = new int[2];

        for (int i = 0; i < 2; i++) {
            slow[i] = measure(db, queries[i], times);
        }
        run(db, "CREATE INDEX idx_name ON students (name)");
        run(db, "CREATE INDEX idx_tuition ON students (tuition)");
        for (int i = 0; i < 2; i++) {
            fast[i] = measure(db, queries[i], times);
            found[i] = run(db, queries[i]).getRows().size();
        }

        System.out.println("строки: " + count + ", время на запрос: " + times);
        System.out.println();
        System.out.println("| запрос | строки | без индекса, мс | с индексом,мс |");
        for (int i = 0; i < 2; i++) {
            System.out.println(String.format("| %s | %d| %.3f | %.3f|",
                    titles[i], found[i], slow[i], fast[i]));
        }
    }

    /** Среднее время одного запроса в миллисекундах */
    private static double measure(Database db, String sql, int times) {
        for (int i = 0; i < WARMUP; i++) {
            run(db, sql);
        }
        long start = System.nanoTime();
        for (int i = 0; i < times; i++) {
            run(db, sql);
        }
        long end = System.nanoTime();
        return (end - start) / 1000000.0 / times;
    }

    private static Result run(Database db, String sql) {
        return new Parser(sql).parse().execute(db);
    }
}
