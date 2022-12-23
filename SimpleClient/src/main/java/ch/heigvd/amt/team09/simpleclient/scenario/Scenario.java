package ch.heigvd.amt.team09.simpleclient.scenario;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mizosoft.methanol.Methanol;
import com.github.mizosoft.methanol.MultipartBodyPublisher;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.function.ThrowingSupplier;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public abstract class Scenario {
    private static final Path IMAGE = Path.of("src", "main", "resources", "image.jpg");
    private static final String OBJECT_KEY = "scenario-object";
    private static final String OBJECT_RESULTS_KEY = "scenario-results";

    private final Methanol client;
    private final String dataObjectURI;
    private final String analyzerURI;

    protected Scenario() {
        var dotenv = Dotenv.load();
        client = Methanol.newBuilder()
                .defaultHeader("Content-Type", "application/json")
                .build();

        this.dataObjectURI = Objects.requireNonNull(dotenv.get("DATA_OBJECT_URI"), "DATA_OBJECT_URI is not set");
        this.analyzerURI = Objects.requireNonNull(dotenv.get("ANALYZER_URI"), "ANALYZER_URI is not set");
    }

    protected static boolean isUrlValid(URL url) {
        try {
            var huc = (HttpURLConnection) url.openConnection();
            var responseCode = huc.getResponseCode();
            return responseCode == 200;
        } catch (IOException e) {
            return false;
        }
    }

    protected static Map<String, Object> parseJson(String json) throws JsonProcessingException {
        var mapper = new ObjectMapper();
        var typeRef = new TypeReference<HashMap<String, Object>>() {
        };
        return mapper.readValue(json, typeRef);
    }

    protected abstract void setup();

    public void start() {
        System.out.println("Running " + name() + ":");
        System.out.println(description());

        System.out.println("Setup...");
        setup();

        System.out.println("Running...");
        run();

        System.out.println("Cleanup...");
        cleanup();

        System.out.println("Scenario completed");
        System.out.println();
    }

    protected abstract String name();

    protected abstract String description();

    protected abstract void run();

    protected void cleanup() {
        assertDoesNotThrow(() -> deleteObjectRequest(OBJECT_KEY));
        assertDoesNotThrow(() -> deleteObjectRequest(OBJECT_RESULTS_KEY));
    }

    protected void uploadImage() {
        // given
        assertTrue(Files.exists(IMAGE));
        assertFalse(assertDoesNotThrow(() -> objectExistsRequest(OBJECT_KEY)));
        assertFalse(assertDoesNotThrow(() -> objectExistsRequest(OBJECT_RESULTS_KEY)));

        // when
        ThrowingSupplier<Boolean> uploadImage = () -> uploadRequest(OBJECT_KEY, IMAGE);

        // then
        var imageUploaded = assertDoesNotThrow(uploadImage);
        assertTrue(imageUploaded);
    }

    protected URL publishImage() {
        // given
        assertTrue(assertDoesNotThrow(() -> objectExistsRequest(OBJECT_KEY)));

        // when
        ThrowingSupplier<Optional<URL>> publish = () -> publishRequest(OBJECT_KEY);

        // then
        return assertDoesNotThrow(publish).orElseGet(() -> fail("No URL returned"));
    }

    protected List<Map<String, Double>> analyzeImage(URL url) {
        // given
        assertTrue(isUrlValid(url));

        // when
        ThrowingSupplier<Optional<Map<String, Object>>> analyze = () -> analyzeRequest(url);

        // then
        var results = assertDoesNotThrow(analyze).orElseGet(() -> fail("No results returned"));
        var labels = assertDoesNotThrow(() -> (List<Map<String, Double>>) results.get("labels"));
        assertFalse(labels.isEmpty());

        return labels;
    }

    protected void uploadResults(List<Map<String, Double>> labels) {
        // given
        assertFalse(assertDoesNotThrow(() -> objectExistsRequest(OBJECT_RESULTS_KEY)));

        // when
        ThrowingSupplier<Boolean> uploadResults = () -> uploadResultsRequest(OBJECT_RESULTS_KEY, labels);

        // then
        var resultsUploaded = assertDoesNotThrow(uploadResults);
        assertTrue(resultsUploaded);
    }

    private boolean objectExistsRequest(String objectName) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(dataObjectURI + "?objectName=" + objectName))
                .method("HEAD", HttpRequest.BodyPublishers.noBody())
                .build();

        return sendRequest(request, HttpResponse.BodyHandlers.discarding()).statusCode() == 200;
    }

    protected boolean createRootObject() {
        assertDoesNotThrow(this::uploadImage);
        return assertDoesNotThrow(() -> deleteObjectRequest(OBJECT_KEY));
    }

    protected int deleteRootRequest() throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(dataObjectURI + "?recursive=true"))
                .DELETE()
                .build();

        return sendRequest(request, HttpResponse.BodyHandlers.discarding()).statusCode();
    }

    private boolean deleteObjectRequest(String objectName) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(dataObjectURI + "?objectName=" + objectName))
                .DELETE()
                .build();

        return sendRequest(request, HttpResponse.BodyHandlers.discarding()).statusCode() == 204;
    }

    private boolean uploadResultsRequest(String objectName, List<Map<String, Double>> labels) throws IOException,
            InterruptedException {
        var tempFile = Files.createTempFile("results", ".json");
        try {
            Files.writeString(tempFile, toPrettyJson(labels));
        } catch (IOException e) {
            Files.delete(tempFile);
            throw e;
        }

        try {
            return uploadRequest(objectName, tempFile);
        } finally {
            Files.delete(tempFile);
        }
    }

    private boolean uploadRequest(String objectName, Path filePath) throws IOException, InterruptedException {
        var multipartBody = MultipartBodyPublisher.newBuilder()
                .filePart("file", filePath)
                .textPart("objectName", objectName)
                .build();

        var request = HttpRequest.newBuilder()
                .uri(URI.create(dataObjectURI))
                .POST(multipartBody)
                .build();

        return sendRequest(request, HttpResponse.BodyHandlers.ofString()).statusCode() == 200;
    }

    private Optional<URL> publishRequest(String key) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(dataObjectURI + "?objectName=" + key))
                .GET()
                .build();

        var response = sendRequest(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            return Optional.empty();
        }

        var data = parseJson(response.body());
        var url = new URL(data.get("url").toString());

        return Optional.of(url);
    }

    private Optional<Map<String, Object>> analyzeRequest(URL imageUrl) throws IOException, InterruptedException {
        var postData = """
                {
                    "source": "%s"
                }
                """.formatted(imageUrl);

        var request = HttpRequest.newBuilder()
                .uri(URI.create(analyzerURI))
                .POST(HttpRequest.BodyPublishers.ofString(postData))
                .build();

        var response = sendRequest(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            return Optional.empty();
        }

        var results = parseJson(response.body());

        return Optional.of(results);
    }

    private String toPrettyJson(Object object) throws JsonProcessingException {
        var mapper = new ObjectMapper();
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }

    private <T> Response<T> sendRequest(HttpRequest request, HttpResponse.BodyHandler<T> bodyHandler) throws IOException, InterruptedException {
        try {
            System.out.printf("%s -> %s%n", request.method(), request.uri());
            var response = client.send(request, bodyHandler);
            System.out.printf("%d -> %s%n", response.statusCode(), response.body());

            return new Response<>(response.statusCode(), response.body());
        } catch (ConnectException e) {
            System.err.println("Failed to connect to " + request.uri());
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    private record Response<T>(int statusCode, T body) {
    }
}
