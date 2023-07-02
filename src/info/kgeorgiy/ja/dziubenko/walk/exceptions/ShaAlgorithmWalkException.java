package info.kgeorgiy.ja.dziubenko.walk.exceptions;

public class ShaAlgorithmWalkException extends WalkException {
    public ShaAlgorithmWalkException(String message) {
        super("SHA algorithm error: " + message);
    }
}
