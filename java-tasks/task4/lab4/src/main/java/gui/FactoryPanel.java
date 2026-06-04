package gui;

import dealer.Dealer;
import factory.Factory;
import supplier.AccessorySupplier;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Панель отображения состояния фабрики.
 * Содержит информационные поля о заполнении складов и ползунки для настройки задержек поставщиков и дилеров.
 */
public class FactoryPanel extends JPanel {
    private final Factory factory;

    private final JLabel bodyStorageLabel = new JLabel();
    private final JLabel motorStorageLabel = new JLabel();
    private final JLabel accessoryStorageLabel = new JLabel();
    private final JLabel autoStorageLabel = new JLabel();
    private final JLabel producedAutosLabel = new JLabel();
    private final JLabel taskQueueLabel = new JLabel();

    private final JSlider bodySupplierSlider = new JSlider(100, 5000, 1000);
    private final JLabel bodySupplierLabel = new JLabel("Значение: " + bodySupplierSlider.getValue());

    private final JSlider motorSupplierSlider = new JSlider(100, 5000, 1000);
    private final JLabel motorSupplierLabel = new JLabel("Значение: " + motorSupplierSlider.getValue());

    private final JSlider accessorySupplierSlider = new JSlider(100, 5000, 1000);
    private final JLabel accessorySupplierLabel = new JLabel("Значение: " + accessorySupplierSlider.getValue());

    private final JSlider dealerSlider = new JSlider(100, 5000, 1000);
    private final JLabel dealerLabel = new JLabel("Значение: " + dealerSlider.getValue());

    /**
     * Конструктор панели фабрики.
     * Создаёт информационную панель с данными о складах.
     * Создаёт панель с ползунками для настройки задержек.
     * Запускает таймер для периодического обновления данных.
     *
     * @param factory объект фабрики, состояние которого отображается
     */
    public FactoryPanel(Factory factory) {
        this.factory = factory;
        setLayout(new BorderLayout());

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout(6, 1));

        infoPanel.add(bodyStorageLabel);
        infoPanel.add(motorStorageLabel);
        infoPanel.add(accessoryStorageLabel);
        infoPanel.add(autoStorageLabel);
        infoPanel.add(producedAutosLabel);
        infoPanel.add(taskQueueLabel);

        JPanel sliderPanel = new JPanel();
        sliderPanel.setLayout(new GridLayout(4, 3));

        // Ползунок для поставщика кузовов
        sliderPanel.add(new JLabel("Body Supplier"));
        sliderPanel.add(bodySupplierSlider);
        bodySupplierSlider.addChangeListener(e -> {
            int value = bodySupplierSlider.getValue();
            bodySupplierLabel.setText("Значение: " + value);
            factory.getBodySupplier().setSupplyDelayMs(value);
        });
        sliderPanel.add(bodySupplierLabel);

        // Ползунок для поставщика двигателей
        sliderPanel.add(new JLabel("Motor Supplier"));
        sliderPanel.add(motorSupplierSlider);
        motorSupplierSlider.addChangeListener(e -> {
            int value = motorSupplierSlider.getValue();
            motorSupplierLabel.setText("Значение: " + value);
            factory.getMotorSupplier().setSupplyDelayMs(value);
        });
        sliderPanel.add(motorSupplierLabel);

        // Ползунок для поставщиков аксессуаров
        sliderPanel.add(new JLabel("Accessory Supplier"));
        sliderPanel.add(accessorySupplierSlider);
        accessorySupplierSlider.addChangeListener(e -> {
            int value = accessorySupplierSlider.getValue();
            accessorySupplierLabel.setText("Значение: " + value);
            for (AccessorySupplier accessorySupplier : factory.getAccessorySuppliers()) {
                accessorySupplier.setSupplyDelayMs(value);
            }
        });
        sliderPanel.add(accessorySupplierLabel);

        // Ползунок для задержки дилеров
        sliderPanel.add(new JLabel("Dealer Delay"));
        sliderPanel.add(dealerSlider);
        dealerSlider.addChangeListener(e -> {
            int value = dealerSlider.getValue();
            dealerLabel.setText("Значение: " + value);
            for (Dealer dealer : factory.getDealers()) {
                dealer.setRequestDelayMs(value);
            }
        });
        sliderPanel.add(dealerLabel);

        add(infoPanel, BorderLayout.CENTER);
        add(sliderPanel, BorderLayout.SOUTH);

        Timer timer = new Timer(100, e -> refresh());
        timer.start();
    }

    /**
     * Обновляет все данные на панели.
     * Запрашивает текущее состояние складов и поставщиков.
     */
    private void refresh() {
        updateBodyStorage(factory.getBodyStorage().getSize(),
                factory.getBodyStorage().getCapacity(),
                factory.getBodySupplier().getCount());

        updateMotorStorage(factory.getMotorStorage().getSize(),
                factory.getMotorStorage().getCapacity(),
                factory.getMotorSupplier().getCount());

        updateAccessoryStorage(factory.getAccessoryStorage().getSize(),
                factory.getAccessoryStorage().getCapacity(),
                factory.getAccessorySuppliers().getFirst().getCount());

        updateAutoStorage(factory.getAutoStorage().getSize(),
                factory.getAutoStorage().getCapacity());

        updateProducedAutos(factory.getProducedAutos());
        updateTaskQueue(factory.getThreadPool().getQueueSize());
    }

    /**
     * Обновляет информацию о складе кузовов.
     *
     * @param current  текущее количество кузовов на складе
     * @param capacity максимальная вместимость склада
     * @param count    общее количество поставленных кузовов
     */
    public void updateBodyStorage(int current, int capacity, AtomicLong count) {
        bodyStorageLabel.setText("Bodies: " + current + " / " + capacity + " " +
                "totally supplied: " + count);
    }

    /**
     * Обновляет информацию о складе двигателей.
     *
     * @param current  текущее количество двигателей на складе
     * @param capacity максимальная вместимость склада
     * @param count    общее количество поставленных двигателей
     */
    public void updateMotorStorage(int current, int capacity, AtomicLong count) {
        motorStorageLabel.setText("Motors: " + current + " / " + capacity + " " +
                "totally supplied: " + count);
    }

    /**
     * Обновляет информацию о складе аксессуаров.
     *
     * @param current  текущее количество аксессуаров на складе
     * @param capacity максимальная вместимость склада
     * @param count    общее количество поставленных аксессуаров
     */
    public void updateAccessoryStorage(int current, int capacity, AtomicLong count) {
        accessoryStorageLabel.setText("Accessories: " + current + " / " + capacity + " " +
                "totally supplied: " + count);
    }

    /**
     * Обновляет информацию о складе готовых автомобилей.
     *
     * @param current  текущее количество автомобилей на складе
     * @param capacity максимальная вместимость склада
     */
    public void updateAutoStorage(int current, int capacity) {
        autoStorageLabel.setText("Autos: " + current + " / " + capacity);
    }

    /**
     * Обновляет количество произведённых автомобилей.
     *
     * @param produced счётчик произведённых автомобилей
     */
    public void updateProducedAutos(AtomicLong produced) {
        producedAutosLabel.setText("Produced Autos: " + produced);
    }

    /**
     * Обновляет размер очереди задач в пуле потоков.
     *
     * @param size текущий размер очереди задач
     */
    public void updateTaskQueue(int size) {
        taskQueueLabel.setText("Task Queue: " + size);
    }
}