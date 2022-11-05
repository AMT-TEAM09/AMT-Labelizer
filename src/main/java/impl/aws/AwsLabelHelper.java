package impl.aws;

import interfaces.LabelHelper;
import models.Label;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsResponse;
import software.amazon.awssdk.services.rekognition.model.Image;

import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.function.Consumer;

public class AwsLabelHelper implements LabelHelper {
    private static final Consumer<LabelOptions.Builder> NO_OPTIONS = b -> {
    };
    private final RekognitionClient client;

    public AwsLabelHelper(ProfileCredentialsProvider credentialsProvider, Region region) {
        client = RekognitionClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }

    @Override
    public Label[] execute(String imageUrl) throws IOException {
        return execute(imageUrl, NO_OPTIONS);
    }

    @Override
    public Label[] execute(String imageUrl, Consumer<LabelOptions.Builder> options) throws IOException {
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
    public Label[] executeFromBase64(String base64) {
        return executeFromBase64(base64, NO_OPTIONS);
    }

    @Override
    public Label[] executeFromBase64(String base64, Consumer<LabelOptions.Builder> options) {
        var decoded = Base64.getDecoder().decode(base64);
        var imageBytes = SdkBytes.fromByteArray(decoded);

        var image = Image.builder()
                .bytes(imageBytes)
                .build();

        return executeRequest(image, options);
    }

    private Label[] executeRequest(Image image, Consumer<LabelOptions.Builder> optionsOperations) {
        var requestBuilder = DetectLabelsRequest.builder()
                .image(image);

        var builder = LabelOptions.builder();
        optionsOperations.accept(builder);
        var options = builder.build();

        options.maxLabels().ifPresent(requestBuilder::maxLabels);
        options.minConfidence().ifPresent(requestBuilder::minConfidence);

        var response = client.detectLabels(requestBuilder.build());

        return responseToArray(response);
    }

    private Label[] responseToArray(DetectLabelsResponse response) {
        var labels = response.labels();

        return labels.stream()
                .map(l -> new Label(l.name(), l.confidence()))
                .toArray(Label[]::new);
    }
}
