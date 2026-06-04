package view;

import model.*;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Главная панель отображения игры.
 * Отвечает за рендеринг всех игровых объектов и отображение информации об игре.
 *
 */
public class GamePanel extends JPanel {

    /** Размер одной клетки в пикселях */
    private static int TILE = 40;

    /** Модель игры */
    private static GameModel model;

    /** Карта соответствия классов объектов их рендерерам */
    private Map<Class<? extends GameObject>, Function<GameObject, Drawable>> rendererMap = new HashMap<>();

    /**
     * Конструктор панели.
     *
     * @param model модель игры
     */
    public GamePanel(GameModel model) {
        this.model = model;
        registerRenderers();
    }

    /**
     * Регистрирует рендереры для всех типов игровых объектов.
     */
    private void registerRenderers() {
        rendererMap.put(Player.class, obj -> new PlayerView((Player) obj, TILE));
        rendererMap.put(Enemy.class, obj -> new EnemyView((Enemy) obj, TILE));
        rendererMap.put(Bomb.class, obj -> new BombView((Bomb) obj, TILE));
        rendererMap.put(Wall.class, obj -> new WallView((Wall) obj, TILE));
        rendererMap.put(BreakableWall.class, obj -> new BreakableWallView((BreakableWall) obj, TILE));
    }

    /**
     * Возвращает функцию-рендерер для данного объекта.
     *
     * @param object игровой объект
     * @return функция, создающая Drawable для объекта
     */
    private Function<GameObject, Drawable> getRenderer(GameObject object) {
        return rendererMap.get(object.getClass());
    }

    /**
     * Отрисовывает все компоненты панели.
     * Если игра окончена, отображает информацию о результате и рекордах.
     *
     * @param g графический контекст
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (model.isGameOver()) {
            drawInfo(g);
        }

        for (GameObject object : model.getObjects()) {
            Function<GameObject, Drawable> renderer = getRenderer(object);
            if (renderer != null) {
                Drawable drawable = renderer.apply(object);
                drawable.draw(g);
            }
        }
    }

    /**
     * Отображает информацию об окончании игры.
     * Показывает сообщение о победе/поражении и таблицу рекордов.
     *
     * @param g графический контекст
     */
    private void drawInfo(Graphics g) {
        g.setColor(Color.BLACK);

        if (model.isWin()) {
            g.drawString("YOU WIN", 620, 40);
        } else {
            g.drawString("GAME OVER", 620, 40);
        }

        int i = 1;
        for (Integer scoreIt : model.getScoreManager().getScores()) {
            g.drawString(i + " Score: " + scoreIt, 620, 60 + i * 20);
            i++;
        }
    }
}