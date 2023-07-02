package info.kgeorgiy.ja.dziubenko.i18n.statistics;

public interface Statistics<T, A> {
    int count();

    int unique();

    T minByValue();

    T maxByValue();

    A average();
}
