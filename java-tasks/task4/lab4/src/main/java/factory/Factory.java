package factory;

import config.Config;
import controller.AutoStorageController;
import dealer.Dealer;
import model.Accessory;
import model.Auto;
import model.Body;
import model.Motor;
import storage.Storage;
import supplier.AccessorySupplier;
import supplier.BodySupplier;
import supplier.MotorSupplier;
import threadpool.ThreadPool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Главный класс фабрики по производству автомобилей.
 * Управляет всеми складами, поставщиками, дилерами и контроллером сборки.
 * Инициализирует и запускает все компоненты системы.
 */
public class Factory {
    private final Storage<Body> bodyStorage;
    private final Storage<Motor> motorStorage;
    private final Storage<Accessory> accessoryStorage;
    private final Storage<Auto> autoStorage;
    private final ThreadPool threadPool;
    private final BodySupplier bodySupplier;
    private final MotorSupplier motorSupplier;
    private final List<AccessorySupplier> accessorySuppliers;
    private final List<Dealer> dealers;
    private final AutoStorageController controller;

    /**
     * Конструктор фабрики.
     * Создаёт все склады с размерами из конфигурации.
     * Создаёт пул потоков с количеством воркеров из конфигурации.
     * Запускает поставщиков кузовов и двигателей.
     * Создаёт и запускает виртуальные потоки для каждого поставщика аксессуаров.
     * Создаёт и запускает контроллер склада автомобилей.
     * Создаёт и запускает виртуальные потоки для каждого дилера.
     */
    public Factory() {
        bodyStorage = new Storage<>(Config.storageBodySize);
        motorStorage = new Storage<>(Config.storageMotorSize);
        accessoryStorage = new Storage<>(Config.storageAccessorySize);
        autoStorage = new Storage<>(Config.storageAutoSize);

        threadPool = new ThreadPool(Config.workers);
        bodySupplier = new BodySupplier(bodyStorage, 1000);
        motorSupplier = new MotorSupplier(motorStorage, 1000);

        accessorySuppliers = new ArrayList<>();
        for (int i = 0; i < Config.accessorySuppliers; i++) {
            AccessorySupplier accessorySupplier =
                    new AccessorySupplier(
                            accessoryStorage,
                            1000
                    );
            accessorySuppliers.add(accessorySupplier);
            Thread.ofVirtual().start(accessorySupplier);
        }

        controller =
                new AutoStorageController(autoStorage, bodyStorage,
                        motorStorage, accessoryStorage, threadPool);

        dealers = new ArrayList<>();
        for (int i = 0; i < Config.dealers; i++) {
            Dealer dealer = new Dealer(i + 1, autoStorage, 1000);
            dealers.add(dealer);
            Thread.ofVirtual().start(dealer);
        }

        bodySupplier.start();
        motorSupplier.start();
        controller.start();
    }

    /**
     * Возвращает склад кузовов.
     *
     * @return склад кузовов
     */
    public Storage<Body> getBodyStorage() {
        return bodyStorage;
    }

    /**
     * Возвращает склад двигателей.
     *
     * @return склад двигателей
     */
    public Storage<Motor> getMotorStorage() {
        return motorStorage;
    }

    /**
     * Возвращает склад аксессуаров.
     *
     * @return склад аксессуаров
     */
    public Storage<Accessory> getAccessoryStorage() {
        return accessoryStorage;
    }

    /**
     * Возвращает склад готовых автомобилей.
     *
     * @return склад автомобилей
     */
    public Storage<Auto> getAutoStorage() {
        return autoStorage;
    }

    /**
     * Возвращает поставщика кузовов.
     *
     * @return поставщик кузовов
     */
    public BodySupplier getBodySupplier() {
        return bodySupplier;
    }

    /**
     * Возвращает поставщика двигателей.
     *
     * @return поставщик двигателей
     */
    public MotorSupplier getMotorSupplier() {
        return motorSupplier;
    }

    /**
     * Возвращает список поставщиков аксессуаров.
     *
     * @return список поставщиков аксессуаров
     */
    public List<AccessorySupplier> getAccessorySuppliers() {
        return accessorySuppliers;
    }

    /**
     * Возвращает список дилеров.
     *
     * @return список дилеров
     */
    public List<Dealer> getDealers() {
        return dealers;
    }

    /**
     * Возвращает количество произведённых автомобилей.
     *
     * @return атомарный счётчик произведённых автомобилей
     */
    public AtomicLong getProducedAutos() {
        return threadPool.getExecuteCount();
    }

    /**
     * Возвращает пул потоков фабрики.
     *
     * @return пул потоков
     */
    public ThreadPool getThreadPool() {
        return threadPool;
    }
}