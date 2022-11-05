package impl.aws;

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
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AwsLabelHelperTest {
    private static final Path RESOURCE_PATH = Paths.get("src", "test", "resources");
    private static final Path IMAGE_FILE = RESOURCE_PATH.resolve("image.jpg");
    private AwsLabelHelper labelHelper;

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
    void execute_fromValidUrl_success() throws IOException {
        // given
        var imageUrl = new URL("https://upload.wikimedia.org/wikipedia/commons/6/6b/American_Beaver.jpg");
        var params = new int[]{};

        var huc = (HttpURLConnection) imageUrl.openConnection();
        var responseCode = huc.getResponseCode();
        assertEquals(200, responseCode);

        // when
        var labels = labelHelper.Execute(imageUrl.toString(), params);

        // then
        assertEquals("Beaver", labels);
    }

    @Test
    void execute_fromValidBase64_success() throws IOException {
        // given
        var fileBytes = Files.readAllBytes(IMAGE_FILE);
        String imageString = Base64.getEncoder().encodeToString(fileBytes);
        var params = new int[]{};

        // when
        var labels = labelHelper.ExecuteFromBase64(imageString, params);

        // then
        assertEquals("Beaver", labels);
    }

    @Test
    void execute_fromInvalidBase64_errorThrown() {
        // given
        var imageString = "invalid";
        var params = new int[]{};

        // then
        assertThrows(InvalidImageFormatException.class, () -> labelHelper.ExecuteFromBase64(imageString, params));
    }
}