package interfaces;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

public interface DataObjectHelper {
    void create(String objectName);

    void create(String objectName, Path filePath);

    boolean exists(String objectName);

    void delete(String objectName);

    URL publish(String objectName);

    InputStream get(String objectName);
}