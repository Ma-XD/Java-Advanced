package info.kgeorgiy.ja.dziubenko.i18n;

import info.kgeorgiy.ja.dziubenko.i18n.statistics.DateStatistics;
import info.kgeorgiy.ja.dziubenko.i18n.statistics.NumberStatistics;
import info.kgeorgiy.ja.dziubenko.i18n.statistics.StringStatistics;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.*;

public class TextStatistics {
    private static final String RESOURCE_BUNDLE_NAME = "info.kgeorgiy.ja.dziubenko.i18n.ResourceBundle";
    private static final Set<Locale> AVAILABLE_LOCALES = new HashSet<>(Arrays.asList(Locale.getAvailableLocales()));
    private static final String RUS = "ru";
    private static final String EN = "en";

    public static void main(String[] args) {
        try {
            run(args);
        } catch (IllegalArgumentException | IOException e) {
            System.err.println(e.getMessage());
        }

    }

    private static void run(String[] args) throws IOException {
        if (args == null || args.length != 4) {
            throw new IllegalArgumentException("Illegal arguments number. Expected: 4");
        }
        for (String arg : args) {
            if (arg == null || arg.isBlank()) {
                throw new IllegalArgumentException("Arguments can't be empty");
            }
        }
        Locale textLocale = Locale.of(args[0]);
        if (!AVAILABLE_LOCALES.contains(textLocale)) {
            throw new IllegalArgumentException("Unsupported text locale: " + args[0]);
        }
        if (!args[1].equals(RUS) && !args[1].equals(EN)) {
            throw new IllegalArgumentException("Unsupported output locale: " + args[1]);
        }
        Locale outLocale = Locale.of(args[1]);
        Path in = Path.of(args[2]);
        String text = Files.readString(in);
        TextParser parser = new TextParser(textLocale);
        parser.parse(text);

        StringStatistics sentenceStatistics = new StringStatistics(parser.getSentences(), textLocale);
        StringStatistics wordStatistics = new StringStatistics(parser.getWords(), textLocale);
        NumberStatistics numberStatistics = new NumberStatistics(parser.getNumbers()
                .stream()
                .map(Number::doubleValue)
                .toList());
        NumberStatistics currencyStatistics = new NumberStatistics(parser.getCurrencies()
                .stream()
                .map(Number::doubleValue)
                .toList());
        DateStatistics dateStatistics = new DateStatistics(parser.getDates());

        NumberFormat numberFormat = NumberFormat.getNumberInstance(outLocale);
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(outLocale);
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, outLocale);
        ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_NAME, outLocale);
        TextFormatter formatter = new TextFormatter(bundle);

        Path out = Path.of(args[3]);
        createParentDirectory(out);

        try (BufferedWriter writer = Files.newBufferedWriter(out, StandardCharsets.UTF_8)) {
            writer.write(formatter.summaryStatisticsFormat(in.toString(),
                    sentenceStatistics.count(),
                    wordStatistics.count(),
                    numberStatistics.count(),
                    currencyStatistics.count(),
                    dateStatistics.count()));
            writer.write(formatter.sentenceStatisticsFormat(sentenceStatistics));
            writer.write(formatter.wordStatisticsFormat(wordStatistics));
            writer.write(formatter.numberStatisticsFormat(numberStatistics, numberFormat));
            writer.write(formatter.currencyStatisticsFormat(currencyStatistics, currencyFormat));
            writer.write(formatter.dateStatisticsFormat(dateStatistics, dateFormat));
        }
    }


    private static void createParentDirectory(final Path out) {
        final Path parent = out.getParent();
        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (IOException | SecurityException ignored) {
            }
        }
    }
}
