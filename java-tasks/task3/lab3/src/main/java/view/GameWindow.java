package view;

import controller.GameController;
import model.GameModel;
import javax.swing.*;
import java.awt.*;

/**
 * Главное окно игры.
 * Содержит игровую панель и настраивает параметры окна.
 *
 */
public class GameWindow extends JFrame {

    /** Игровая панель для отображения игрового процесса */
    public GamePanel panel;

    /**
     * Конструктор окна.
     * Создает и настраивает главное окно игры.
     *
     * @param model модель игры
     */
    public GameWindow(GameModel model) {
        setTitle("BOMBA");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel = new GamePanel(model);
        add(panel);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Возвращает игровую панель.
     *
     * @return панель игры
     */
    public GamePanel getPanel() {
        return panel;
    }
}