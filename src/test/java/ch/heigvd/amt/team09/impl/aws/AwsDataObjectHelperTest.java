package ch.heigvd.amt.team09.impl.aws;

import ch.heigvd.amt.team09.interfaces.DataObjectHelper;
import ch.heigvd.amt.team09.util.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class AwsDataObjectHelperTest {
    private static final Path RESOURCE_PATH = Paths.get("src", "test", "resources");
    private static final Path IMAGE_FILE = RESOURCE_PATH.resolve("image.jpg");
    private static final String TEST_OBJECT_NAME = "test-object";
    private static AwsDataObjectHelper objectHelper;

    @BeforeAll
    static void setUp() {
        var regionName = Configuration.get("AWS_REGION");
        var bucketName = Configuration.get("AWS_BUCKET_NAME");
        var credentials = Configuration.getAwsCredentials();

        objectHelper = new AwsDataObjectHelper(bucketName, regionName, credentials);
    }

    @AfterEach
    void tearDown() {
        if (objectHelper.exists(TEST_OBJECT_NAME)) {
            objectHelper.delete(TEST_OBJECT_NAME);
        }
    }

    @Test
    void create_bucketExists_success() {
        // given
        assertTrue(objectHelper.exists());
        assertFalse(objectHelper.exists(TEST_OBJECT_NAME));

        // when
        assertDoesNotThrow(() -> objectHelper.create(TEST_OBJECT_NAME, IMAGE_FILE));

        // then
        assertTrue(objectHelper.exists(TEST_OBJECT_NAME));
    }

    @Test
    void create_fileNameDoesNotExist_success() {
        // given
        var invalidImagePath = Path.of(IMAGE_FILE + "1");
        assertFalse(objectHelper.exists(TEST_OBJECT_NAME));
        assertFalse(Files.exists(invalidImagePath));

        // then
        assertThrows(NoSuchFileException.class, () -> objectHelper.create(TEST_OBJECT_NAME, invalidImagePath));
    }

    //TODO REVIEW Try to avoid as much as possible exception in test case
    @Test
    void get_nominalCase_success() {
        // given
        String fileContent = assertDoesNotThrow(() -> {
            try (var stream = Files.newInputStream(IMAGE_FILE)) {
                return new String(stream.readAllBytes());
            }
        });
        assertDoesNotThrow(() -> objectHelper.create(TEST_OBJECT_NAME, IMAGE_FILE));
        assertTrue(objectHelper.exists(TEST_OBJECT_NAME));

        // when
        String downloadedContent = assertDoesNotThrow(() -> {
            try (var downloaded = objectHelper.get(TEST_OBJECT_NAME)) {
                return new String(downloaded.readAllBytes());
            }
        });

        // then
        assertEquals(fileContent, downloadedContent);
    }

    @Test
    void exists_objectNominalCase_success() {
        // given
        assertFalse(objectHelper.exists(TEST_OBJECT_NAME));
        assertDoesNotThrow(() -> objectHelper.create(TEST_OBJECT_NAME, IMAGE_FILE));

        // when
        var exists = objectHelper.exists(TEST_OBJECT_NAME);

        // then
        assertTrue(exists);
    }

    @Test
    void exists_bucketNotExists_success() {
        // given
        var bucketName = "not-existing-bucket";
        var regionName = Configuration.get("AWS_REGION");
        var credentials = Configuration.getAwsCredentials();
        var objectHelper = new AwsDataObjectHelper(bucketName, regionName, credentials);

        // when
        var exists = objectHelper.exists();

        // then
        assertFalse(exists);
    }

    @Test
    void exists_objectNotExists_success() {
        // given
        assertFalse(objectHelper.exists(TEST_OBJECT_NAME));

        // when
        var exists = objectHelper.exists(TEST_OBJECT_NAME);

        // then
        assertFalse(exists);
    }

    @Test
    void delete_objectNotExists_success() {
        // given
        assertTrue(objectHelper.exists());
        assertFalse(objectHelper.exists(TEST_OBJECT_NAME));

        // when
        objectHelper.delete(TEST_OBJECT_NAME);

        // then
        assertFalse(objectHelper.exists(TEST_OBJECT_NAME));
    }

    @Test
    void delete_objectExists_success() {
        // given
        assertDoesNotThrow(() -> objectHelper.create(TEST_OBJECT_NAME, IMAGE_FILE));
        assertTrue(objectHelper.exists(TEST_OBJECT_NAME));

        // when
        objectHelper.delete(TEST_OBJECT_NAME);

        // then
        assertFalse(objectHelper.exists(TEST_OBJECT_NAME));
    }

    @Test
    void publish_objectExists_success() {
        // given
        assertDoesNotThrow(() -> objectHelper.create(TEST_OBJECT_NAME, IMAGE_FILE));
        assertTrue(objectHelper.exists(TEST_OBJECT_NAME));

        // when
        var url = assertDoesNotThrow(() -> (objectHelper.publish(TEST_OBJECT_NAME)));

        // then
        var responseCode = assertDoesNotThrow(() -> {
            var huc = (HttpURLConnection) url.openConnection();
            return huc.getResponseCode();
        });


        assertEquals(200, responseCode);
    }

    @Test
    void publish_objectNotExists_success() {
        // given
        assertTrue(objectHelper.exists());
        assertFalse(objectHelper.exists(TEST_OBJECT_NAME + "1"));

        // then
        assertThrows(DataObjectHelper.NoSuchObjectException.class, () -> objectHelper.publish(TEST_OBJECT_NAME));
    }
}