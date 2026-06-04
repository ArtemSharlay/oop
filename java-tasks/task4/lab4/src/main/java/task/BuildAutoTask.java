package task;

import model.Accessory;
import model.Auto;
import model.Body;
import model.Motor;
import storage.Storage;

/**
 * Задача по сборке одного автомобиля.
 * Забирает со складов кузов, двигатель и аксессуар, собирает автомобиль и помещает его на склад готовых авто.
 */
public class BuildAutoTask implements Task {

    private final Storage<Body> bodyStorage;
    private final Storage<Motor> motorStorage;
    private final Storage<Accessory> accessoryStorage;
    private final Storage<Auto> autoStorage;

    /**
     * Конструктор задачи сборки автомобиля.
     *
     * @param bodyStorage      склад кузовов
     * @param motorStorage     склад двигателей
     * @param accessoryStorage склад аксессуаров
     * @param autoStorage      склад готовых автомобилей
     */
    public BuildAutoTask(Storage<Body> bodyStorage,
                         Storage<Motor> motorStorage,
                         Storage<Accessory> accessoryStorage,
                         Storage<Auto> autoStorage) {
        this.bodyStorage = bodyStorage;
        this.motorStorage = motorStorage;
        this.accessoryStorage = accessoryStorage;
        this.autoStorage = autoStorage;
    }

    /**
     * Выполняет сборку автомобиля.
     * Последовательно забирает детали со складов, создаёт автомобиль и помещает его на склад.
     * При прерывании потока корректно завершает выполнение.
     */
    @Override
    public void execute() {
        try {
            Body body = bodyStorage.take();
            Motor motor = motorStorage.take();
            Accessory accessory = accessoryStorage.take();
            Auto auto = new Auto(body, motor, accessory);
            autoStorage.put(auto);
            System.out.println(Thread.currentThread().getName() + " built " + auto);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}