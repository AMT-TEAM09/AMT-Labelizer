package ch.heigvd.amt.team09.impl.aws;

import ch.heigvd.amt.team09.interfaces.LabelHelper;
import ch.heigvd.amt.team09.models.Label;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
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
import java.util.logging.Level;
import java.util.logging.Logger;

public class AwsLabelHelper implements LabelHelper {
    public static final Consumer<LabelOptions.Builder> NO_OPTIONS = b -> {
    };
    private static final Logger LOG = Logger.getLogger(AwsLabelHelper.class.getName());
    private final RekognitionClient client;

    public AwsLabelHelper(AwsCredentialsProvider credentialsProvider, Region region) {
        client = RekognitionClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
    }

    @Override
    public Label[] execute(String imageUrl, Consumer<LabelOptions.Builder> options) throws IOException {
        var url = new URL(imageUrl);
        byte[] imageBytes;

        // TODO on attendait que vous utilisiez la fonctionalité du AWS SDK permettant
        // d'aller lire l'image directement dans le S3 sans la stream.
        try (var stream = url.openStream()) {
            imageBytes = stream.readAllBytes();
        }

        var image = Image.builder()
                .bytes(SdkBytes.fromByteArray(imageBytes))
                .build();

        return executeRequest(image, options);
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

        LOG.info("Sending request to AWS Rekognition");
        var start = System.currentTimeMillis();

        var response = client.detectLabels(requestBuilder.build());

        var elapsed = System.currentTimeMillis() - start;
        LOG.log(Level.INFO, "Request completed in {0}ms", elapsed);

        return responseToArray(response);
    }

    private Label[] responseToArray(DetectLabelsResponse response) {
        var labels = response.labels();

        return labels.stream()
                .map(l -> new Label(l.name(), l.confidence()))
                .toArray(Label[]::new);
    }
}
