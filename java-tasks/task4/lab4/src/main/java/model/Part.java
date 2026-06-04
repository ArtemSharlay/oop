package model;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Абстрактный базовый класс для всех деталей автомобиля.
 * Обеспечивает автоматическую генерацию уникальных идентификаторов для каждой детали.
 */
public abstract class Part {
    private static final AtomicLong ID_GENERATOR = new AtomicLong(0);
    protected final long id;

    /**
     * Конструктор детали.
     * При создании автоматически присваивает уникальный идентификатор.
     */
    public Part() {
        this.id = ID_GENERATOR.incrementAndGet();
    }

    /**
     * Возвращает уникальный идентификатор детали.
     *
     * @return идентификатор детали
     */
    public long getId() {
        return id;
    }
}