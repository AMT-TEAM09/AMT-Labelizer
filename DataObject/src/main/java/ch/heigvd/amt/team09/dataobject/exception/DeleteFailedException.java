package ch.heigvd.amt.team09.dataobject.exception;

public class DeleteFailedException extends RuntimeException {
    public DeleteFailedException(String message) {
        super(message);
    }
}
