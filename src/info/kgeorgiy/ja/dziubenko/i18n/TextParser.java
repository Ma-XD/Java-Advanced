package info.kgeorgiy.ja.dziubenko.i18n;

import java.text.BreakIterator;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.*;

public class TextParser {
    private final List<String> sentences = new ArrayList<>();
    private final List<String> words = new ArrayList<>();
    private final List<Number> numbers = new ArrayList<>();
    private final List<Number> currencies = new ArrayList<>();
    private final List<Date> dates = new ArrayList<>();
    private final NumberFormat currencyFormat;
    private final NumberFormat numberFormat;
    private final List<DateFormat> dateFormats;
    private final BreakIterator sentenceIterator;
    private final BreakIterator wordsIterator;

    public TextParser(Locale locale) {
        currencyFormat = NumberFormat.getCurrencyInstance(locale);
        numberFormat = NumberFormat.getNumberInstance(locale);
        dateFormats = List.of(DateFormat.getDateInstance(DateFormat.FULL, locale),
                DateFormat.getDateInstance(DateFormat.LONG, locale),
                DateFormat.getDateInstance(DateFormat.MEDIUM, locale),
                DateFormat.getDateInstance(DateFormat.SHORT, locale));
        sentenceIterator = BreakIterator.getSentenceInstance(locale);
        wordsIterator = BreakIterator.getWordInstance(locale);
    }

    public List<String> getSentences() {
        return Collections.unmodifiableList(sentences);
    }

    public List<String> getWords() {
        return Collections.unmodifiableList(words);
    }

    public List<Number> getNumbers() {
        return Collections.unmodifiableList(numbers);
    }

    public List<Number> getCurrencies() {
        return Collections.unmodifiableList(currencies);
    }

    public List<Date> getDates() {
        return Collections.unmodifiableList(dates);
    }

    public void clear() {
        sentences.clear();
        words.clear();
        numbers.clear();
        currencies.clear();
        dates.clear();
    }

    public void parse(String text) {
        clear();
        parseText(text);
        for (String sentence : sentences) {
            parseSentence(sentence);
        }
    }

    private void parseText(String text) {
        sentenceIterator.setText(text);
        String sentence;
        while ((sentence = nextString(sentenceIterator, text)) != null) {
            if (sentence.isBlank()) {
                continue;
            }
            sentences.add(sentence.trim());
        }
    }

    private void parseSentence(String sentence) {
        wordsIterator.setText(sentence);
        ParsePosition position = new ParsePosition(wordsIterator.current());
        while (position.getIndex() != sentence.length()) {
            Date date;
            Number number;
            if ((date = parseDate(sentence, position)) != null) {
                dates.add(date);
            } else if ((number = currencyFormat.parse(sentence, position)) != null) {
                currencies.add(number);
            } else if ((number = numberFormat.parse(sentence, position)) != null) {
                numbers.add(number);
            }
            if (position.getIndex() != wordsIterator.current()) {
                wordsIterator.following(position.getIndex());
            } else {
                String word = nextString(wordsIterator, sentence);
                if (isWord(word)) {
                    words.add(word);
                }
            }
            position.setIndex(wordsIterator.current());
        }
    }

    private Date parseDate(String sentence, ParsePosition position) {
        Date date;
        for (DateFormat dateFormat : dateFormats) {
            if ((date = dateFormat.parse(sentence, position)) != null) {
                return date;
            }
        }
        return null;
    }

    private static boolean isWord(String word) {
        return word != null
                && !word.isBlank()
                && (word.length() > 1 || Character.isLetterOrDigit(word.charAt(0)));
    }

    private static String nextString(BreakIterator iterator, String text) {
        int start = iterator.current();
        int end = iterator.next();
        return end == BreakIterator.DONE ? null : text.substring(start, end);
    }
}
