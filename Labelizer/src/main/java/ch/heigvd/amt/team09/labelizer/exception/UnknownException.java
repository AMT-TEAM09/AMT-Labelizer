package ch.heigvd.amt.team09.labelizer.exception;

public class UnknownException extends RuntimeException {
    public UnknownException() {
        super("Internal server error");
    }
}
