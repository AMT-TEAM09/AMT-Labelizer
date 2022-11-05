package ch.heigvd.amt.team09.impl.aws;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.model.InvalidImageFormatException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class AwsLabelHelperTest {
    private static final Path RESOURCE_PATH = Paths.get("src", "test", "resources");
    private static final Path IMAGE_FILE = RESOURCE_PATH.resolve("image.jpg");
    private static final String IMAGE_URL = "https://upload.wikimedia.org/wikipedia/commons/6/6b/American_Beaver.jpg";
    private AwsLabelHelper labelHelper;

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

    @BeforeEach
    void setUp() {
        Dotenv dotenv = Dotenv.configure().load();

        var profile = dotenv.get("AWS_PROFILE");
        var region = Region.of(dotenv.get("AWS_REGION"));
        var credentialsProvider = ProfileCredentialsProvider.create(profile);

        labelHelper = new AwsLabelHelper(credentialsProvider, region);
    }

    @AfterEach
    void tearDown() {

    }

    @Test
    void execute_NoOptions_success() throws IOException {
        // given
        var imageUrl = IMAGE_URL;
        var expectedLabelName = "Beaver";

        assertTrue(isUrlValid(imageUrl));

        // when
        var labels = labelHelper.execute(imageUrl, AwsLabelHelper.NO_OPTIONS);

        // then
        assertTrue(labels.length > 0);
        assertTrue(Arrays.stream(labels).anyMatch(l -> l.name().equals(expectedLabelName)));
    }

    @Test
    void execute_withMinConfidence_success() throws IOException {
        // given
        var imageUrl = IMAGE_URL;
        var minConfidence = 99;

        assertTrue(isUrlValid(imageUrl));

        // when
        var labels = labelHelper.execute(imageUrl, options -> options.minConfidence(minConfidence));

        // then
        assertTrue(labels.length > 0);
        assertTrue(Arrays.stream(labels).allMatch(l -> l.confidence() >= minConfidence));
    }

    @Test
    void execute_withMaxLabels_success() throws IOException {
        // given
        var imageUrl = IMAGE_URL;
        var maxLabels = 1;

        assertTrue(isUrlValid(imageUrl));

        // when
        var labels = labelHelper.execute(imageUrl, options -> options.maxLabels(maxLabels));

        // then
        assertEquals(labels.length, maxLabels);
    }

    @Test
    void execute_withMinConfidenceAndMaxLabels_success() throws IOException {
        // given
        var imageUrl = IMAGE_URL;
        var minConfidence = 99;
        var maxLabels = 1;

        assertTrue(isUrlValid(imageUrl));

        // when
        var labels = labelHelper.execute(imageUrl, options -> {
                    options.minConfidence(minConfidence);
                    options.maxLabels(maxLabels);
                }
        );

        // then
        assertEquals(labels.length, maxLabels);
        assertTrue(Arrays.stream(labels).allMatch(l -> l.confidence() >= minConfidence));
    }

    @Test
    void executeFromBase64_noOptions_success() throws IOException {
        // given
        var imageString = getImageAsBase64();
        var expectedLabelName = "Phone";

        // when
        var labels = labelHelper.executeFromBase64(imageString, AwsLabelHelper.NO_OPTIONS);

        // then
        assertTrue(labels.length > 0);
        assertTrue(Arrays.stream(labels).anyMatch(l -> l.name().equals(expectedLabelName)));
    }

    @Test
    void executeFromBase64_withMinConfidence_success() throws IOException {
        // given
        var imageString = getImageAsBase64();
        var minConfidence = 90;

        // when
        var labels = labelHelper.executeFromBase64(imageString, options -> options.minConfidence(minConfidence));

        // then
        assertTrue(labels.length > 0);
        assertTrue(Arrays.stream(labels).allMatch(l -> l.confidence() >= minConfidence));
    }

    @Test
    void executeFromBase64_withMaxLabels_success() throws IOException {
        // given
        var imageString = getImageAsBase64();
        var maxLabels = 1;

        // when
        var labels = labelHelper.executeFromBase64(imageString, options -> options.maxLabels(maxLabels));

        // then
        assertEquals(labels.length, maxLabels);
    }

    @Test
    void executeFromBase64_withMinConfidenceAndMaxLabels_success() throws IOException {
        // given
        var imageString = getImageAsBase64();
        var minConfidence = 90;
        var maxLabels = 1;

        // when
        var labels = labelHelper.executeFromBase64(imageString, options -> {
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
        assertThrows(InvalidImageFormatException.class, () -> {
            labelHelper.executeFromBase64(imageString, AwsLabelHelper.NO_OPTIONS);
        });
    }
}