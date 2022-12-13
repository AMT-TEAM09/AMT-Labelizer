package ch.heigvd.amt.team09.dataobject.service.impl;

import ch.heigvd.amt.team09.dataobject.service.interfaces.DataObjectService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class AwsDataObjectServiceTest {
    private static final Path RESOURCE_PATH = Paths.get("src", "test", "resources");
    private static final Path IMAGE_FILE = RESOURCE_PATH.resolve("image.jpg");
    private static final String TEST_OBJECT_NAME = "test-object";
    private static final String TEST_FOLDER = "test-folder";
    private static AwsDataObjectService dataObjectService;
    private static byte[] imageContent;

    @BeforeAll
    static void setUp() {
        dataObjectService = new AwsDataObjectService();
        try {
            imageContent = Files.readAllBytes(IMAGE_FILE);
        } catch (IOException e) {
            fail(e);
        }
    }

    @AfterEach
    void tearDown() {
        if (dataObjectService.exists(TEST_OBJECT_NAME)) {
            assertDoesNotThrow(() -> dataObjectService.delete(TEST_OBJECT_NAME));
        }

        if (dataObjectService.exists(TEST_FOLDER)) {
            assertDoesNotThrow(() -> dataObjectService.delete(TEST_FOLDER, true));
        }
    }

    @Test
    void exists_rootObjectExists_exists() {
        // when
        var exists = dataObjectService.exists();

        // then
        assertTrue(exists);
    }

    @Test
    void exists_rootObjectNotExists_notExists() {
        // given
        var bucketName = "not-existing-bucket";
        var service = new AwsDataObjectService(bucketName);

        // when
        var exists = service.exists();

        // then
        assertFalse(exists);
    }

    @Test
    void exists_rootObjectAndObjectExist_exists() {
        // given
        assertTrue(dataObjectService.exists());
        assertFalse(dataObjectService.exists(TEST_OBJECT_NAME));
        assertDoesNotThrow(() -> dataObjectService.create(TEST_OBJECT_NAME, imageContent));

        // when
        var exists = dataObjectService.exists(TEST_OBJECT_NAME);

        // then
        assertTrue(exists);
    }

    @Test
    void exists_rootObjectExistsObjectNotExists_notExists() {
        // given
        assertTrue(dataObjectService.exists());
        assertFalse(dataObjectService.exists(TEST_OBJECT_NAME));

        // when
        var exists = dataObjectService.exists(TEST_OBJECT_NAME);

        // then
        assertFalse(exists);
    }

    @Test
    void create_rootObjectExistsNewObject_objectCreated() {
        // given
        assertTrue(dataObjectService.exists());
        assertFalse(dataObjectService.exists(TEST_OBJECT_NAME));

        // when
        assertDoesNotThrow(() -> dataObjectService.create(TEST_OBJECT_NAME, imageContent));

        // then
        assertTrue(dataObjectService.exists(TEST_OBJECT_NAME));
    }

    @Test
    void create_rootObjectExistsObjectAlreadyExists_exceptionThrown() {
        // given
        assertTrue(dataObjectService.exists());
        assertDoesNotThrow(() -> dataObjectService.create(TEST_OBJECT_NAME, imageContent));
        assertTrue(dataObjectService.exists(TEST_OBJECT_NAME));

        // when
        Executable func = () -> dataObjectService.create(TEST_OBJECT_NAME, imageContent);

        // then
        assertThrows(DataObjectService.ObjectAlreadyExistsException.class, func);
    }

    @Disabled("only on release")
    @Test
    void create_rootObjectNotExistsNewObject_objectCreated() {
        // given
        assertDoesNotThrow(() -> dataObjectService.delete());
        assertFalse(dataObjectService.exists());

        // when
        assertDoesNotThrow(() -> dataObjectService.create(TEST_OBJECT_NAME, imageContent));

        // then
        assertTrue(dataObjectService.exists());
        assertTrue(dataObjectService.exists(TEST_OBJECT_NAME));
    }

    @Test
    void get_objectExists_fileDownloaded() {
        // given
        assertDoesNotThrow(() -> dataObjectService.create(TEST_OBJECT_NAME, imageContent));
        assertTrue(dataObjectService.exists(TEST_OBJECT_NAME));

        // when
        var downloadedContent = assertDoesNotThrow(() -> {
            try (var downloaded = dataObjectService.get(TEST_OBJECT_NAME)) {
                return downloaded.readAllBytes();
            }
        });

        // then
        assertArrayEquals(imageContent, downloadedContent);
    }

    @Test
    void get_objectNotExists_exceptionThrown() {
        // given
        assertFalse(dataObjectService.exists(TEST_OBJECT_NAME));

        // when
        Executable func = () -> dataObjectService.get(TEST_OBJECT_NAME);

        // then
        assertThrows(DataObjectService.ObjectNotFoundException.class, func);
    }

    @Test
    void publish_objectExists_urlIsReachable() {
        // given
        assertDoesNotThrow(() -> dataObjectService.create(TEST_OBJECT_NAME, imageContent));
        assertTrue(dataObjectService.exists(TEST_OBJECT_NAME));

        // when
        var url = assertDoesNotThrow(() -> dataObjectService.publish(TEST_OBJECT_NAME));

        // then
        var responseCode = assertDoesNotThrow(() -> {
            var huc = (HttpURLConnection) url.openConnection();
            return huc.getResponseCode();
        });
        assertEquals(200, responseCode);
    }

    @Test
    void publish_objectNotExists_exceptionThrown() {
        // given
        assertTrue(dataObjectService.exists());
        assertFalse(dataObjectService.exists(TEST_OBJECT_NAME));

        // when
        Executable func = () -> dataObjectService.publish(TEST_OBJECT_NAME);

        // then
        assertThrows(DataObjectService.ObjectNotFoundException.class, func);
    }

    @Test
    void publish_negativeUrlDuration_exceptionThrown() {
        // given
        var urlDuration = Duration.ofSeconds(-1);
        assertDoesNotThrow(() -> dataObjectService.create(TEST_OBJECT_NAME, imageContent));
        assertTrue(dataObjectService.exists(TEST_OBJECT_NAME));

        // when
        Executable func = () -> dataObjectService.publish(TEST_OBJECT_NAME, urlDuration);

        // then
        assertThrows(IllegalArgumentException.class, func);
    }

    @Test
    void publish_zeroUrlDuration_errorThrown() {
        // given
        var urlDuration = Duration.ZERO;
        assertDoesNotThrow(() -> dataObjectService.create(TEST_OBJECT_NAME, imageContent));
        assertTrue(dataObjectService.exists(TEST_OBJECT_NAME));

        // when
        Executable func = () -> dataObjectService.publish(TEST_OBJECT_NAME, urlDuration);

        // then
        assertThrows(IllegalArgumentException.class, func);
    }

    @Test
    void delete_singleObjectExists_objectDeleted() {
        // given
        assertDoesNotThrow(() -> dataObjectService.create(TEST_OBJECT_NAME, imageContent));
        assertTrue(dataObjectService.exists(TEST_OBJECT_NAME));

        // when
        assertDoesNotThrow(() -> dataObjectService.delete(TEST_OBJECT_NAME, false));

        // then
        assertFalse(dataObjectService.exists(TEST_OBJECT_NAME));
    }

    @Test
    void delete_singleObjectNotExists_exceptionThrown() {
        // given
        assertTrue(dataObjectService.exists());
        assertFalse(dataObjectService.exists(TEST_OBJECT_NAME));

        // when
        Executable func = () -> dataObjectService.delete(TEST_OBJECT_NAME, false);

        // then
        assertThrows(DataObjectService.ObjectNotFoundException.class, func);
    }

    @Test
    void delete_folderObjectExistsWithoutRecursiveOption_exceptionThrown() {
        // given
        var testObjectName = TEST_FOLDER + "/" + TEST_OBJECT_NAME;
        var testObjectName2 = TEST_FOLDER + "/" + TEST_OBJECT_NAME + "2";
        assertDoesNotThrow(() -> dataObjectService.create(testObjectName, imageContent));
        assertDoesNotThrow(() -> dataObjectService.create(testObjectName2, imageContent));
        assertTrue(dataObjectService.exists(testObjectName));
        assertTrue(dataObjectService.exists(testObjectName2));
        assertTrue(dataObjectService.exists(TEST_FOLDER));

        // when
        Executable func = () -> dataObjectService.delete(TEST_FOLDER, false);

        // then
        assertThrows(DataObjectService.ObjectNotEmptyException.class, func);
        assertTrue(dataObjectService.exists(testObjectName));
        assertTrue(dataObjectService.exists(testObjectName2));
        assertTrue(dataObjectService.exists(TEST_FOLDER));
    }

    @Test
    void delete_folderObjectExistsWithRecursiveOption_folderDeleted() {
        // given
        var testObjectName = TEST_FOLDER + "/" + TEST_OBJECT_NAME;
        var testObjectName2 = TEST_FOLDER + "/" + TEST_OBJECT_NAME + "2";
        assertDoesNotThrow(() -> dataObjectService.create(testObjectName, imageContent));
        assertDoesNotThrow(() -> dataObjectService.create(testObjectName2, imageContent));
        assertTrue(dataObjectService.exists(testObjectName));
        assertTrue(dataObjectService.exists(testObjectName2));
        assertTrue(dataObjectService.exists(TEST_FOLDER));

        // when
        assertDoesNotThrow(() -> dataObjectService.delete(TEST_FOLDER, true));

        // then
        assertFalse(dataObjectService.exists(testObjectName));
        assertFalse(dataObjectService.exists(testObjectName2));
        assertFalse(dataObjectService.exists(TEST_FOLDER));
    }

    @Disabled("only on release")
    @Test
    void delete_rootObjectNotEmptyWithoutRecursiveOption_exceptionThrown() {
        // given
        var testObjectName = TEST_OBJECT_NAME;
        assertDoesNotThrow(() -> dataObjectService.create(testObjectName, imageContent));
        assertTrue(dataObjectService.exists(testObjectName));

        // when
        Executable func = () -> dataObjectService.delete(false);

        // then
        assertThrows(DataObjectService.ObjectNotEmptyException.class, func);
        assertTrue(dataObjectService.exists(testObjectName));
    }

    @Disabled("only on release")
    @Test
    void delete_rootObjectNotEmptyWithRecursiveOption_rootDeleted() {
        // given
        var testObjectName = TEST_OBJECT_NAME;
        assertDoesNotThrow(() -> dataObjectService.create(testObjectName, imageContent));
        assertTrue(dataObjectService.exists(testObjectName));

        // when
        assertDoesNotThrow(() -> dataObjectService.delete(true));

        // then
        assertFalse(dataObjectService.exists(testObjectName));
    }
}