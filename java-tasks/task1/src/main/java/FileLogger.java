import java.io.IOException;
import java.util.logging.*;

/**
 * класс для логирования событий приложения в файл.
 * Этот класс предоставляет простой интерфейс для записи логов в файл
 * с использованием стандартного механизма логирования Java
 * Запись логов производится в файл "application.log" в текущей директории
 */
public class FileLogger {
    /**
     * Единственный экземпляр логгера
     */
    private static FileLogger instance;
    /**
     * Внутренний логгер Java для фактической записи
     */
    private Logger logger;
    /**
     * Обработчик для записи в файл
     */
    private FileHandler fileHandler;

    /**
     * Приватный конструктор
     * Инициализирует логгер и файловый обработчик.

     * В процессе инициализации:
     * Создается или получается экземпляр Logger с именем
     * "ApplicationLogge"
     *  Создается FileHandler для записи в "application.log"
     *  Устанавливается SimpleFormatter для читаемого
     *   форматирования
     * Устанавливается уровень логирования
     * В случае ошибки создания файла, сообщение выводится ошибка
     * и логирование будет недоступно.
     */
    private FileLogger() {
        try {
            logger = Logger.getLogger("ApplicationLogger");
            logger.setUseParentHandlers(false);

            fileHandler = new FileHandler("application.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.ALL);

            logger.addHandler(fileHandler);
            logger.setLevel(Level.ALL);

        } catch (IOException e) {
            System.err.println("Не удалось создать файл лога: " + e.getMessage());
        }
    }
    /**
     * Возвращает единственный экземпляр логгера.
     * Реализует ленивую инициализацию - экземпляр создается только при первом вызове.
     * @return единственный экземпляр
     */
    public static FileLogger getInstance() {
        if (instance == null) {
            instance = new FileLogger();
        }
        return instance;
    }

    /**
     * Записывает информационное сообщение в лог-файл
     * Сообщение будет иметь уровень INFO
     * @param message текст информационного сообщения
     */
    public void info(String message) {
        logger.info(message);
    }

    /**
     * Записывает сообщение об ошибке в лог-файл вместе с исключением
     * Сообщение будет иметь уровень SEVERE
     * @param message текст сообщения об ошибке
     * @param throwable объект исключения для логирования стектрейса (
     */
    public void error(String message, Throwable throwable) {
        logger.log(Level.SEVERE, message, throwable);
    }

    /**
     * Закрывает файловый обработчик и освобождает ресурсы.
     * После вызова этого метода запись логов будет невозможна,
     * и для продолжения логирования потребуется создать новый экземпляр
     */
    public void close() {
        if (fileHandler != null) {
            fileHandler.close();
        }
    }
}