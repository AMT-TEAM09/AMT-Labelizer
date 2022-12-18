package ch.heigvd.amt.team09.dataobject.exception;

public class ObjectNotFoundException extends RuntimeException {
    public ObjectNotFoundException(String objectName) {
        super("Object '%s' not found".formatted(objectName));
    }
}
