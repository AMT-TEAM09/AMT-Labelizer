package ch.heigvd.amt.team09.labelizer.exception;

public class InvalidBase64Exception extends RuntimeException {
    public InvalidBase64Exception(String message) {
        super("Base64 string is invalid: " + message);
    }
}
