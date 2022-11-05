package impl.aws;

import interfaces.LabelHelper;
import models.Label;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsResponse;

import java.io.IOException;
import java.net.URL;
import java.util.Base64;

public class AwsLabelHelper implements LabelHelper {
    private final RekognitionClient client;

    public AwsLabelHelper(ProfileCredentialsProvider credentialsProvider, Region region) {
        client = RekognitionClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }

    @Override
    public Label[] execute(String imageUrl, int[] params) throws IOException {
        var url = new URL(imageUrl);
        byte[] imageBytes;

        try (var stream = url.openStream()) {
            imageBytes = stream.readAllBytes();
        }

        var request = DetectLabelsRequest.builder()
                .image(b -> b.bytes(SdkBytes.fromByteArray(imageBytes)))
                .build();

        var response = executeRequest(request);

        return responseToArray(response);
    }

    @Override
    public Label[] executeFromBase64(String base64, int[] params) {
        var decoded = Base64.getDecoder().decode(base64);
        var imageBytes = SdkBytes.fromByteArray(decoded);

        var request = DetectLabelsRequest.builder()
                .image(b -> b.bytes(imageBytes))
                .build();

        var response = executeRequest(request);
        return responseToArray(response);
    }

    private DetectLabelsResponse executeRequest(DetectLabelsRequest request) {
        return client.detectLabels(request);
    }

    private Label[] responseToArray(DetectLabelsResponse response) {
        var labels = response.labels();

        return labels.stream()
                .map(l -> new Label(l.name(), l.confidence()))
                .toArray(Label[]::new);
    }
}
