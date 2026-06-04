package controller;

import model.Action;
import model.GameModel;
import view.GamePanel;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Контроллер игры, управляющий взаимодействием между моделью и представлением.
 * Обрабатывает ввод с клавиатуры и управляет игровым таймером.
 *
 */
public class GameController {

    /** Таймер для периодического обновления состояния игры */
    private Timer timer;

    /** Модель игры, содержащая игровую логику и состояние */
    private GameModel model;

    /** Панель отображения игры для визуализации состояния */
    private GamePanel panel;

    /**
     * Конструктор контроллера игры.
     * Инициализирует обработку клавиатуры и таймер обновления.
     *
     * @param model Модель игры, содержащая игровую логику
     * @param panel Панель отображения игры
     */
    public GameController(GameModel model, GamePanel panel) {
        this.model = model;
        this.panel = panel;
        initKeyboard();
        initTimer();
    }

    /**
     * Инициализирует обработку событий клавиатуры.
     * Настраивает панель для получения фокуса и добавляет слушателя клавиш.
     * Поддерживаемые клавиши:
     * <ul>
     *   <li>Стрелка влево - движение влево</li>
     *   <li>Стрелка вправо - движение вправо</li>
     *   <li>Стрелка вверх - движение вверх</li>
     *   <li>Стрелка вниз - движение вниз</li>
     *   <li>Пробел - установка бомбы</li>
     *   <li>Клавиша R - перезапуск игры</li>
     * </ul>
     */
    private void initKeyboard() {
        panel.setFocusable(true);
        panel.addKeyListener(new KeyAdapter() {
            /**
             * Обрабатывает нажатие клавиши.
             * Преобразует нажатую клавишу в соответствующее действие модели.
             *
             * @param e Событие нажатия клавиши
             */
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT -> model.bufferAction(Action.LEFT);
                    case KeyEvent.VK_RIGHT -> model.bufferAction(Action.RIGHT);
                    case KeyEvent.VK_UP -> model.bufferAction(Action.UP);
                    case KeyEvent.VK_DOWN -> model.bufferAction(Action.DOWN);
                    case KeyEvent.VK_SPACE -> model.bufferAction(Action.BOMB);
                    case KeyEvent.VK_R -> restartGame();
                }
            }
        });
    }

    /**
     * Инициализирует игровой таймер.
     * Таймер срабатывает каждые 200 миллисекунд, обновляет модель,
     * перерисовывает панель и проверяет состояние игры.
     * При завершении игры таймер автоматически останавливается.
     */
    private void initTimer() {
        timer = new Timer(200, e -> {
            model.update();
            panel.repaint();
            if (model.isGameOver()){
                ((Timer)e.getSource()).stop();
                System.out.println("timer stop");
            }
        });
        timer.start();
    }

    /**
     * Перезапускает игру.
     * Останавливает текущий таймер, сбрасывает состояние модели до начального,
     * перерисовывает панель и запускает таймер заново.
     * В консоль выводится сообщение о перезапуске игры.
     */
    private void restartGame() {
        System.out.println("Restarting game...");
        timer.stop();
        model.restart();
        panel.repaint();
        timer.start();
    }
}