package model;

/**
 * Класс неразрушаемой стены.
 * Препятствие, которое нельзя уничтожить взрывом бомбы.
 *
 */
public class Wall extends GameObject {

    /**
     * Конструктор стены.
     *
     * @param x координата X
     * @param y координата Y
     */
    public Wall(int x, int y) {
        super(x, y);
    }
}