package model;

import java.util.Random;

/**
 * Направления движения врага.
 */
enum Direction {
    UP,    // Вверх
    DOWN,  // Вниз
    LEFT,  // Влево
    RIGHT  // Вправо
}

/**
 * Класс врага. Перемещается по карте и убивает игрока при столкновении.
 *
 */
public class Enemy extends GameObject implements Updatable, Damagable {

    /** Текущее направление движения */
    private Direction direction = Direction.UP;

    /** Все возможные направления */
    private static Direction[] directions = Direction.values();

    /** Счетчик шагов для смены направления */
    private int stepCount = 0;

    /** Генератор случайных чисел */
    private final Random RAND = new Random();

    /**
     * Конструктор врага.
     *
     * @param x координата X
     * @param y координата Y
     */
    public Enemy(int x, int y) {
        super(x, y);
    }

    /**
     * Устанавливает направление движения.
     *
     * @param changeDirection true - принудительно сменить направление по порядку,
     *                        false - сменить случайно через каждые 3 шага
     */
    public void setDirection(boolean changeDirection) {
        if (changeDirection) {
            direction = directions[(direction.ordinal() + 1) % directions.length];
            stepCount = 0;
            return;
        }
        if (stepCount == 3) {
            direction = directions[RAND.nextInt(directions.length)];
            stepCount = 0;
        }
        stepCount++;
    }

    /**
     * Перемещает врага на карте.
     *
     * @param map карта игрового поля
     */
    public void move(TileType[][] map) {
        map[y][x] = TileType.EMPTY;
        switch (direction) {
            case RIGHT -> x++;
            case LEFT -> x--;
            case DOWN -> y++;
            case UP -> y--;
        }
        map[y][x] = TileType.ENEMY;
    }

    /**
     * Обновляет состояние врага каждый тик игры.
     *
     * @param model модель игры
     */
    @Override
    public void update(GameModel model) {
        TileType[][] map = model.getMap();
        setDirection(false);
        if (checkDirection(map)) {
            move(map);
            if (model.getPlayer().getX() == x && model.getPlayer().getY() == y) {
                model.setGameOver(true);
            }
        }
    }

    /**
     * Проверяет возможность движения и корректирует направление.
     *
     * @param map карта игрового поля
     * @return true если есть куда двигаться
     */
    public boolean checkDirection(TileType[][] map) {
        if (checkNeighbours(map)) {
            return true;
        }

        for (int i = 0; i < 4; i++) {
            setDirection(true);
            if (checkNeighbours(map)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Проверяет свободна ли клетка в текущем направлении.
     *
     * @param map карта игрового поля
     * @return true если клетка пуста
     */
    public boolean checkNeighbours(TileType[][] map) {
        switch (direction) {
            case RIGHT -> {
                return map[y][x + 1] == TileType.EMPTY;
            }
            case LEFT -> {
                return map[y][x - 1] == TileType.EMPTY;
            }
            case DOWN -> {
                return map[y + 1][x] == TileType.EMPTY;
            }
            case UP -> {
                return map[y - 1][x] == TileType.EMPTY;
            }
            default -> {
                return false;
            }
        }
    }

    /**
     * Помечает врага как уничтоженного.
     */
    @Override
    public void setDead() {
        this.dead = true;
    }
}