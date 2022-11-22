package ch.heigvd.amt.team09.impl.aws;

import ch.heigvd.amt.team09.interfaces.DataObjectHelper;
import ch.heigvd.amt.team09.util.Configuration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
//TODO REVIEW Remove all AWS dependencies from your test class
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;


class AwsDataObjectHelperTest {
    private static final Path RESOURCE_PATH = Paths.get("src", "test", "resources");
    private static final Path IMAGE_FILE = RESOURCE_PATH.resolve("image.jpg");
    private static final String TEST_OBJECT_NAME = "test-object";
    private Region region;
    private AwsCredentialsProvider credentialsProvider;
    private AwsDataObjectHelper objectHelper;

    //TODO REVIEW Before each, all or before class ?
    @BeforeEach
    void setUp() {
        var bucketName = Configuration.get("AWS_BUCKET_NAME");

        region = Region.of(Configuration.get("AWS_REGION"));
        credentialsProvider = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                        Configuration.get("AWS_ACCESS_KEY_ID"),
                        Configuration.get("AWS_SECRET_ACCESS_KEY")
                )
        );

        objectHelper = new AwsDataObjectHelper(credentialsProvider, bucketName, region);
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
        objectHelper.create(TEST_OBJECT_NAME, IMAGE_FILE);

        // then
        assertTrue(objectHelper.exists(TEST_OBJECT_NAME));
    }

    @Test
    void create_fileNameDoesNotExist_success() {
        // given
        assertFalse(objectHelper.exists(TEST_OBJECT_NAME));

        // then
        assertThrows(UncheckedIOException.class, () -> objectHelper.create(TEST_OBJECT_NAME, Path.of(IMAGE_FILE + "1")));
    }

    //TODO REVIEW Try to avoid as much as possible exception in test case
    @Test
    void get_nominalCase_success() throws IOException {
        // given
        String fileContent;
        try (var stream = Files.newInputStream(IMAGE_FILE)) {
            fileContent = new String(stream.readAllBytes());
        }
        objectHelper.create(TEST_OBJECT_NAME, IMAGE_FILE);
        assertTrue(objectHelper.exists(TEST_OBJECT_NAME));

        // when
        String downloadedContent;
        try (var downloaded = objectHelper.get(TEST_OBJECT_NAME)) {
            downloadedContent = new String(downloaded.readAllBytes());
        }

        // then
        assertEquals(fileContent, downloadedContent);
    }

    @Test
    void exists_objectNominalCase_success() {
        // given
        assertFalse(objectHelper.exists(TEST_OBJECT_NAME));
        objectHelper.create(TEST_OBJECT_NAME, IMAGE_FILE);

        // when
        var exists = objectHelper.exists(TEST_OBJECT_NAME);

        // then
        assertTrue(exists);
    }

    @Test
    void exists_bucketNotExists_success() {
        // given
        var bucketName = "not-existing-bucket";
        var objectHelper = new AwsDataObjectHelper(credentialsProvider, bucketName, region);

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
        objectHelper.create(TEST_OBJECT_NAME, IMAGE_FILE);
        assertTrue(objectHelper.exists(TEST_OBJECT_NAME));

        // when
        objectHelper.delete(TEST_OBJECT_NAME);

        // then
        assertFalse(objectHelper.exists(TEST_OBJECT_NAME));
    }

    @Test
    void publish_objectExists_success() throws IOException {
        // given
        objectHelper.create(TEST_OBJECT_NAME, IMAGE_FILE);
        assertTrue(objectHelper.exists(TEST_OBJECT_NAME));

        // when
        var url = assertDoesNotThrow(() -> (objectHelper.publish(TEST_OBJECT_NAME)));

        // then
        var huc = (HttpURLConnection) url.openConnection();
        var responseCode = huc.getResponseCode();

        assertEquals(200, responseCode);
    }

    @Test
    void publish_objectNotExists_success(){
        // given
        assertTrue(objectHelper.exists());
        assertFalse(objectHelper.exists(TEST_OBJECT_NAME + "1"));

        // then
        assertThrows(DataObjectHelper.NoSuchObjectException.class, () -> objectHelper.publish(TEST_OBJECT_NAME));
    }
}