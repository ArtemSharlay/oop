package supplier;

import model.Body;
import storage.Storage;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Поставщик кузовов.
 * Работает в отдельном потоке и периодически создаёт новые кузова, помещая их на склад.
 */
public class BodySupplier extends Thread {
    private final Storage<Body> storage;
    private int supplyDelayMs;
    private static final AtomicLong supplyCount = new AtomicLong(0);
    private volatile boolean running = true;

    /**
     * Конструктор поставщика кузовов.
     *
     * @param storage       склад для хранения кузовов
     * @param supplyDelayMs задержка между поставками в миллисекундах
     */
    public BodySupplier(Storage<Body> storage, int supplyDelayMs) {
        this.storage = storage;
        this.supplyDelayMs = supplyDelayMs;
    }

    /**
     * Основной метод потока поставщика.
     * Циклически ожидает заданную задержку, создаёт новый кузов и помещает его на склад.
     */
    @Override
    public void run() {
        try {
            while (running) {
                Thread.sleep(supplyDelayMs);

                Body body = new Body();
                supplyCount.incrementAndGet();
                storage.put(body);

                System.out.println("Produced " + body + " id = " + body.getId());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("BodySupplier stopped.");
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
     * Возвращает общее количество поставленных кузовов.
     *
     * @return атомарный счётчик поставленных кузовов
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