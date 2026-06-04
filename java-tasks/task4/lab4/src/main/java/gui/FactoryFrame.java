package gui;

import factory.Factory;

import javax.swing.*;

/**
 * Главное окно приложения для отображения информации о фабрике.
 */
public class FactoryFrame extends JFrame {

    /**
     * Конструктор окна фабрики.
     * Создаёт и настраивает главное окно с панелью отображения состояния фабрики.
     *
     * @param factory объект фабрики, состояние которого будет отображаться
     */
    public FactoryFrame(Factory factory) {
        setTitle("Factory");
        setSize(800, 600);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        add(new FactoryPanel(factory));
        setLocationRelativeTo(null);
        setVisible(true);
    }
}