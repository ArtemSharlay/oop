package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Менеджер для управления рекордами (очками).
 * Хранит топ-10 лучших результатов.
 *
 */
public class ScoreManager {

    /** Максимальное количество сохраняемых рекордов */
    private final int MAX_SIZE = 10;

    /** Список рекордов (отсортирован по убыванию) */
    private List<Integer> scores = new ArrayList<>();

    /**
     * Добавляет новый результат в список рекордов.
     * Список автоматически сортируется и обрезается до 10 элементов.
     *
     * @param score новое количество очков
     */
    public void addScore(int score) {
        scores.add(score);
        scores.sort(Collections.reverseOrder());

        if (scores.size() > MAX_SIZE) {
            scores = scores.subList(0, MAX_SIZE);
        }
    }

    /**
     * Возвращает список лучших результатов.
     *
     * @return список рекордов (от большего к меньшему)
     */
    public List<Integer> getScores() {
        return scores;
    }

    /**
     * Очищает список рекордов.
     */
    public void clearScores() {
        scores.clear();
    }
}