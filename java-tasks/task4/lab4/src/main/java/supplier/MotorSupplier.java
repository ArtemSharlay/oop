package supplier;

import model.Motor;
import storage.Storage;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Поставщик двигателей.
 * Работает в отдельном потоке и периодически создаёт новые двигатели, помещая их на склад.
 */
public class MotorSupplier extends Thread {
    private final Storage<Motor> storage;
    private int supplyDelayMs;
    private static final AtomicLong supplyCount = new AtomicLong(0);
    private volatile boolean running = true;

    /**
     * Конструктор поставщика двигателей.
     *
     * @param storage       склад для хранения двигателей
     * @param supplyDelayMs задержка между поставками в миллисекундах
     */
    public MotorSupplier(Storage<Motor> storage, int supplyDelayMs) {
        this.storage = storage;
        this.supplyDelayMs = supplyDelayMs;
    }

    /**
     * Основной метод потока поставщика.
     * Циклически ожидает заданную задержку, создаёт новый двигатель и помещает его на склад.
     */
    @Override
    public void run() {
        try {
            while (running) {
                Thread.sleep(supplyDelayMs);

                Motor motor = new Motor();
                supplyCount.incrementAndGet();
                storage.put(motor);

                System.out.println("Produced " + motor + " id = " + motor.getId());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("MotorSupplier stopped.");
        }
    }

    /**
     * Останавливает работу поставщика.
     * Устанавливает флаг остановки и прерывает поток.
     */
    public void stopSupplier() {
        running = false;
        this.interrupt();
    }

    /**
     * Возвращает общее количество поставленных двигателей.
     *
     * @return атомарный счётчик поставленных двигателей
     */
    public AtomicLong getCount() {
        return supplyCount;
    }

    /**
     * Устанавливает новую задержку между поставками.
     *
     * @param supplyDelayMs задержка в миллисекундах
     */
    public void setSupplyDelayMs(int supplyDelayMs) {
        this.supplyDelayMs = supplyDelayMs;
    }
}