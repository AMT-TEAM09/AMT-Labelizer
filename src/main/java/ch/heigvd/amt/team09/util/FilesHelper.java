package ch.heigvd.amt.team09.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class FilesHelper {
    private FilesHelper() {
    }

    public static Path storeToTempFile(String fileName, String content) throws IOException {
        var filePath = Files.createTempFile(fileName, null);
        Files.writeString(filePath, content, StandardCharsets.UTF_8);

        return filePath;
    }

    public static Path storeBase64ToTempFile(String fileName, String base64) throws IOException {
        var split = base64.split(",");
        var image = split[split.length - 1];

        byte[] decodedImg = Base64.getDecoder()
                .decode(image.getBytes(StandardCharsets.UTF_8));

        var filePath = Files.createTempFile(fileName, null);
        Files.write(filePath, decodedImg);

        return filePath;
    }
}
