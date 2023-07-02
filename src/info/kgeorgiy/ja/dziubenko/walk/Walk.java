package info.kgeorgiy.ja.dziubenko.walk;

import info.kgeorgiy.ja.dziubenko.walk.exceptions.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Walk {

    private static final String SHA_256 = "SHA-256";
    public static final int BUFFER_SIZE = 1024;

    private static void printErr(final String message) {
        System.err.println("VERIFIABLE ERROR => " + message);
    }

    public static void main(String[] args) {
        try {
            run(args);
        } catch (WalkException e) {
            printErr(e.getMessage());
        }
    }

    private static void run(String[] args) throws WalkException {
        checkArgs(args);

        final Path in = getPathFromArgs(args[0], "first");
        final Path out = getPathFromArgs(args[1], "second");

        final MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance(SHA_256);
        } catch (NoSuchAlgorithmException e) {
            throw new ShaAlgorithmWalkException(e.getMessage());
        }

        final HashCalculator hashCalculator = new HashCalculator(messageDigest, BUFFER_SIZE);

        try (BufferedReader reader = Files.newBufferedReader(in, StandardCharsets.UTF_8)) {
            createParentDirectory(out);
            try (BufferedWriter writer = Files.newBufferedWriter(out, StandardCharsets.UTF_8)) {
                String fileName;
                while ((fileName = readFileName(reader)) != null) {
                    String fileHash = hashCalculator.calculateFileHash(fileName);
                    writeFileData(fileName, fileHash, writer);
                }
                writer.flush();
            } catch (IOException | SecurityException e) {
                throw new WritingWalkException("Can't write to output file: " + e.getMessage());
            }
        } catch (IOException | SecurityException e) {
            throw new ReadingWalkException("Can't read from input file: " + e.getMessage());
        }
    }

    private static void checkArgs(final String[] args) throws ArgumentsWalkException {
        if (args == null || args.length != 2) {
            throw new ArgumentsWalkException("Expected two file names");
        }
        if (args[0] == null || args[0].isBlank()) {
            throw new ArgumentsWalkException("First argument can't be empty");
        }
        if (args[1] == null || args[1].isBlank()) {
            throw new ArgumentsWalkException("Second argument can't be empty");
        }
    }

    private static Path getPathFromArgs(final String fileName, final String argNumber) throws PathWalkException {
        try {
            return Path.of(fileName);
        } catch (InvalidPathException e) {
            throw new PathWalkException(
                    "Invalid path name of " + argNumber + " argument: " + e.getMessage());
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

    private static String readFileName(final BufferedReader reader) throws ReadingWalkException {
        try {
            return reader.readLine();
        } catch (IOException e) {
            throw new ReadingWalkException("On reading error: " + e.getMessage());
        }
    }

    private static void writeFileData(
            final String fileName, final String fileSHA, final BufferedWriter writer) throws WritingWalkException {
        try {
            writer.write(fileSHA + " " + fileName);
            writer.newLine();
        } catch (IOException e) {
            throw new WritingWalkException("On writing error: " + e.getMessage());
        }
    }
}