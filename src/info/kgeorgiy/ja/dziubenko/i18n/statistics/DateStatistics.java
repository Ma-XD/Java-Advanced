package info.kgeorgiy.ja.dziubenko.i18n.statistics;

import java.util.Date;
import java.util.List;

public class DateStatistics extends EntityStatistics<Date, Date> {
    public DateStatistics(List<Date> dates) {
        super(Date::compareTo, dates);
    }

    @Override
    Date averageImpl(List<Date> list) {
        return list.isEmpty() ? null : new Date((long) getAverage(Date::getTime, list));
    }
}
