package view;

import java.awt.Graphics;

/**
 * Интерфейс для всех объектов, которые можно отрисовать на игровом поле.
 *
 */
public interface Drawable {

    /**
     * Отрисовывает объект на графическом контексте.
     *
     * @param g графический контекст (объект Graphics для рисования)
     */
    void draw(Graphics g);
}