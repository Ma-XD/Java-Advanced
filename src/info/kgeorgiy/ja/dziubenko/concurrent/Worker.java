package info.kgeorgiy.ja.dziubenko.concurrent;

import java.util.Queue;

/**
 * Synchronized takes tasks from general queue and runs them
 * while current thread is not interrupted.
 *
 * @param tasks a {@link Queue queue} of {@link Runnable task} to be run.
 */
public record Worker(Queue<Runnable> tasks) implements Runnable {
    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                Runnable task = getTask();
                try {
                    task.run();
                } catch (RuntimeException ignored) {
                }
            }
        } catch (InterruptedException ignored) {
        } finally {
            Thread.currentThread().interrupt();
        }
    }

    private Runnable getTask() throws InterruptedException {
        synchronized (tasks) {
            while (tasks.isEmpty()) {
                tasks.wait();
            }
            return tasks.poll();
        }
    }
}
