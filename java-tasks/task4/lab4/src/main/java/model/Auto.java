package model;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Класс, представляющий готовый автомобиль.
 * Состоит из кузова, двигателя и аксессуара.
 * Наследует уникальный идентификатор от Part.
 */
public class Auto extends Part {

    private final Body body;
    private final Motor motor;
    private final Accessory accessory;

    /**
     * Конструктор автомобиля.
     * Создаёт новый автомобиль из переданных комплектующих.
     *
     * @param body      кузов автомобиля
     * @param motor     двигатель автомобиля
     * @param accessory аксессуар автомобиля
     */
    public Auto(Body body, Motor motor, Accessory accessory) {
        super();
        this.body = body;
        this.motor = motor;
        this.accessory = accessory;
    }

    /**
     * Возвращает уникальный идентификатор автомобиля.
     *
     * @return идентификатор автомобиля
     */
    public long getId() {
        return id;
    }

    /**
     * Возвращает кузов автомобиля.
     *
     * @return кузов автомобиля
     */
    public Body getBody() {
        return body;
    }

    /**
     * Возвращает двигатель автомобиля.
     *
     * @return двигатель автомобиля
     */
    public Motor getMotor() {
        return motor;
    }

    /**
     * Возвращает аксессуар автомобиля.
     *
     * @return аксессуар автомобиля
     */
    public Accessory getAccessory() {
        return accessory;
    }
}