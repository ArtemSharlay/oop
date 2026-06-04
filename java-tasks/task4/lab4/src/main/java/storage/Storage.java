package storage;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Потокобезопасное хранилище (склад) для деталей или готовых автомобилей.
 * Использует механизм ожидания/уведомления для синхронизации доступа.
 *
 * @param <T> тип хранимых объектов
 */
public class Storage<T> {
    private final int capacity;
    private final Queue<T> items;

    /**
     * Конструктор склада.
     *
     * @param capacity максимальная вместимость склада
     */
    public Storage(int capacity) {
        this.capacity = capacity;
        this.items = new LinkedList<>();
    }

    /**
     * Добавляет элемент на склад.
     * Если склад заполнен, поток ждёт освобождения места.
     *
     * @param item добавляемый элемент
     * @throws InterruptedException если ожидание было прервано
     */
    public synchronized void put(T item) throws InterruptedException {
        while (items.size() >= capacity) {
            wait();
        }
        items.add(item);
        notifyAll();
    }

    /**
     * Забирает элемент со склада.
     * Если склад пуст, поток ждёт появления элемента.
     *
     * @return извлечённый элемент
     * @throws InterruptedException если ожидание было прервано
     */
    public synchronized T take() throws InterruptedException {
        while (items.isEmpty()) {
            wait();
        }
        T item = items.remove();
        notifyAll();
        return item;
    }

    /**
     * Возвращает текущее количество элементов на складе.
     *
     * @return количество элементов
     */
    public synchronized int getSize() {
        return items.size();
    }

    /**
     * Возвращает максимальную вместимость склада.
     *
     * @return вместимость склада
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Проверяет, пуст ли склад.
     *
     * @return true, если склад пуст, иначе false
     */
    public synchronized boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Проверяет, заполнен ли склад.
     *
     * @return true, если склад заполнен, иначе false
     */
    public synchronized boolean isFull() {
        return items.size() == capacity;
    }
}