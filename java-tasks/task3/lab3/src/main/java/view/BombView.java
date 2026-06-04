package view;

import model.Bomb;
import model.Player;
import java.awt.*;

/**
 * Отвечает за отрисовку бомбы и её взрыва на игровом поле.
 *
 */
public class BombView implements Drawable {

    /** Размер одной клетки в пикселях */
    private int TILE;

    /** Бомба для отрисовки */
    private Bomb bomb;

    /**
     * Конструктор.
     *
     * @param bomb бомба для отрисовки
     * @param TILE размер клетки
     */
    public BombView(Bomb bomb, int TILE) {
        this.bomb = bomb;
        this.TILE = TILE;
    }

    /**
     * Отрисовывает бомбу или эффект взрыва.
     *
     * @param g графический контекст
     */
    @Override
    public void draw(Graphics g) {
        if (bomb.isDead()) {
            drawExplosion(g);
            return;
        }

        g.setColor(Color.BLACK);
        g.fillOval(
                bomb.getX() * TILE,
                bomb.getY() * TILE,
                TILE,
                TILE
        );
    }

    /**
     * Отрисовывает эффект взрыва бомбы.
     * Закрашивает центр и все клетки в радиусе поражения.
     *
     * @param g графический контекст
     */
    public void drawExplosion(Graphics g) {
        int x = bomb.getX();
        int y = bomb.getY();

        g.setColor(Color.ORANGE);

        // Центр взрыва
        g.fillRect(x * TILE, y * TILE, TILE, TILE);

        // Взрыв вверх
        for (int i = 0; i <= bomb.getExplosionRangeUp(); i++) {
            g.fillRect(x * TILE, (y - i) * TILE, TILE, TILE);
        }

        // Взрыв вниз
        for (int i = 0; i <= bomb.getExplosionRangeDown(); i++) {
            g.fillRect(x * TILE, (y + i) * TILE, TILE, TILE);
        }

        // Взрыв вправо
        for (int i = 0; i <= bomb.getExplosionRangeRight(); i++) {
            g.fillRect((x + i) * TILE, y * TILE, TILE, TILE);
        }

        // Взрыв влево
        for (int i = 0; i <= bomb.getExplosionRangeLeft(); i++) {
            g.fillRect((x - i) * TILE, y * TILE, TILE, TILE);
        }
    }
}