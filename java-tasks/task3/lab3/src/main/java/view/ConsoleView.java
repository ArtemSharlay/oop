package view;

import model.GameModel;
import java.util.Scanner;

/**
 * Консольное меню для просмотра рекордов.
 * Позволяет просматривать и очищать таблицу лучших результатов.
 *
 */
public class ConsoleView {

    /** Модель игры для доступа к менеджеру очков */
    private GameModel model;

    /**
     * Конструктор.
     *
     * @param model модель игры
     */
    public ConsoleView(GameModel model) {
        this.model = model;
    }

    /**
     * Отображает консольное меню и обрабатывает ввод пользователя.
     * Доступные опции:
     * 1 - показать все рекорды
     * 2 - очистить рекорды
     * 3 - выйти из меню
     */
    public void showMenu() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("==== CONSOLE VIEW ====");
            System.out.println("1. Show scores");
            System.out.println("2. Clear scores");
            System.out.println("3. Remove console");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1 -> {
                    System.out.println("Scores:");
                    for (Integer score : model.getScoreManager().getScores()) {
                        System.out.println(score);
                    }
                }
                case 2 -> {
                    model.getScoreManager().clearScores();
                    System.out.println("Scores cleared");
                }
                case 3 -> {
                    return;
                }
            }
        }
    }
}