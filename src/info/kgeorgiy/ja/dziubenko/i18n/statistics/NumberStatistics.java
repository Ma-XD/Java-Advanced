package info.kgeorgiy.ja.dziubenko.i18n.statistics;

import java.util.List;

public class NumberStatistics extends EntityStatistics<Double, Double> {
    public NumberStatistics(List<Double> numbers) {
        super(Double::compare, numbers);
    }

    @Override
    Double averageImpl(List<Double> list) {
        return list.isEmpty() ? null : getAverage(Double::doubleValue, list);
    }
}
