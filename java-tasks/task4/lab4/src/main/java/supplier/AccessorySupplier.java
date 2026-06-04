package supplier;

import model.Accessory;
import storage.Storage;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Поставщик аксессуаров.
 * Работает в отдельном потоке и периодически создаёт новые аксессуары, помещая их на склад.
 */
public class AccessorySupplier extends Thread {
    private final Storage<Accessory> storage;
    private int supplyDelayMs;
    private static final AtomicLong supplyCount = new AtomicLong(0);
    private volatile boolean running = true;

    /**
     * Конструктор поставщика аксессуаров.
     *
     * @param storage       склад для хранения аксессуаров
     * @param supplyDelayMs задержка между поставками в миллисекундах
     */
    public AccessorySupplier(Storage<Accessory> storage, int supplyDelayMs) {
        this.storage = storage;
        this.supplyDelayMs = supplyDelayMs;
    }

    /**
     * Основной метод потока поставщика.
     * Циклически ожидает заданную задержку, создаёт новый аксессуар и помещает его на склад.
     */
    @Override
    public void run() {
        try {
            while (running) {
                Thread.sleep(supplyDelayMs);

                Accessory accessory = new Accessory();
                supplyCount.incrementAndGet();
                storage.put(accessory);

                System.out.println("Produced " + accessory + " id = " + accessory.getId());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("AccessorySupplier stopped.");
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
     * Возвращает общее количество поставленных аксессуаров.
     *
     * @return атомарный счётчик поставленных аксессуаров
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