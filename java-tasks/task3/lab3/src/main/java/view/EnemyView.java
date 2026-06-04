package view;

import model.Enemy;
import java.awt.*;

/**
 * Отвечает за отрисовку врага.
 *
 */
public class EnemyView implements Drawable {

    /** Размер одной клетки в пикселях */
    private int TILE;

    /** Враг для отрисовки */
    private Enemy enemy;

    /**
     * Конструктор.
     *
     * @param enemy враг
     * @param TILE размер клетки
     */
    public EnemyView(Enemy enemy, int TILE) {
        this.enemy = enemy;
        this.TILE = TILE;
    }

    /**
     * Отрисовывает врага красным квадратом.
     *
     * @param g графический контекст
     */
    @Override
    public void draw(Graphics g) {
        g.setColor(Color.RED);
        g.fillRect(
                enemy.getX() * TILE,
                enemy.getY() * TILE,
                TILE,
                TILE
        );
    }
}