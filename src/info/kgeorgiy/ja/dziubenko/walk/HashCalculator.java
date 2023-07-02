package info.kgeorgiy.ja.dziubenko.walk;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.InvalidPathException;
import java.security.MessageDigest;
import java.util.HexFormat;

public class HashCalculator {
    private final MessageDigest messageDigest;
    private final String errorHash;
    private final byte[] buffer;

    public HashCalculator(final MessageDigest messageDigest, final int bufferSize) {
        this.messageDigest = messageDigest;
        errorHash = "0".repeat(messageDigest.getDigestLength() << 1);
        buffer = new byte[bufferSize];
    }

    public String calculateFileHash(final String fileName) {
        try (InputStream is = Files.newInputStream(Path.of(fileName))) {
            int readBytes;
            while ((readBytes = is.read(buffer)) >= 0) {
                messageDigest.update(buffer, 0, readBytes);
            }
            return HexFormat.of().formatHex(messageDigest.digest());
        } catch (IOException | SecurityException | InvalidPathException e) {
            messageDigest.reset();
            return errorHash;
        }
    }
}
