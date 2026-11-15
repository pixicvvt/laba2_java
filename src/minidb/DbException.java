package minidb;

/**
 * Ошибка базы данных. Её текст показывается пользователю:
 * неверный запрос, повтор первичного ключа, неизвестная таблица и т.д.
 */
public class DbException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Создаёт ошибку с понятным текстом.
     * @param message текст ошибки для пользователя
     */
    public DbException(String message) {
        super(message);
    }

    /**
     * Создаёт ошибку и запоминает её причину (например, ошибку чтения файла).
     * @param message текст ошибки для пользователя
     * @param cause исходная ошибка
     */
    public DbException(String message, Throwable cause) {
        super(message, cause);
    }
}
