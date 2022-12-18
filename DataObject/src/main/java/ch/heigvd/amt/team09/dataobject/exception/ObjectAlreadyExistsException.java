package ch.heigvd.amt.team09.dataobject.exception;

public class ObjectAlreadyExistsException extends RuntimeException {
    public ObjectAlreadyExistsException(String objectName) {
        super("Object with name '%s' already exists".formatted(objectName));
    }
}
