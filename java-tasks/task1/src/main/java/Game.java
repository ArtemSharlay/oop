import java.io.IOException;
import java.util.Random;

/**
 * Основной класс игры "Быки и коровы".
 * Управляет игровым процессом, включая настройку длины слова,
 * генерацию секретного слова, обработку попыток игрока и определение результата.
 * Игровой процесс:
 * Запрос длины слова у игрока (от 3 до 6 символов)
 * Генерация случайного секретного слова указанной длины
 * Цикл попыток угадать слово
 * Анализ каждой попытки через
 * Вывод результата (победа/поражение)
 */

public class Game {

    /* Максимальное количество попыток для угадывания слова */
    public static final int attempts_amount = 3;
    /* Экземпляр логгера для записи событий игры */
    private static FileLogger logger = null;
    /* Текущий результат последней попытки */
    private static GameResult result;
    /* Длина загаданного слова (вводится игроком) */
    private int word_length;
    /* Секретное слово, которое нужно угадать */
    private String secret_word;
    /* Компаратор для сравнения попыток с секретным словом */
    private Comparer comparer;
    /**
     * Запускает основной игровой процесс.
     * Последовательность действий:
     *   Инициализация логгера
     *   Запрос длины слова у игрока
     *   Установка длины слова с проверкой времени ввода
     *   Генерация секретного слова
     *   Инициализация компаратора
     *   Цикл попыток угадывания
     *   Вывод результата игры
     * В случае ошибок ввода или обработки, ошибки логируются и игра
     * завершается.
     */
    public void StartGame() {
        logger = FileLogger.getInstance();
        logger.info("secret word Length set");
        AskWordLength();
        try {
            SetWordLength();
        } catch (Exception e) {
            logger.error("Something wrong with word length input", e);
            logger.close();
            return;
        }
        logger.info("set secret word ");
        SetSecretWord();
        logger.info("set comparer");
        SetComparer();

        int attempts_counter = 0;
        result = new GameResult(0, 0);
        logger.info("start game loop with attempts:" + attempts_amount);

        try {
            while (attempts_counter < attempts_amount && !EndGame(result)) {
                logger.info("loop:" + (attempts_counter + 1));
                AskWordTry();
                logger.info("processing guess word");
                result = ProcessTry();
                attempts_counter++;
            }
        } catch (Exception e) {
            logger.error("Something wrong with guess word processing", e);
            logger.close();
            return;
        }

        logger.info("print result");
        if (result.GetBulls() == word_length) {
            System.out.println("WIN");
        } else {
            System.out.println("LOSE");
        }
    }
    /**
     * Выводит запрос для ввода длины слова.
     */
    private void AskWordLength() {
        System.out.println("Enter word length");
    }
    /**
     * Считывает и устанавливает длину слова с таймаутом.
     *  Использует TimeLimitedInput для чтения с ограничением по времени
     *  Преобразует введенную строку в целое число
     *  Проверяет, что длина находится в диапазоне от 3 до 6 включительно
     * @throws Exception если:
     *  Ввод не является числом
     *  Число вне допустимого диапазона (3-6)
     */
    private void SetWordLength() throws Exception {

        TimeLimitedInput reader =
                new TimeLimitedInput();
        try {
            word_length = Integer.parseInt(reader.ReadWithTimeout());
            if (word_length < 3 || word_length > 6) {
                throw new Exception("Must be in bounds from 3 to 6");
            }

            System.out.println(word_length);
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * Генерирует случайное секретное слово указанной длины
     * Слово состоит из случайных цифр (0-9)
     * Длина слова определяется полем word_length
     */
    private void SetSecretWord() {
        StringBuilder s_word = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < word_length; i++) {
            s_word.append(random.nextInt(10));
        }
        secret_word = s_word.toString();
       // System.out.println(s_word);
    }
    /**
     * Выводит запрос для ввода слова-попытки
     */
    private void AskWordTry() {
        System.out.println("Enter word");
    }

    /**
     * Инициализирует компаратор с текущим секретным словом
     * Создает экземпляр Comparer для сравнения попыток
     */
    private void SetComparer() {
        comparer = new Comparer(secret_word);
    }

    /**
     * Обрабатывает попытку угадывания слова.
     *
     * Процесс:
     * Считывает слово-попытку с таймаутом через TimeLimitedInput
     * Проверяет длину введенного слова
     * Если длина превышает ожидаемую, обрезает до нужной длины
     * Передает слово компаратору для анализа
     * @return результат анализа попытки (количество быков и коров)
     * @throws Exception если возникла ошибка при чтении ввода
     * @see Comparer#ProccessWordTry(String, FileLogger)
     */
    private GameResult ProcessTry() throws IOException, InterruptedException {
        TimeLimitedInput reader =
                new TimeLimitedInput();
        try {
            String word_try = reader.ReadWithTimeout();
            if (word_try == null){
                return new GameResult(0,0);
            }
            else if  (word_try.length() < word_length){
                System.out.println("Not enough symbols");
                return new GameResult(0,0);
            }
            else if  (word_try.length() > word_length){
                System.out.println("Too many symbols, used only first " + word_length);
                return comparer.ProccessWordTry(word_try.substring(0,
                        word_length), logger);
            }
            return comparer.ProccessWordTry(word_try, logger);

        } catch (Exception e) {
            throw e;
        }

    }
    /**
     * Проверяет, закончена ли игра.
     * Игра считается законченной, если:
     * Количество быков равно длине слова (победа)
     * @param result текущий результат попытки
     * @return true если игра должна быть завершена, иначе false
     */
    private Boolean EndGame(GameResult result) {
        if (result.GetBulls() == word_length) {
            return true;
        }

        result.PrintResult();
        return false;
    }
}
