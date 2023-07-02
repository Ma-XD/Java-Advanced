package info.kgeorgiy.ja.dziubenko.i18n.statistics;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class StringStatistics extends EntityStatistics<String, Double> {
    private static final Comparator<String> LENGTH_COMPARATOR = Comparator.comparingInt(String::length);
    private final String minByLength;
    private final String maxByLength;

    public StringStatistics(List<String> strings, Locale locale) {
        super(Collator.getInstance(locale), strings);
        minByLength = getMaxByLength(LENGTH_COMPARATOR.reversed(), strings);
        maxByLength = getMaxByLength(LENGTH_COMPARATOR, strings);
    }

    @Override
    Double averageImpl(List<String> list) {
        return list.isEmpty() ? null : getAverage(String::length, list);
    }

    public String minByLength() {
        return minByLength;
    }

    public String maxByLength() {
        return maxByLength;
    }

    private static String getMaxByLength(Comparator<String> comparator, List<String> list) {
        return list.stream().max(comparator).orElse(null);
    }
}
