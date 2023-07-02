package info.kgeorgiy.ja.dziubenko.i18n;

import info.kgeorgiy.ja.dziubenko.i18n.statistics.DateStatistics;
import info.kgeorgiy.ja.dziubenko.i18n.statistics.NumberStatistics;
import info.kgeorgiy.ja.dziubenko.i18n.statistics.StringStatistics;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Date;
import java.util.ResourceBundle;

public class TextFormatter {

    private final ResourceBundle bundle;

    public TextFormatter(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    public String summaryStatisticsFormat(String inputFile, int sentences, int words, int numbers, int currencies, int dates) {
        return String.format("""
                        %s "%s"
                        %s
                            %s %s.
                            %s %s.
                            %s %s.
                            %s %s.
                            %s %s.
                        """, bundle.getObject("analyzedFile"), inputFile,
                bundle.getObject("summaryStatistics"),
                bundle.getObject("sentencesCount"), sentences,
                bundle.getObject("wordsCount"), words,
                bundle.getObject("numbersCount"), numbers,
                bundle.getObject("currenciesCount"), currencies,
                bundle.getObject("datesCount"), dates);
    }

    public String sentenceStatisticsFormat(StringStatistics sentence) {
        return String.format("""
                        %s
                            %s %s (%s %s).
                            %s "%s".
                            %s "%s".
                            %s %s ("%s").
                            %s %s ("%s").
                            %s %s.
                        """,
                bundle.getObject("sentencesStatistics"),
                bundle.getObject("sentencesCount"), sentence.count(), sentence.unique(), bundle.getObject("different"),
                bundle.getObject("minSentence"), sentence.minByValue(),
                bundle.getObject("maxSentence"), sentence.maxByValue(),
                bundle.getObject("minLengthSentence"), checkedStringLength(sentence.minByLength()), sentence.minByLength(),
                bundle.getObject("maxLengthSentence"), checkedStringLength(sentence.maxByLength()), sentence.maxByLength(),
                bundle.getObject("averageLengthSentence"), sentence.average());
    }

    public String wordStatisticsFormat(StringStatistics word) {
        return String.format("""
                        %s
                            %s %s (%s %s).
                            %s "%s".
                            %s "%s".
                            %s %s ("%s").
                            %s %s ("%s").
                            %s %s.
                        """,
                bundle.getObject("wordStatistics"),
                bundle.getObject("wordsCount"), word.count(), word.unique(), bundle.getObject("different"),
                bundle.getObject("minWord"), word.minByValue(),
                bundle.getObject("maxWord"), word.maxByValue(),
                bundle.getObject("minLengthWord"), checkedStringLength(word.minByLength()), word.minByLength(),
                bundle.getObject("maxLengthWord"), checkedStringLength(word.maxByLength()), word.maxByLength(),
                bundle.getObject("averageLengthWord"), word.average());
    }

    public String numberStatisticsFormat(NumberStatistics number, NumberFormat format) {
        return String.format("""
                        %s
                            %s %s (%s %s).
                            %s %s.
                            %s %s.
                            %s %s.
                        """,
                bundle.getObject("numbersStatistics"),
                bundle.getObject("numbersCount"), number.count(), number.unique(), bundle.getObject("different"),
                bundle.getObject("minNumber"), checkedNumberFormat(number.minByValue(), format),
                bundle.getObject("maxNumber"), checkedNumberFormat(number.maxByValue(), format),
                bundle.getObject("averageNumber"), checkedNumberFormat(number.average(), format));
    }

    public String currencyStatisticsFormat(NumberStatistics currencyStatistics, NumberFormat format) {
        return String.format("""
                        %s
                            %s %s (%s %s).
                            %s %s.
                            %s %s.
                            %s %s.
                        """,
                bundle.getObject("currenciesStatistics"),
                bundle.getObject("currenciesCount"), currencyStatistics.count(), currencyStatistics.unique(), bundle.getObject("different"),
                bundle.getObject("minCurrency"), checkedNumberFormat(currencyStatistics.minByValue(), format),
                bundle.getObject("maxCurrency"), checkedNumberFormat(currencyStatistics.maxByValue(), format),
                bundle.getObject("averageCurrency"), checkedNumberFormat(currencyStatistics.average(), format));
    }

    public String dateStatisticsFormat(DateStatistics date, DateFormat format) {
        return String.format("""
                        %s
                            %s %s (%s %s).
                            %s %s.
                            %s %s.
                            %s %s.
                        """,
                bundle.getObject("datesStatistics"),
                bundle.getObject("datesCount"), date.count(), date.unique(), bundle.getObject("different"),
                bundle.getObject("minDate"), checkedDateFormat(date.minByValue(), format),
                bundle.getObject("maxDate"), checkedDateFormat(date.maxByValue(), format),
                bundle.getObject("averageDate"), checkedDateFormat(date.average(), format));
    }

    private static String checkedNumberFormat(Double value, NumberFormat format) {
        return value == null ? null : format.format(value);
    }

    private static String checkedDateFormat(Date value, DateFormat format) {
        return value == null ? null : format.format(value);
    }

    private static String checkedStringLength(String value) {
        return value == null ? null : String.valueOf(value.length());
    }

}