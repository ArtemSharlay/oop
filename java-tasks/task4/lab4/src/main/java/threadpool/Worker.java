package threadpool;

import task.Task;

/**
 * Рабочий поток, который выполняет задачи из пула потоков.
 * Постоянно ожидает появления новой задачи в очереди и выполняет её.
 */
public class Worker extends Thread {
    private final ThreadPool threadPool;

    /**
     * Конструктор рабочего потока.
     *
     * @param threadPool пул потоков, из которого будут браться задачи
     */
    public Worker(ThreadPool threadPool) {
        this.threadPool = threadPool;
    }

    /**
     * Основной метод рабочего потока.
     * Циклически получает задачи из пула и выполняет их.
     * При прерывании потока корректно завершает работу.
     */
    @Override
    public void run() {
        try {
            while (!isInterrupted()) {
                Task task = threadPool.getTask();
                task.execute();
                threadPool.taskExecuted();
            }
        } catch (InterruptedException e) {
            interrupt();
        }
    }
}