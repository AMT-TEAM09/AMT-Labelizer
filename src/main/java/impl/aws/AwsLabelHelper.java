package impl.aws;

import interfaces.LabelHelper;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.Label;

import java.io.IOException;
import java.net.URL;
import java.util.Base64;
import java.util.List;

public class AwsLabelHelper implements LabelHelper {
    private final RekognitionClient client;

    public AwsLabelHelper(ProfileCredentialsProvider credentialsProvider, Region region) {
        client = RekognitionClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }

    @Override
    public String Execute(String imageUrl, int[] params) throws IOException {
        var url = new URL(imageUrl);
        byte[] imageBytes;

        try (var stream = url.openStream()) {
            imageBytes = stream.readAllBytes();
        }

        var request = DetectLabelsRequest.builder()
                .image(b -> b.bytes(SdkBytes.fromByteArray(imageBytes)))
                .build();

        var response = client.detectLabels(request);

        return labelsToJson(response.labels());
    }

    @Override
    public String ExecuteFromBase64(String base64, int[] params) {
        var decoded = Base64.getDecoder().decode(base64);
        var imageBytes = SdkBytes.fromByteArray(decoded);

        var request = DetectLabelsRequest.builder()
                .image(b -> b.bytes(imageBytes))
                .build();

        var response = client.detectLabels(request);
        return labelsToJson(response.labels());
    }

    private String labelsToJson(List<Label> labels) {
        return labels.stream()
                .map(label -> String.format("{\"name\": \"%s\", \"confidence\": %f}", label.name(), label.confidence()))
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }
}
