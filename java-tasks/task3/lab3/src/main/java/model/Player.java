package model;

import java.util.Deque;
import java.util.Iterator;
import java.util.List;

/**
 * Класс игрока. Управляется пользователем через клавиатуру.
 * Может двигаться и устанавливать бомбы.
 *
 */
public class Player extends GameObject implements Updatable {

    /** Текущий радиус взрыва бомб игрока */
    private int explosionRange = 0;

    /** Максимальный радиус взрыва */
    private static final int MAX_EXPLOSION_SIZE = 3;

    /** Флаг установки бомбы */
    private boolean bombFlag = false;

    /**
     * Конструктор игрока.
     *
     * @param x начальная координата X
     * @param y начальная координата Y
     */
    public Player(int x, int y) {
        super(x, y);
    }

    /**
     * Создает бомбу на указанной позиции.
     *
     * @param model модель игры
     * @param map карта игрового поля
     * @param bombX координата X установки бомбы
     * @param bombY координата Y установки бомбы
     */
    public void spawnBomb(GameModel model, TileType[][] map, int bombX, int bombY) {
        if (bombFlag) {
            bombFlag = false;
            if (explosionRange < MAX_EXPLOSION_SIZE) {
                explosionRange++;
            }
            model.addToObjects(new Bomb(bombX, bombY, explosionRange));
            map[bombY][bombX] = TileType.BOMB;
        }
    }

    /**
     * Обновляет состояние игрока. Обрабатывает действия из буфера.
     *
     * @param model модель игры
     */
    @Override
    public void update(GameModel model) {
        TileType[][] map = model.getMap();
        Deque<Action> actionBuffer = model.getActionBuffer();
        if (actionBuffer == null) {
            return;
        }

        Iterator<Action> it = actionBuffer.descendingIterator();
        Action displace = null;

        while (it.hasNext()) {
            Action action = it.next();
            if (action == Action.BOMB) {
                bombFlag = true;
                continue;
            }
            if (displace == null) {
                displace = action;
            }
        }

        if (displace == null) {
            return;
        }

        int bombX = x;
        int bombY = y;

        if (tryToMove(map, displace)) {
            if (map[y][x] == TileType.ENEMY) {
                model.setGameOver(true);
            }
            spawnBomb(model, map, bombX, bombY);
        }
    }

    /**
     * Пытается переместить игрока в заданном направлении.
     *
     * @param map карта игрового поля
     * @param action направление движения
     * @return true если перемещение выполнено
     */
    public boolean tryToMove(TileType[][] map, Action action) {
        switch (action) {
            case RIGHT:
                if (map[y][x + 1] == TileType.EMPTY || map[y][x + 1] == TileType.ENEMY) {
                    x++;
                    return true;
                }
                return false;
            case LEFT:
                if (map[y][x - 1] == TileType.EMPTY || map[y][x - 1] == TileType.ENEMY) {
                    x--;
                    return true;
                }
                return false;
            case UP:
                if (map[y - 1][x] == TileType.EMPTY || map[y - 1][x] == TileType.ENEMY) {
                    y--;
                    return true;
                }
                return false;
            case DOWN:
                if (map[y + 1][x] == TileType.EMPTY || map[y + 1][x] == TileType.ENEMY) {
                    y++;
                    return true;
                }
                return false;
            default:
                return false;
        }
    }
}