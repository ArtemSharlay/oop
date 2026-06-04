package threadpool;

import task.Task;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Пул потоков для выполнения задач типа Task.
 * Управляет очередью задач и рабочими потоками.
 */
public class ThreadPool {
    private final Queue<Task> taskQueue = new LinkedList<>();
    private final Worker[] workers;
    private static final AtomicLong executeCount = new AtomicLong(0);

    /**
     * Конструктор пула потоков.
     * Создаёт указанное количество рабочих потоков и запускает их как виртуальные потоки.
     *
     * @param workerCount количество рабочих потоков в пуле
     */
    public ThreadPool(int workerCount) {
        workers = new Worker[workerCount];

        for (int i = 0; i < workerCount; i++) {
            workers[i] = new Worker(this);
            Thread.ofVirtual().start(workers[i]);
        }
    }

    /**
     * Добавляет задачу в очередь.
     * Уведомляет ожидающие потоки о появлении новой задачи.
     *
     * @param task добавляемая задача
     */
    public synchronized void addTask(Task task) {
        taskQueue.add(task);
        notifyAll();
    }

    /**
     * Извлекает задачу из очереди.
     * Если очередь пуста, поток ожидает появления задачи.
     *
     * @return задача из очереди
     * @throws InterruptedException если ожидание было прервано
     */
    public synchronized Task getTask() throws InterruptedException {
        while (taskQueue.isEmpty()) {
            wait();
        }
        Task task = taskQueue.remove();
        notifyAll();
        return task;
    }

    /**
     * Возвращает текущий размер очереди задач.
     *
     * @return количество задач в очереди
     */
    public synchronized int getQueueSize() {
        return taskQueue.size();
    }

    /**
     * Останавливает работу пула потоков.
     * Прерывает все рабочие потоки.
     */
    public void shutdown() {
        for (Worker worker : workers) {
            worker.interrupt();
        }
    }

    /**
     * Увеличивает счётчик выполненных задач.
     * Вызывается после успешного выполнения задачи.
     */
    public void taskExecuted() {
        executeCount.incrementAndGet();
    }

    /**
     * Возвращает количество выполненных задач.
     *
     * @return атомарный счётчик выполненных задач
     */
    public AtomicLong getExecuteCount() {
        return executeCount;
    }
}