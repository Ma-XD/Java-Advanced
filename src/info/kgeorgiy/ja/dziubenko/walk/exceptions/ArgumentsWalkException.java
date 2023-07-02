package info.kgeorgiy.ja.dziubenko.walk.exceptions;

public class ArgumentsWalkException extends WalkException {
    public ArgumentsWalkException(String message) {
        super("Unexpected arguments: " + message);
    }
}
