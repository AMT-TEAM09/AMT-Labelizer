package impl.aws;

import interfaces.LabelDetector;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.Image;

import java.util.Base64;

public class AwsLabelDetector implements LabelDetector {
    private final RekognitionClient client;

    public AwsLabelDetector(ProfileCredentialsProvider credentialsProvider) {
        client = RekognitionClient.builder()
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Override
    public String Execute(String imageUri, int[] params) {
//
//        var request = DetectLabelsRequest.builder()
//                .image(image)
//                .build();
//
//        var response = client.detectLabels(request);
//        return response.labels().toString();
        return "test";
    }

    @Override
    public String ExecuteFromBase64(String base64, int[] params) {
        var decoded = Base64.getDecoder().decode(base64);
        var imageBytes = SdkBytes.fromByteArray(decoded);

        var image = Image.builder()
                .bytes(imageBytes)
                .build();

        var request = DetectLabelsRequest.builder()
                .image(image)
                .build();

        var response = client.detectLabels(request);
        return response.labels().toString();
    }
}
