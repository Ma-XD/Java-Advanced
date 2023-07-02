package info.kgeorgiy.ja.dziubenko.concurrent;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class IterativeParallelism implements ScalarIP {
    private final ParallelMapper parallelMapper;

    public IterativeParallelism() {
        parallelMapper = null;
    }

    public IterativeParallelism(ParallelMapper parallelMapper) {
        this.parallelMapper = parallelMapper;
    }

    private <T, R> R parallelWork(
            final int threads,
            final List<? extends T> values,
            final Function<Stream<? extends T>, R> work,
            final Function<Stream<? extends R>, R> combine
    ) throws InterruptedException {
        Objects.requireNonNull(values);
        int actualThreads = Math.max(1, Math.min(threads, values.size()));
        List<Stream<? extends T>> portions = divideIntoPortions(values, actualThreads);
        List<R> resultList = new ArrayList<>();

        if (parallelMapper == null) {
            List<Thread> threadList = new ArrayList<>(actualThreads);
            resultList.addAll(Collections.nCopies(actualThreads, null));
            for (int i = 0; i < actualThreads; i++) {
                threadList.add(runThreadWork(work, resultList, i, portions.get(i)));
            }
            for (Thread thread : threadList) {
                thread.join();
            }
        } else {
            resultList.addAll(parallelMapper.map(work, portions));
        }
        return combine.apply(resultList.stream());
    }

    private static <T> List<Stream<? extends T>> divideIntoPortions(List<? extends T> values, int actualThreads) {
        int portionSize = values.size() / actualThreads;
        int remainder = values.size() % actualThreads;
        List<Stream<? extends T>> portions = new ArrayList<>();

        int end = 0;
        for (int i = 0; i < actualThreads; i++) {
            int start = end;
            end += portionSize;
            if (remainder > 0) {
                remainder--;
                end++;
            }
            portions.add(values.subList(start, end).stream());
        }
        return portions;
    }

    private static <T, R> Thread runThreadWork(
            Function<Stream<? extends T>, R> work,
            List<R> resultList,
            int setAt,
            Stream<? extends T> portion
    ) {
        Runnable runnable = () -> resultList.set(setAt, work.apply(portion));
        Thread thread = new Thread(runnable);
        thread.start();
        return thread;
    }


    @Override
    public <T> T maximum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        final Function<Stream<? extends T>, T> work = stream -> stream.max(comparator).orElseThrow();
        return parallelWork(threads, values, work, work);
    }


    @Override
    public <T> T minimum(int threads, List<? extends T> values, Comparator<? super T> comparator) throws InterruptedException {
        return maximum(threads, values, comparator.reversed());
    }

    @Override
    public <T> boolean all(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return parallelWork(threads, values,
                stream -> stream.allMatch(predicate),
                stream -> stream.allMatch(b -> b));
    }

    @Override
    public <T> boolean any(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        return parallelWork(threads, values,
                stream -> stream.anyMatch(predicate),
                stream -> stream.anyMatch(b -> b));
    }

    @Override
    public <T> int count(int threads, List<? extends T> values, Predicate<? super T> predicate) throws InterruptedException {
        final long result = parallelWork(threads, values,
                stream -> stream.filter(predicate).count(),
                stream -> stream.mapToLong(l -> l).sum());
        return Math.toIntExact(result);
    }
}
