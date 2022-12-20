package ch.heigvd.amt.team09.simpleclient.scenario;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mizosoft.methanol.Methanol;
import com.github.mizosoft.methanol.MultipartBodyPublisher;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class Scenario {
    private static final String DATA_OBJECT_URI = "http://localhost:8080/data-object/v1/objects";
    private static final String DATA_OBJECT_UPLOAD_URI = DATA_OBJECT_URI;
    private static final String DATA_OBJECT_PUBLISH_URI = DATA_OBJECT_URI;
    private static final String DATA_OBJECT_DELETE_ROOT_URI = DATA_OBJECT_URI;
    private static final String DATA_OBJECT_DELETE_OBJECT_URI = DATA_OBJECT_URI;
    private static final String ANALYZER_URI = "http://localhost:8081/analyzer/v1/url";

    private final Methanol client;

    protected Scenario() {
        client = Methanol.newBuilder()
                .defaultHeader("Content-Type", "application/json")
                .build();
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

    public void start() {
        System.out.println("Running " + name() + ":");
        System.out.println(description());

        System.out.println("Setup...");
        setup();

        run();

        System.out.println("Cleanup...");
        cleanup();

        System.out.println("Scenario completed");
        System.out.println();
    }

    protected abstract String name();

    protected abstract String description();

    protected abstract void run();

    protected void setup() {
    }

    protected void cleanup() {
    }

    protected boolean objectExists(String objectName) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(DATA_OBJECT_PUBLISH_URI + "?objectName=" + objectName))
                .method("HEAD", HttpRequest.BodyPublishers.noBody())
                .build();

        return sendRequest(request, HttpResponse.BodyHandlers.discarding()).statusCode() == 200;
    }

    protected boolean deleteObject(String objectName) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(DATA_OBJECT_DELETE_OBJECT_URI + "?objectName=" + objectName))
                .DELETE()
                .build();

        return sendRequest(request, HttpResponse.BodyHandlers.discarding()).statusCode() == 204;
    }

    protected int deleteRoot() throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(DATA_OBJECT_DELETE_ROOT_URI + "?recursive=true"))
                .DELETE()
                .build();

        return sendRequest(request, HttpResponse.BodyHandlers.discarding()).statusCode();
    }

    protected boolean uploadResults(String objectName, List<Map<String, Double>> labels) throws IOException,
            InterruptedException {
        var tempFile = Files.createTempFile("results", ".json");
        try {
            Files.writeString(tempFile, toPrettyJson(labels));
        } catch (IOException e) {
            Files.delete(tempFile);
            throw e;
        }

        try {
            return uploadFile(objectName, tempFile);
        } finally {
            Files.delete(tempFile);
        }
    }

    protected boolean uploadFile(String objectName, Path filePath) throws IOException, InterruptedException {
        var multipartBody = MultipartBodyPublisher.newBuilder()
                .filePart("file", filePath)
                .textPart("objectName", objectName)
                .build();

        var request = HttpRequest.newBuilder()
                .uri(URI.create(DATA_OBJECT_UPLOAD_URI))
                .POST(multipartBody)
                .build();

        return sendRequest(request, HttpResponse.BodyHandlers.ofString()).statusCode() == 200;
    }

    protected Optional<URL> publishImage(String key) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(DATA_OBJECT_PUBLISH_URI + "?objectName=" + key))
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

    protected Optional<Map<String, Object>> analyzeImage(URL imageUrl) throws IOException, InterruptedException {
        var postData = """
                {
                    "source": "%s"
                }
                """.formatted(imageUrl);

        var request = HttpRequest.newBuilder()
                .uri(URI.create(ANALYZER_URI))
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
