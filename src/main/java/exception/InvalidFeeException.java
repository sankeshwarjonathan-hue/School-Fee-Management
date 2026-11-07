package exception;

/** Thrown when a fee amount or class is invalid (from Phase 2). */
public class InvalidFeeException extends Exception {
    public InvalidFeeException(String message) {
        super(message);
    }
}
