package view;

import model.Wall;
import java.awt.*;

/**
 * Отвечает за отрисовку неразрушаемой стены.
 *
 */
public class WallView implements Drawable {

    /** Размер одной клетки в пикселях */
    private int TILE;

    /** Стена для отрисовки */
    private Wall wall;

    /**
     * Конструктор.
     *
     * @param wall стена
     * @param TILE размер клетки
     */
    public WallView(Wall wall, int TILE) {
        this.wall = wall;
        this.TILE = TILE;
    }

    /**
     * Отрисовывает стену серым квадратом с черным контуром.
     *
     * @param g графический контекст
     */
    @Override
    public void draw(Graphics g) {
        g.setColor(Color.GRAY);
        g.fillRect(
                wall.getX() * TILE,
                wall.getY() * TILE,
                TILE,
                TILE
        );

        g.setColor(Color.BLACK);
        g.drawRect(
                wall.getX() * TILE,
                wall.getY() * TILE,
                TILE,
                TILE
        );
    }
}