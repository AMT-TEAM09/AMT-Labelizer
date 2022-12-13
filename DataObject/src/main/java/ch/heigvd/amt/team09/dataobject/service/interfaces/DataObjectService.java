package ch.heigvd.amt.team09.dataobject.service.interfaces;

import java.io.InputStream;
import java.net.URL;
import java.time.Duration;

public interface DataObjectService {
    Duration DEFAULT_URL_EXPIRATION_TIME = Duration.ofSeconds(90);

    void create(String objectName, byte[] content) throws ObjectAlreadyExistsException;

    boolean exists(String objectName);

    boolean exists();

    default void delete() throws ObjectNotFoundException, ObjectNotEmptyException {
        delete(false);
    }

    void delete(boolean recursive) throws ObjectNotFoundException, ObjectNotEmptyException;

    default void delete(String objectName) throws ObjectNotFoundException, ObjectNotEmptyException {
        delete(objectName, false);
    }

    void delete(String objectName, boolean recursive) throws ObjectNotFoundException, ObjectNotEmptyException;

    URL publish(String objectName, Duration urlDuration) throws ObjectNotFoundException;

    default URL publish(String objectName) throws ObjectNotFoundException {
        return publish(objectName, DEFAULT_URL_EXPIRATION_TIME);
    }

    InputStream get(String objectName) throws ObjectNotFoundException;

    class DataObjectException extends Exception {
        public DataObjectException(String message) {
            super(message);
        }
    }

    class ObjectNotFoundException extends DataObjectException {
        public ObjectNotFoundException(String objectName) {
            super("Object %s does not exist".formatted(objectName));
        }
    }

    class ObjectAlreadyExistsException extends DataObjectException {
        public ObjectAlreadyExistsException(String objectName) {
            super("Object %s already exists".formatted(objectName));
        }
    }

    class ObjectNotEmptyException extends DataObjectException {
        public ObjectNotEmptyException(String objectName) {
            super("Object %s is not empty".formatted(objectName));
        }
    }
}

