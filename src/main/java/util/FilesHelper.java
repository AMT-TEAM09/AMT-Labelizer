package util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

public class FilesHelper {
    private FilesHelper() {
    }

    public static Path storeToFile(String fileName, String content) throws IOException {
        var filePath = Files.createFile(Paths.get(fileName));
        Files.writeString(filePath, content, StandardCharsets.UTF_8);

        return filePath;
    }

    public static Path storeBase64ToFile(String fileName, String base64) throws IOException {
        var split = base64.split(",");
        var image = split[split.length - 1];

        byte[] decodedImg = Base64.getDecoder()
                .decode(image.getBytes(StandardCharsets.UTF_8));

        var filePath = Files.createFile(Paths.get(fileName));
        Files.write(filePath, decodedImg);

        return filePath;
    }
}
