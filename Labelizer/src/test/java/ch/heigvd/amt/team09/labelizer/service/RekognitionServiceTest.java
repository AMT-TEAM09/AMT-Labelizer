package ch.heigvd.amt.team09.labelizer.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class RekognitionServiceTest {
    private static final Path RESOURCE_PATH = Paths.get("src", "test", "resources");
    private static final Path IMAGE_FILE = RESOURCE_PATH.resolve("image.jpg");
    private static final String IMAGE_URL = "https://upload.wikimedia.org/wikipedia/commons/6/6b/American_Beaver.jpg";
    private static RekognitionService rekognitionService;

    private static String getImageAsBase64() throws IOException {
        var fileBytes = Files.readAllBytes(IMAGE_FILE);
        return Base64.getEncoder().encodeToString(fileBytes);
    }

    private static boolean isUrlValid(String url) {
        try {
            var huc = (HttpURLConnection) new URL(url).openConnection();
            var responseCode = huc.getResponseCode();
            return responseCode == 200;
        } catch (IOException e) {
            return false;
        }
    }

    @BeforeAll
    static void setUp() {
        rekognitionService = new RekognitionService();
    }

    @Test
    void execute_unreachableUrl_errorThrown() {
        // given
        var unreachableUrl = "https://www.invalidurl.com";
        assertFalse(isUrlValid(unreachableUrl));

        // then
        assertThrows(IOException.class, () -> rekognitionService.execute(unreachableUrl, RekognitionService.NO_OPTIONS));
    }

    @Test
    void execute_malformedUrl_errorThrown() {
        // given
        var malformedUrl = "https/www.invalidurl";
        assertFalse(isUrlValid(malformedUrl));

        // then
        assertThrows(MalformedURLException.class, () -> rekognitionService.execute(malformedUrl, RekognitionService.NO_OPTIONS));
    }

    @Test
    void execute_noOptions_imageAnalyzed() {
        // given
        var imageUrl = IMAGE_URL;
        var expectedLabelName = "Beaver";

        assertTrue(isUrlValid(imageUrl));

        // when
        var labels = assertDoesNotThrow(() -> rekognitionService.execute(imageUrl, RekognitionService.NO_OPTIONS));

        // then
        assertTrue(labels.length > 0);
        assertTrue(Arrays.stream(labels).anyMatch(l -> l.name().equals(expectedLabelName)));
    }

    @Test
    void execute_withMinConfidence_imageAnalyzedWithOptions() {
        // given
        var imageUrl = IMAGE_URL;
        var minConfidence = 99;

        assertTrue(isUrlValid(imageUrl));

        // when
        var labels = assertDoesNotThrow(() ->
                rekognitionService.execute(imageUrl, options -> options.minConfidence(minConfidence))
        );

        // then
        assertTrue(labels.length > 0);
        assertTrue(Arrays.stream(labels).allMatch(l -> l.confidence() >= minConfidence));
    }

    @Test
    void execute_withMaxLabels_imageAnalyzedWithOptions() {
        // given
        var imageUrl = IMAGE_URL;
        var maxLabels = 1;

        assertTrue(isUrlValid(imageUrl));

        // when
        var labels = assertDoesNotThrow(() -> rekognitionService.execute(imageUrl, options -> options.maxLabels(maxLabels)));

        // then
        assertEquals(labels.length, maxLabels);
    }

    @Test
    void execute_withMinConfidenceAndMaxLabels_imageAnalyzedWithOptions() {
        // given
        var imageUrl = IMAGE_URL;
        var minConfidence = 99;
        var maxLabels = 1;

        assertTrue(isUrlValid(imageUrl));

        // when
        var labels = assertDoesNotThrow(() -> rekognitionService.execute(imageUrl, options -> {
                            options.minConfidence(minConfidence);
                            options.maxLabels(maxLabels);
                        }
                )
        );

        // then
        assertEquals(labels.length, maxLabels);
        assertTrue(Arrays.stream(labels).allMatch(l -> l.confidence() >= minConfidence));
    }

    @Test
    void executeFromBase64_noOptions_imageAnalyzed() {
        // given
        var imageString = assertDoesNotThrow(RekognitionServiceTest::getImageAsBase64);
        var expectedLabelName = "Phone";

        // when
        var labels = rekognitionService.executeFromBase64(imageString, RekognitionService.NO_OPTIONS);

        // then
        assertTrue(labels.length > 0);
        assertTrue(Arrays.stream(labels).anyMatch(l -> l.name().equals(expectedLabelName)));
    }

    @Test
    void executeFromBase64_withMinConfidence_imageAnalyzedWithOptions() {
        // given
        var imageString = assertDoesNotThrow(RekognitionServiceTest::getImageAsBase64);
        var minConfidence = 90;

        // when
        var labels = rekognitionService.executeFromBase64(imageString, options -> options.minConfidence(minConfidence));

        // then
        assertTrue(labels.length > 0);
        assertTrue(Arrays.stream(labels).allMatch(l -> l.confidence() >= minConfidence));
    }

    @Test
    void executeFromBase64_withMaxLabels_imageAnalyzedWithOptions() {
        // given
        var imageString = assertDoesNotThrow(RekognitionServiceTest::getImageAsBase64);
        var maxLabels = 1;

        // when
        var labels = rekognitionService.executeFromBase64(imageString, options -> options.maxLabels(maxLabels));

        // then
        assertEquals(labels.length, maxLabels);
    }

    @Test
    void executeFromBase64_withMinConfidenceAndMaxLabels_imageAnalyzedWithOptions() {
        // given
        var imageString = assertDoesNotThrow(RekognitionServiceTest::getImageAsBase64);
        var minConfidence = 90;
        var maxLabels = 1;

        // when
        var labels = rekognitionService.executeFromBase64(imageString, options -> {
            options.minConfidence(minConfidence);
            options.maxLabels(maxLabels);
        });

        // then
        assertEquals(labels.length, maxLabels);
        assertTrue(Arrays.stream(labels).allMatch(l -> l.confidence() >= minConfidence));
    }

    @Test
    void executeFromBase64_invalidBase64_errorThrown() {
        // given
        var imageString = "invalid";

        // then
        assertThrows(Exception.class, () -> rekognitionService.executeFromBase64(imageString, RekognitionService.NO_OPTIONS));
    }
}