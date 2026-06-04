package model;

import java.util.Iterator;
import java.util.List;

/**
 * Класс, представляющий бомбу в игре.
 * Бомба является игровым объектом, который может взрываться через определенное время,
 * нанося урон игрокам и разрушая препятствия в радиусе взрыва.
 * Реализует интерфейсы {@link Updatable} (обновляемое состояние) и {@link Damagable} (повреждаемый объект).
 *
 * @see GameObject
 * @see Updatable
 * @see Damagable
 */
public class Bomb extends GameObject implements Updatable, Damagable {

    /** Базовый радиус взрыва бомбы */
    private int explosionRange = 0;

    /** Расстояние взрыва вверх от центра */
    private int explosionRangeUp;

    /** Расстояние взрыва вниз от центра */
    private int explosionRangeDown;

    /** Расстояние взрыва влево от центра */
    private int explosionRangeLeft;

    /** Расстояние взрыва вправо от центра */
    private int explosionRangeRight;

    /** Время создания бомбы в миллисекундах (используется для отсчета времени до взрыва) */
    private long spawnTime;

    /**
     * Конструктор бомбы.
     * Создает новую бомбу в указанных координатах с заданным радиусом поражения.
     *
     * @param x Координата X (столбец) на игровой карте
     * @param y Координата Y (строка) на игровой карте
     * @param explosionRange Базовый радиус взрыва бомбы
     */
    public Bomb(int x, int y, int explosionRange) {
        spawnTime = System.currentTimeMillis();
        super(x, y);
        this.explosionRange = explosionRange;
        explosionRangeUp = explosionRange;
        explosionRangeDown = explosionRange;
        explosionRangeLeft = explosionRange;
        explosionRangeRight = explosionRange;
    }

    /**
     * Обновляет состояние бомбы.
     * Проверяет, прошло ли 3 секунды с момента создания бомбы или бомба уже уничтожена.
     * При наступлении одного из этих условий запускает взрыв, помечает бомбу как уничтоженную
     * и очищает ячейку на карте.
     *
     * @param model Модель игры, содержащая карту и все игровые объекты
     */
    @Override
    public void update(GameModel model) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - spawnTime >= 3000 || isDead()) {
            startExplosion(model);
            setDead();
            model.getMap()[y][x] = TileType.EMPTY;
        }
    }

    /**
     * Запускает процесс взрыва бомбы.
     * Определяет границы взрыва с учетом препятствий и применяет урон ко всем объектам
     * в зоне поражения.
     *
     * @param model Модель игры, содержащая карту и все игровые объекты
     */
    public void startExplosion(GameModel model) {
        TileType[][] map = model.getMap();
        List<GameObject> objects = model.getObjects();
        setExplosionBoundaries(map);
        setTouched(model, objects);
    }

    /**
     * Определяет фактические границы взрыва с учетом препятствий.
     * Анализирует карту в четырех направлениях (вверх, вниз, влево, вправо)
     * и корректирует радиус взрыва при встрече с непроходимыми стенами (WALL)
     * или разрушаемыми блоками (BREAKABLE). Разрушаемые блоки останавливают волну взрыва.
     *
     * @param map Двумерный массив типов тайлов, представляющий игровую карту
     */
    public void setExplosionBoundaries(TileType[][] map) {
        // Определение границы взрыва вправо
        for (int i = 1; i <= explosionRange; i++) {
            int newX = x + i;
            if (newX >= map[0].length) break;

            if (map[y][newX] == TileType.WALL) {
                explosionRangeRight = i - 1;
                break;
            } else if (map[y][newX] == TileType.BREAKABLE) {
                explosionRangeRight = i;
                break;
            }
        }

        // Определение границы взрыва влево
        for (int i = 1; i <= explosionRange; i++) {
            int newX = x - i;
            if (newX < 0) break;

            if (map[y][newX] == TileType.WALL) {
                explosionRangeLeft = i - 1;
                break;
            } else if (map[y][newX] == TileType.BREAKABLE) {
                explosionRangeLeft = i;
                break;
            }
        }

        // Определение границы взрыва вверх
        for (int i = 1; i <= explosionRange; i++) {
            int newY = y - i;
            if (newY < 0) break;

            if (map[newY][x] == TileType.WALL) {
                explosionRangeUp = i - 1;
                break;
            } else if (map[newY][x] == TileType.BREAKABLE) {
                explosionRangeUp = i;
                break;
            }
        }

        // Определение границы взрыва вниз
        for (int i = 1; i <= explosionRange; i++) {
            int newY = y + i;
            if (newY >= map.length) break;

            if (map[newY][x] == TileType.WALL) {
                explosionRangeDown = i - 1;
                break;
            } else if (map[newY][x] == TileType.BREAKABLE) {
                explosionRangeDown = i;
                break;
            }
        }
    }

    /**
     * Применяет урон ко всем объектам, попавшим в зону взрыва.
     * Перебирает все игровые объекты и проверяет, находятся ли они в зоне поражения.
     * При попадании игрока (Player) завершает игру.
     * При попадании других объектов (кроме стен) помечает их как уничтоженные,
     * очищает соответствующие ячейки на карте и начисляет очки (кроме бомб).
     *
     * @param model Модель игры (для установки флага окончания игры и начисления очков)
     * @param objects Список всех игровых объектов в игре
     */
    public void setTouched(GameModel model, List<GameObject> objects) {
        Iterator<GameObject> iterator = objects.iterator();
        while (iterator.hasNext()) {
            GameObject object = iterator.next();
            if (!(object instanceof Wall)) {
                int objX = object.getX();
                int objY = object.getY();
                int rangeXStart = x - explosionRangeLeft;
                int rangeXEnd = x + explosionRangeRight;
                int rangeYStart = y + explosionRangeDown;
                int rangeYEnd = y - explosionRangeUp;

                if ((objX >= rangeXStart && objX <= rangeXEnd && objY == y) ||
                        (objY <= rangeYStart && objY >= rangeYEnd && objX == x)) {
                    if (object instanceof Player) {
                        model.setGameOver(true);
                        continue;
                    }

                    model.getMap()[object.getY()][object.getX()] = TileType.EMPTY;
                    ((Damagable) object).setDead();
                    System.out.println(object);
                    if (!(object instanceof Bomb)) {
                        model.addToScore(100);
                    }
                }
            }
        }
    }

    /**
     * Помечает бомбу как уничтоженную (взорванную).
     * Переопределение метода из интерфейса {@link Damagable}.
     */
    public void setDead() {
        this.dead = true;
    }

    /**
     * Возвращает расстояние взрыва вверх от центра бомбы.
     *
     * @return Расстояние взрыва вверх (количество клеток)
     */
    public int getExplosionRangeUp() {
        return explosionRangeUp;
    }

    /**
     * Возвращает расстояние взрыва вниз от центра бомбы.
     *
     * @return Расстояние взрыва вниз (количество клеток)
     */
    public int getExplosionRangeDown() {
        return explosionRangeDown;
    }

    /**
     * Возвращает расстояние взрыва вправо от центра бомбы.
     *
     * @return Расстояние взрыва вправо (количество клеток)
     */
    public int getExplosionRangeRight() {
        return explosionRangeRight;
    }

    /**
     * Возвращает расстояние взрыва влево от центра бомбы.
     *
     * @return Расстояние взрыва влево (количество клеток)
     */
    public int getExplosionRangeLeft() {
        return explosionRangeLeft;
    }
}