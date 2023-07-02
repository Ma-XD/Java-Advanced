package info.kgeorgiy.ja.dziubenko.concurrent;

import java.util.*;
import java.util.function.Function;

public class ParallelMapperImpl implements ParallelMapper {
    private final List<Thread> threads;
    private final Queue<Runnable> tasks;

    /**
     * Constructs a ParallelMapper implementation with number of threads.
     *
     * @param threadsNumber number of concurrent threads.
     */
    public ParallelMapperImpl(int threadsNumber) {
        tasks = new ArrayDeque<>();
        threads = new ArrayList<>(threadsNumber);

        for (int i = 0; i < threadsNumber; i++) {
            Thread thread = new Thread(new Worker(tasks));
            thread.start();
            threads.add(thread);
        }
    }

    @Override
    public <T, R> List<R> map(
            Function<? super T, ? extends R> f,
            List<? extends T> args
    ) throws InterruptedException {
        final ArrayList<R> results = new ArrayList<>(Collections.nCopies(args.size(), null));
        final ArrayList<? extends T> argArray = new ArrayList<>(args);
        ConcurrentCounter taskCounter = new ConcurrentCounter();

        for (int i = 0; i < args.size(); i++) {
            addTask(f, results, i, argArray.get(i), taskCounter);
        }

        taskCounter.waitUntil(args.size());
        return results;
    }

    private <T, R> void addTask(
            Function<? super T, ? extends R> f,
            ArrayList<R> results,
            int setAt, T value,
            ConcurrentCounter counter
    ) {
        Runnable runnable = () -> {
            results.set(setAt, f.apply(value));
            counter.inc();
        };
        synchronized (tasks) {
            tasks.add(runnable);
            tasks.notify();
        }
    }

    @Override
    public void close() {
        for (Thread thread : threads) {
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException ignored) {
            }
        }
    }

    private static class ConcurrentCounter {
        private int value = 0;

        public synchronized void inc() {
            value++;
            notify();
        }

        public synchronized void waitUntil(int until) throws InterruptedException {
            while (value < until) {
                wait();
            }
        }
    }
}
