package minidb;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Хранит каждую таблицу в отдельном файле "имя.tbl" (вместе со строками и индексами),
 * используя сериализацию Java.
 *
 * Защита от сбоя: сначала данные пишутся во временный файл "имя.tbl.tmp" и только после
 * успешной записи заменяют основной файл. Если программа упала посреди записи,
 * основной файл остаётся целым, а временный удаляется при следующем запуске.
 */
public class FileTableStorage implements TableStorage {
    private final File dir;

    /**
     * Открывает папку с данными (создаёт её, если нет).
     * @param path путь к папке
     */
    public FileTableStorage(String path) {
        dir = new File(path);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new DbException("Cannot create data dir " + path);
        }
    }

    @Override
    public void save(Table table) {
        File target = new File(dir, table.getName() + ".tbl");
        File temp = new File(dir, table.getName() + ".tbl.tmp");
        try (ObjectOutputStream out = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(temp)))) {
            out.writeObject(table);
        } catch (IOException e) {
            throw new DbException("Cannot write file " + temp.getName(), e);
        }
        try {
            Files.move(temp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new DbException("Cannot replace file " + target.getName(), e);
        }
    }

    @Override
    public void delete(String tableName) {
        File file = new File(dir, tableName + ".tbl");
        if (file.exists() && !file.delete()) {
            throw new DbException("Cannot delete file " + file.getName());
        }
    }

    @Override
    public List<Table> loadAll() {
        List<Table> tables = new ArrayList<>();
        File[] files = dir.listFiles();
        if (files == null) return tables;
        for (File file : files) {
            if (file.getName().endsWith(".tmp")) {
                file.delete();
            } else if (file.getName().endsWith(".tbl")) {
                tables.add(readTable(file));
            }
        }
        return tables;
    }

    private Table readTable(File file) {
        try (ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(file)))) {
            return (Table) in.readObject();
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            throw new DbException("File " + file.getName() + " is damaged: " + e, e);
        }
    }
}
