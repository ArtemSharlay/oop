package model;

import java.util.*;

/**
 * Основная модель игры. Содержит всю игровую логику, карту, объекты и состояние игры.
 *
 */
public class GameModel {

    /** Ширина карты в клетках */
    public static final int WIDTH = 15;

    /** Высота карты в клетках */
    public static final int HEIGHT = 10;

    /** Максимальный размер буфера действий */
    public static final int BUFFER_SIZE = 10;

    /** Список всех игровых объектов */
    private static List<GameObject> objects;

    /** Буфер действий игрока */
    private static Deque<Action> actionBuffer;

    /** Список объектов для добавления в следующем тике */
    private List<GameObject> objectsToAdd;

    /** Карта игрового поля */
    private TileType[][] map;

    /** Игрок */
    private static Player player;

    /** Флаг окончания игры */
    private boolean gameOver = false;

    /** Флаг победы */
    private boolean win = false;

    /** Текущий счет */
    private int score = 0;

    /** Менеджер очков для сохранения рекордов */
    private static ScoreManager scoreManager = new ScoreManager();

    /**
     * Конструктор. Инициализирует игру.
     */
    public GameModel() {
        init();
    }

    /**
     * Инициализирует игру: создает карту, стены, игрока и врагов.
     */
    private void init() {
        objects = new ArrayList<>();
        actionBuffer = new ArrayDeque<>();
        objectsToAdd = new ArrayList<>();
        map = new TileType[HEIGHT][WIDTH];

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                map[y][x] = TileType.EMPTY;
                // Границы карты - неразрушаемые стены
                if (x == 0 || y == 0 || x == WIDTH - 1 || y == HEIGHT - 1) {
                    map[y][x] = TileType.WALL;
                    objects.add(new Wall(x, y));
                }
                // Шахматная расстановка неразрушаемых стен
                else if (x % 2 == 1 && y % 2 == 1 && x != 1 && x != WIDTH - 2) {
                    map[y][x] = TileType.WALL;
                    objects.add(new Wall(x, y));
                }
                // Разрушаемые стены на четных позициях
                else if (x % 2 == 0 && y % 2 == 0) {
                    map[y][x] = TileType.BREAKABLE;
                    objects.add(new BreakableWall(x, y));
                }
            }
        }

        player = new Player(2, 1);
        objects.add(player);
        objects.add(new Enemy(1, HEIGHT - 2));
    }

    /**
     * Перезапускает игру. Сохраняет текущий счет и сбрасывает состояние.
     */
    public void restart() {
        gameOver = false;
        win = false;
        scoreManager.addScore(score);
        score = 0;
        init();
    }

    /**
     * Обновляет состояние игры каждый тик.
     * Вызывает update() у всех обновляемых объектов, проверяет условия победы/поражения.
     */
    public void update() {
        if (gameOver || win) {
            System.out.println("IGRA ZAKONCHENA");
            scoreManager.addScore(score);
            return;
        }

        objects.removeIf(GameObject::isDead);

        boolean isGameOver = true;
        Iterator<GameObject> iterator = objects.iterator();
        while (iterator.hasNext()) {
            GameObject object = iterator.next();
            if (object instanceof Updatable) {
                if (object instanceof Enemy) {
                    isGameOver = false;
                }
                ((Updatable) object).update(this);
            }
        }

        if (isGameOver) {
            win = true;
            setGameOver(true);
            scoreManager.addScore(score);
            return;
        }

        objects.addAll(objectsToAdd);
        objectsToAdd.clear();
        actionBuffer.clear();
    }

    /**
     * Добавляет действие в буфер.
     *
     * @param action действие игрока
     */
    public void bufferAction(Action action) {
        if (actionBuffer.size() >= BUFFER_SIZE) {
            actionBuffer.pollFirst();
        }
        actionBuffer.addLast(action);
    }

    /**
     * Добавляет объект в список для добавления в следующем тике.
     *
     * @param object игровой объект
     */
    public void addToObjects(GameObject object) {
        objectsToAdd.add(object);
    }

    /**
     * Возвращает игрока.
     *
     * @return объект игрока
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Возвращает карту игрового поля.
     *
     * @return двумерный массив типов тайлов
     */
    public TileType[][] getMap() {
        return map;
    }

    /**
     * Возвращает буфер действий.
     *
     * @return очередь действий
     */
    public Deque<Action> getActionBuffer() {
        return actionBuffer;
    }

    /**
     * Возвращает список всех игровых объектов.
     *
     * @return список объектов
     */
    public List<GameObject> getObjects() {
        return objects;
    }

    /**
     * Устанавливает флаг окончания игры.
     *
     * @param result true - игра окончена
     */
    public void setGameOver(boolean result) {
        gameOver = result;
    }

    /**
     * Проверяет, окончена ли игра.
     *
     * @return true если игра окончена
     */
    public boolean isGameOver() {
        return gameOver;
    }

    /**
     * Проверяет, одержана ли победа.
     *
     * @return true если победа
     */
    public boolean isWin() {
        return win;
    }

    /**
     * Возвращает текущий счет.
     *
     * @return количество очков
     */
    public int getScore() {
        return score;
    }

    /**
     * Добавляет очки к текущему счету.
     *
     * @param number количество добавляемых очков
     */
    public void addToScore(int number) {
        score += number;
    }

    /**
     * Возвращает менеджер очков.
     *
     * @return менеджер очков
     */
    public ScoreManager getScoreManager() {
        return scoreManager;
    }
}