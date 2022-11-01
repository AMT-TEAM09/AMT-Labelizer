package impl.aws;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;

import java.io.IOException;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AwsLabelDetectorTest {
    private static final String BUCKET_NAME = "amt.team09.diduno.education";
    private ProfileCredentialsProvider credentialsProvider;
    private AwsLabelDetector labelDetector;

    @BeforeEach
    void setUp() {
        credentialsProvider = ProfileCredentialsProvider.create();
        labelDetector = new AwsLabelDetector(credentialsProvider);
    }

    @AfterEach
    void tearDown() {
        credentialsProvider.close();
    }

    @Test
    void itShouldDetectLabelsFromS3() {
        var imageUri = "test.jpg";
        var params = new int[0];
        var result = labelDetector.Execute(imageUri, params);
        System.out.println(result);
    }

    @Test
    void itShouldDetectLabelsFromBase64() throws IOException {
        String imageString;
        try (var stream = getClass().getClassLoader().getResourceAsStream("test.jpg")) {
            assertNotNull(stream);

            var bytes = stream.readAllBytes();
            imageString = Base64.getEncoder().encodeToString(bytes);
        }

        var params = new int[0];
        var result = labelDetector.ExecuteFromBase64(imageString, params);
        System.out.println(result);
    }

    @Test
    void itShouldThrowOnInvalidBase64() {
        var imageString = "invalid";
        var params = new int[0];

        assertThrows(Exception.class, () -> labelDetector.ExecuteFromBase64(imageString, params));
    }

    @Test
    void itShouldThrowOnMissingS3Object() {
        var imageUri = "missing.jpg";
        var params = new int[0];
        assertThrows(Exception.class, () -> labelDetector.Execute(imageUri, params));
    }
}