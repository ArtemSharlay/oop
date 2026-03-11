import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.*;

/**
 * Класс для чтения ввода с клавиатуры с ограничением по времени (таймаутом).
 * Этот класс позволяет считывать строку, введенную пользователем, но если
 * пользователь не успевает ввести данные за отведенное время, операция
 * прерывается и возвращается null
 * Таймаут по умолчанию: timeout_seconds секунд
 * Поддерживает прерывание ввода по истечении времени
 */
public class TimeLimitedInput {
    /**
     * Время ожидания ввода в секундах
     */
    private static int timeout_seconds = 10;
    /**
     * Поток ввода
     */
    private InputStream input_stream;

    /**
     * Конструктор по умолчанию.
     * Создает экземпляр для чтения из стандартного потока ввода
     */
    public TimeLimitedInput() {
        this.input_stream = System.in;
    }

    public TimeLimitedInput(InputStream input_stream) {
        if (input_stream == null) {
            throw new NullPointerException("InputStream cannot be null");
        }
        this.input_stream = input_stream;
    }

    /**
     * Читает строку ввода с ограничением по времени.
     * Выводится сообщение о таймере
     * Если данных нет, ожидается их появление в течение таймаута
     * Символы считываются по одному до символа новой строки ('\n')
     * Если время истекло, возвращается null
     * Метод использует неблокирующую проверку
     * для определения наличия данных без блокировки потока.
     *
     * @return введенная строка без символа новой строки, или null если время
     * истекло
     * @throws IOException          если произошла ошибка ввода/вывода
     * @throws InterruptedException если поток был прерван во время ожидания
     */
    public String ReadWithTimeout() throws IOException, InterruptedException {
        StringBuilder input = new StringBuilder();
        long end_time = System.currentTimeMillis() + (timeout_seconds * 1000);
        try {
            System.out.println("timer " + timeout_seconds + " seconds");
            while (System.currentTimeMillis() < end_time) {
                if (input_stream.available() > 0) {
                    int ch = input_stream.read();
                    if (ch == '\n') {
                        return input.toString();
                    } else if (ch != '\r') {
                        input.append((char) ch);
                    }
                } else {
                    Thread.sleep(50);
                }
            }
            System.out.println("\ntimeout");
            return null;
        } catch (Exception e) {
            throw e;
        }
    }
}