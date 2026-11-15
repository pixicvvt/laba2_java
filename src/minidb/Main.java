package minidb;

import java.util.Scanner;

/**
 * консольный интерфейс, читает запросы (каждый заканчивается знаком ;),
 * выполняет их и печатает результат. Любые ошибки выводятся как "Error: ...",
 * программа при этом не падает. Выход команда EXIT.
 */
public class Main {
    private Main() {
    }

    /**
     * Запускает консоль
     * @param args пусто или "-d папка" папка для файлов данных
     */
    public static void main(String[] args) {
        String dir = "data";
        if (args.length == 2 && args[0].equals("-d")) {
            dir = args[1];
        } else if (args.length != 0) {
            System.out.println("Usage: java -cp out minidb.Main [-d <data dir>]");
            return;
        }

        Database db;
        try {
            db = new Database(dir);
        } catch (DbException e) {
            System.out.println("Cannot open database: " + e.getMessage());
            return;
        }

        System.out.println("MiniDB. Data dir: " + dir + ". Type SQL ending with ';' or EXIT.");
        Scanner scanner = new Scanner(System.in);
        String text = "";
        while (true) {
            if (text.trim().isEmpty()) {
                System.out.print("minidb> ");
            } else {
                System.out.print("     -> ");
            }
            if (!scanner.hasNextLine()) break;
            String line = scanner.nextLine();
            if (text.trim().isEmpty() && (line.trim().equalsIgnoreCase("exit") || line.trim().equalsIgnoreCase("quit"))) {
                break;
            }
            text = runAll(text + line + "\n", db);
        }
    }

    /**
     * Находит в тексте законченные запросы и выполняет их
     * Возвращает остаток текста начало ещё не законченного запроса
     */
    private static String runAll(String text, Database db) {
        int start = 0;
        boolean inQuotes = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\'') {
                inQuotes = !inQuotes;
            } else if (c == ';' && !inQuotes) {
                runOne(text.substring(start, i + 1), db);
                start = i + 1;
            }
        }
        return text.substring(start);
    }

    private static void runOne(String sql, Database db) {
        if (sql.trim().equals(";")) return;
        try {
            Statement statement = new Parser(sql).parse();
            Result result = statement.execute(db);
            System.out.println(TablePrinter.format(result));
        } catch (DbException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println("Internal error: " + e);
        }
    }
}