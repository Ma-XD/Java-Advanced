package info.kgeorgiy.ja.dziubenko.i18n.statistics;

import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.function.ToDoubleFunction;

public abstract class EntityStatistics<T, A> implements Statistics<T, A> {
    private final int count;
    private final int unique;
    private final T minByValue;
    private final T maxByValue;
    private final A average;

    protected EntityStatistics(Comparator<? super T> comparator, List<T> entities) {
        count = entities.size();
        TreeSet<T> set = new TreeSet<>(comparator);
        set.addAll(entities);
        unique = set.size();
        if (unique > 0) {
            minByValue = set.first();
            maxByValue = set.last();
        } else {
            minByValue = null;
            maxByValue = null;
        }
        average = averageImpl(entities);
    }

    @Override
    public int count() {
        return count;
    }

    @Override
    public int unique() {
        return unique;
    }

    @Override
    public T maxByValue() {
        return maxByValue;
    }

    @Override
    public T minByValue() {
        return minByValue;
    }

    @Override
    public A average() {
        return average;
    }

    abstract A averageImpl(List<T> list);

    protected static <T> double getAverage(ToDoubleFunction<T> mapper, List<T> list) {
        return list.stream().mapToDouble(mapper).average().orElse(0);
    }
}
