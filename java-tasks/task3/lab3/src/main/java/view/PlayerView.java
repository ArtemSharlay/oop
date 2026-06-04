package view;

import model.Player;
import java.awt.*;

/**
 * Отвечает за отрисовку игрока.
 *
 */
public class PlayerView implements Drawable {

    /** Размер одной клетки в пикселях */
    private int TILE;

    /** Игрок для отрисовки */
    private Player player;

    /**
     * Конструктор.
     *
     * @param player игрок
     * @param TILE размер клетки
     */
    public PlayerView(Player player, int TILE) {
        this.player = player;
        this.TILE = TILE;
    }

    /**
     * Отрисовывает игрока синим кругом (овалом).
     *
     * @param g графический контекст
     */
    @Override
    public void draw(Graphics g) {
        g.setColor(Color.BLUE);
        g.fillOval(
                player.getX() * TILE,
                player.getY() * TILE,
                TILE,
                TILE
        );
    }
}