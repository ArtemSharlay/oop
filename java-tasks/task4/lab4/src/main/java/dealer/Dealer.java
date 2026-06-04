package dealer;

import config.Config;
import model.Auto;
import storage.Storage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Дилер, который покупает автомобили со склада.
 * Работает в отдельном потоке и периодически забирает автомобили с заданной задержкой.
 */
public class Dealer extends Thread {

    private final int dealerId;
    private final Storage<Auto> autoStorage;
    private Logger logger = LogManager.getLogger(Dealer.class);

    private volatile boolean running = true;
    private int requestDelayMs;

    /**
     * Конструктор дилера.
     *
     * @param dealerId       уникальный идентификатор дилера
     * @param autoStorage    склад автомобилей, откуда дилер будет их забирать
     * @param requestDelayMs задержка между запросами в миллисекундах
     */
    public Dealer(
            int dealerId,
            Storage<Auto> autoStorage,
            int requestDelayMs
    ) {
        this.dealerId = dealerId;
        this.autoStorage = autoStorage;
        this.requestDelayMs = requestDelayMs;
    }

    /**
     * Основной метод потока дилера.
     * Циклически ожидает заданную задержку и забирает автомобиль со склада.
     * При включённой настройке логирования записывает информацию о продаже.
     */
    @Override
    public void run() {
        try {
            while (running) {
                Thread.sleep(requestDelayMs);

                Auto auto = autoStorage.take();
                if (Config.logSale) {
                    logger.info(buildLogMessage(auto));
                }

                System.out.println(
                        "Dealer "
                                + dealerId
                                + " bought "
                                + auto
                );
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Формирует строку лога о продаже автомобиля.
     *
     * @param auto проданный автомобиль
     * @return строка с информацией об ID автомобиля и его составных частях
     */
    private String buildLogMessage(Auto auto) {
        return auto.getId()
                + " (Body: "
                + auto.getBody().getId()
                + ", Motor: "
                + auto.getMotor().getId()
                + ", Accessory: "
                + auto.getAccessory().getId()
                + ") by Dealer "
                + dealerId;
    }

    /**
     * Останавливает работу дилера.
     * Устанавливает флаг остановки и прерывает поток дилера.
     */
    public void stopDealer() {
        running = false;
        interrupt();
    }

    /**
     * Устанавливает новую задержку между запросами дилера.
     *
     * @param requestDelayMs задержка в миллисекундах
     */
    public void setRequestDelayMs(int requestDelayMs) {
        this.requestDelayMs = requestDelayMs;
    }

    /**
     * Возвращает идентификатор дилера.
     *
     * @return идентификатор дилера
     */
    public int getDealerId() {
        return dealerId;
    }
}