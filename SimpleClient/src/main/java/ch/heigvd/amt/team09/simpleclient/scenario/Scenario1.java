package ch.heigvd.amt.team09.simpleclient.scenario;

import org.junit.jupiter.api.function.ThrowingSupplier;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class Scenario1 extends Scenario {
    private static final Path IMAGE = Path.of("src", "main", "resources", "image.jpg");
    private static final String OBJECT_KEY = "1-some-object";
    private static final String OBJECT_RESULTS_KEY = "1-some-results";

    @Override
    protected String name() {
        return "Scenario 1 : tout est nouveau";
    }

    @Override
    protected String description() {
        return """
                Premier appel au service.
                -----
                -   Le bucket doit être créé
                -   L'image doit être uploadée
                -   Publication
                -   Analyse
                -   Livraison du résultat sur le bucket hébergeant l'image.
                """;
    }

    @Override
    protected void setup() {
        var deleteStatus = assertDoesNotThrow(this::deleteRoot);
        assertTrue(deleteStatus == 204 || deleteStatus == 404);
    }

    @Override
    protected void cleanup() {
        assertDoesNotThrow(() -> deleteObject(OBJECT_KEY));
        assertDoesNotThrow(() -> deleteObject(OBJECT_RESULTS_KEY));
    }

    @Override
    protected void run() {
        // # Upload image
        // given
        assertTrue(Files.exists(IMAGE));
        assertFalse(assertDoesNotThrow(() -> objectExists(OBJECT_KEY)));
        assertFalse(assertDoesNotThrow(() -> objectExists(OBJECT_RESULTS_KEY)));

        // when
        ThrowingSupplier<Boolean> uploadImage = () -> uploadFile(OBJECT_KEY, IMAGE);

        // then
        var imageUploaded = assertDoesNotThrow(uploadImage);
        assertTrue(imageUploaded);

        // # Publish image
        // given
        assertTrue(assertDoesNotThrow(() -> objectExists(OBJECT_KEY)));

        // when
        ThrowingSupplier<Optional<URL>> publish = () -> publishImage(OBJECT_KEY);

        // then
        var url = assertDoesNotThrow(publish).orElseGet(() -> fail("No URL returned"));

        // # Analyze image
        // given
        assertTrue(isUrlValid(url));

        // when
        ThrowingSupplier<Optional<Map<String, Object>>> analyze = () -> analyzeImage(url);

        // then
        var results = assertDoesNotThrow(analyze).orElseGet(() -> fail("No results returned"));
        var labels = assertDoesNotThrow(() -> (List<Map<String, Double>>) results.get("labels"));
        assertFalse(labels.isEmpty());

        // # Upload results
        // when
        ThrowingSupplier<Boolean> uploadResults = () -> uploadResults(OBJECT_RESULTS_KEY, labels);

        // then
        assertTrue(assertDoesNotThrow(uploadResults));
        assertTrue(assertDoesNotThrow(() -> objectExists(OBJECT_RESULTS_KEY)));
    }
}
