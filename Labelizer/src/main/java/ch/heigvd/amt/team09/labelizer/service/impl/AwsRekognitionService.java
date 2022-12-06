package ch.heigvd.amt.team09.labelizer.service.impl;

import ch.heigvd.amt.team09.labelizer.dto.Label;
import ch.heigvd.amt.team09.labelizer.service.interfaces.AnalyzerService;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsResponse;
import software.amazon.awssdk.services.rekognition.model.Image;

import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.Objects;
import java.util.function.Consumer;

@Service
public class AwsRekognitionService implements AnalyzerService {
    public static final Consumer<AnalyzerService.Options.Builder> NO_OPTIONS = b -> {
    };
    public static final int DEFAULT_MAX_LABELS = 10;
    public static final float DEFAULT_MIN_CONFIDENCE = 90.0f;
    private static final Logger LOG = LoggerFactory.getLogger(AwsRekognitionService.class.getName());
    private final RekognitionClient client;

    public AwsRekognitionService() {
        var dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        var region = Objects.requireNonNull(dotenv.get("AWS_REGION"), "AWS_REGION is not set");

        client = RekognitionClient.builder()
                .credentialsProvider(getCredentialsProvider(dotenv))
                .region(Region.of(region))
                .build();
    }

    private static AwsCredentialsProvider getCredentialsProvider(Dotenv dotenv) {
        var accessKey = Objects.requireNonNull(dotenv.get("AWS_ACCESS_KEY_ID"), "AWS_ACCESS_KEY_ID is not set");
        var secretKey = Objects.requireNonNull(dotenv.get("AWS_SECRET_ACCESS_KEY"), "AWS_SECRET_ACCESS_KEY is not set");

        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                        accessKey,
                        secretKey
                )
        );
    }

    @Override
    public Label[] execute(String imageUrl, Consumer<AnalyzerService.Options.Builder> options) throws IOException {
        var url = new URL(imageUrl);
        byte[] imageBytes;

        try (var stream = url.openStream()) {
            imageBytes = stream.readAllBytes();
        }

        var image = Image.builder()
                .bytes(SdkBytes.fromByteArray(imageBytes))
                .build();

        return executeRequest(image, options);
    }

    @Override
    public Label[] executeFromBase64(String base64, Consumer<AnalyzerService.Options.Builder> options) {
        var decoded = Base64.getDecoder().decode(base64);
        var imageBytes = SdkBytes.fromByteArray(decoded);

        var image = Image.builder()
                .bytes(imageBytes)
                .build();

        return executeRequest(image, options);
    }

    private Label[] executeRequest(Image image, Consumer<AnalyzerService.Options.Builder> optionsOperations) {
        var requestBuilder = DetectLabelsRequest.builder()
                .image(image);

        var builder = AnalyzerService.Options.builder();
        optionsOperations.accept(builder);
        var options = builder.build();

        requestBuilder.minConfidence(options.minConfidence().orElse(DEFAULT_MIN_CONFIDENCE));
        requestBuilder.maxLabels(options.maxLabels().orElse(DEFAULT_MAX_LABELS));

        LOG.info("Sending request to AWS Rekognition");
        var start = System.currentTimeMillis();

        var response = client.detectLabels(requestBuilder.build());

        var elapsed = System.currentTimeMillis() - start;
        LOG.info("Request completed in {}ms", elapsed);

        return responseToArray(response);
    }

    private Label[] responseToArray(DetectLabelsResponse response) {
        var labels = response.labels();

        return labels.stream()
                .map(l -> new Label(l.name(), l.confidence()))
                .toArray(Label[]::new);
    }
}
