package view;

import model.BreakableWall;
import model.Wall;
import java.awt.*;

/**
 * Отвечает за отрисовку разрушаемой стены.
 *
 */
public class BreakableWallView implements Drawable {

    /** Размер одной клетки в пикселях */
    private int TILE;

    /** Разрушаемая стена для отрисовки */
    private BreakableWall bWall;

    /**
     * Конструктор.
     *
     * @param bWall разрушаемая стена
     * @param TILE размер клетки
     */
    public BreakableWallView(BreakableWall bWall, int TILE) {
        this.bWall = bWall;
        this.TILE = TILE;
    }

    /**
     * Отрисовывает разрушаемую стену.
     * Заливка желтым цветом с черным контуром.
     *
     * @param g графический контекст
     */
    @Override
    public void draw(Graphics g) {
        g.setColor(Color.YELLOW);
        g.fillRect(
                bWall.getX() * TILE,
                bWall.getY() * TILE,
                TILE,
                TILE
        );

        g.setColor(Color.BLACK);
        g.drawRect(
                bWall.getX() * TILE,
                bWall.getY() * TILE,
                TILE,
                TILE
        );
    }
}