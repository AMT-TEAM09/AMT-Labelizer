package ch.heigvd.amt.team09.labelizer.exception;

public class UnreachableUrlException extends RuntimeException {
    public UnreachableUrlException(String url) {
        super("URL is unreachable: " + url);
    }
}
