import java.util.HashMap;
import java.util.Map;

/**
 * Перечисление возможных цветов для букв в слове.
 * Используется для обозначения результата сравнения букв:
 * Grey буквы нет в загаданном слове
 * Yellow буква есть в слове, но в другой позиции
 * Green буква есть в слове на той же позиции
 */
enum Color {
    Grey, Yellow, Green
}

/* Класс для сравнения попыток угадывания слова с загаданным словом.
 * Реализует логику игры "Быки и коровы" (Bulls and Cows) с цветовой индикацией:
 *   Green (бык) - буква на правильном месте
 *   Yellow (корова) - буква есть в слове, но не на своем месте
 *   Grey - буквы нет в слове
 */

public class Comparer {

    /* Загаданное слово, с которым происходит сравнение */
    private String right_word;

    /**
     * Конструктор класса Comparer.
     * Создает экземпляр для сравнения с указанным загаданным словом.
     *
     * @param word загаданное слово
     */
    public Comparer(String word) {
        if (word == null) {
            throw new NullPointerException("Guess word cant be null");
        }
        this.right_word = word;
    }

    /**
     * Обрабатывает попытку угадать слово.
     * Анализирует предложенное слово, определяет цвета для каждой буквы
     * и подсчитывает количество быков (зеленых) и коров (желтых).
     *
     * @param word_try слово-попытка, предложенное игроком
     * @param logger   экземпляр логгера для записи информации о процессе
     * @return объект GameResult с количеством быков и коров
     */
    public GameResult ProccessWordTry(String word_try, FileLogger logger) {
        logger.info("processing guess word letters color");
        Color[] letter_colors = new Color[right_word.length()];
        GetLetterColor(word_try, letter_colors);

        logger.info("set amount of bulls and cows");
        int counter_bulls = 0;
        int counter_cows = 0;
        for (int i = 0; i < right_word.length(); i++) {
            if (letter_colors[i] == Color.Green) {
                counter_bulls++;
            } else if (letter_colors[i] == Color.Yellow) {
                counter_cows++;
            }
        }

        return new GameResult(counter_bulls, counter_cows);
    }

    /**
     * Проверяет буквы, которые находятся на правильных позициях (зеленые).
     * Для каждой позиции сравнивает буквы загаданного слова и попытки.
     * Если буквы совпадают, помечает позицию как зеленую.
     * Иначе увеличивает счетчик вхождений буквы из загаданного слова.
     *
     * @param word_try      слово-попытка для проверки
     * @param letter_colors массив цветов для заполнения
     * @param letter_count  карта подсчета количества каждой буквы в
     *                      загаданном слове
     * @return обновленную карту подсчета букв
     */

    private Map<Character, Integer> CheckGreen(String word_try,
                                               Color[] letter_colors,
                                               Map<Character, Integer>
                                                       letter_count) {
        for (int i = 0; i < right_word.length(); i++) {
            if (right_word.charAt(i) == word_try.charAt(i)) {
                letter_colors[i] = Color.Green;

            } else {

                letter_count.put(right_word.charAt(i),
                        letter_count.getOrDefault(right_word.charAt(i), 0) + 1);

            }
        }
        return letter_count;
    }

    /**
     * Проверяет буквы, которые присутствуют в слове, но на других позициях
     * (желтые).
     * Пропускает уже отмеченные зеленые позиции.
     * Для каждой неправильной позиции проверяет, есть ли такая буква в
     * загаданном слове
     * и доступна ли она для использования (не использована ранее).
     *
     * @param word_try      слово-попытка для проверки
     * @param letter_colors массив цветов для заполнения
     * @param letter_count  карта с оставшимся количеством каждой буквы
     */
    private void CheckYellow(String word_try,
                             Color[] letter_colors,
                             Map<Character, Integer>
                                     letter_count) {

        for (int i = 0; i < right_word.length(); i++) {
            if (letter_colors[i] == Color.Green) {
                continue;
            }

            for (int j = 0; j < right_word.length(); j++) {
                if (right_word.charAt(j) == word_try.charAt(i)
                        && letter_colors[j] != Color.Green) {
                    if (letter_count.get(right_word.charAt(j)) > 0) {
                        letter_colors[i] = Color.Yellow;
                        letter_count.put(right_word.charAt(j),
                                letter_count.get(right_word.charAt(j)) - 1);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Определяет цвета для всех букв в слове-попытке.
     * Последовательность определения цветов:
     * Изначально все буквы помечаются серым
     * Потом определяются зеленые буквы (точные совпадения)
     * Затем определяются желтые буквы (есть в слове, но не на своих местах)
     *
     * @param word_try      слово-попытка для проверки
     * @param letter_colors массив для заполнения цветами букв
     */
    private void GetLetterColor(String word_try,
                                Color[] letter_colors) {
        for (int i = 0; i < right_word.length(); i++) {
            letter_colors[i] = Color.Grey;
        }
        Map<Character, Integer> letter_count = new HashMap<>();
        letter_count = CheckGreen(word_try, letter_colors, letter_count);
        CheckYellow(word_try, letter_colors, letter_count);
    }

}