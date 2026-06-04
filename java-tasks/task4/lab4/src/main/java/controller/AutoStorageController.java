package controller;

import config.Config;
import model.Accessory;
import model.Auto;
import model.Body;
import model.Motor;
import storage.Storage;
import task.BuildAutoTask;
import threadpool.ThreadPool;

/**
 * Контроллер управления сборкой автомобилей.
 * Запускается в отдельном потоке и следит за заполнением склада автомобилей.
 * При уменьшении количества автомобилей добавляет новые задачи сборки в пул потоков.
 */
public class AutoStorageController extends Thread {

    private final Storage<Auto> autoStorage;
    private final Storage<Body> bodyStorage;
    private final Storage<Motor> motorStorage;
    private final Storage<Accessory> accessoryStorage;
    private final ThreadPool threadPool;
    private volatile boolean running = true;

    /**
     * Конструктор контроллера склада автомобилей.
     * При создании сразу заполняет склад автомобилей начальными задачами сборки.
     *
     * @param autoStorage     склад для готовых автомобилей
     * @param bodyStorage     склад кузовов
     * @param motorStorage    склад двигателей
     * @param accessoryStorage склад аксессуаров
     * @param threadPool      пул потоков для выполнения задач сборки
     */
    public AutoStorageController(Storage<Auto> autoStorage,
                                 Storage<Body> bodyStorage,
                                 Storage<Motor> motorStorage,
                                 Storage<Accessory> accessoryStorage,
                                 ThreadPool threadPool) {
        this.autoStorage = autoStorage;
        this.bodyStorage = bodyStorage;
        this.motorStorage = motorStorage;
        this.accessoryStorage = accessoryStorage;
        this.threadPool = threadPool;

        // Заполняем очередь задачами на сборку автомобилей
        for (int i = 0; i < autoStorage.getCapacity(); i++) {
            threadPool.addTask(
                    new BuildAutoTask(
                            bodyStorage,
                            motorStorage,
                            accessoryStorage,
                            autoStorage
                    )
            );
        }
    }

    /**
     * Основной метод потока контроллера.
     * Ожидает сигнала от склада автомобилей, затем добавляет новые задачи сборки.
     * Количество добавляемых задач не превышает количество свободных мест на складе
     * и количество доступных воркеров из конфигурации.
     */
    @Override
    public void run() {
        try {
            while (running) {
                synchronized (autoStorage) {
                    autoStorage.wait();  // Ожидаем сигнала об освобождении места

                    int missingCars = autoStorage.getCapacity() - autoStorage.getSize();
                    int target = Math.min(missingCars, Config.workers);

                    for (int i = 0; i < target; i++) {
                        threadPool.addTask(new BuildAutoTask(bodyStorage,
                                motorStorage, accessoryStorage, autoStorage));
                    }
                }
            }
        } catch (InterruptedException e) {
            interrupt();
        }
    }

    /**
     * Останавливает работу контроллера.
     * Устанавливает флаг остановки и прерывает поток контроллера.
     */
    public void shutdown() {
        running = false;
        interrupt();
    }
}