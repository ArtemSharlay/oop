package model;

import java.awt.*;

/**
 * Абстрактный базовый класс для всех игровых объектов.
 * Содержит общие свойства: координаты и флаг уничтожения.
 *
 */
public abstract class GameObject {

    /** Координата X (столбец) на игровой карте */
    protected int x;

    /** Координата Y (строка) на игровой карте */
    protected int y;

    /** Флаг уничтожения объекта */
    protected boolean dead = false;

    /**
     * Конструктор игрового объекта.
     *
     * @param x координата X
     * @param y координата Y
     */
    public GameObject(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Возвращает координату X.
     *
     * @return координата X
     */
    public int getX() {
        return x;
    }

    /**
     * Возвращает координату Y.
     *
     * @return координата Y
     */
    public int getY() {
        return y;
    }

    /**
     * Проверяет, уничтожен ли объект.
     *
     * @return true если объект уничтожен
     */
    public boolean isDead() {
        return dead;
    }
}