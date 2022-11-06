package ch.heigvd.amt.team09.impl.aws;

import ch.heigvd.amt.team09.interfaces.CloudClient;
import ch.heigvd.amt.team09.interfaces.LabelHelper.LabelOptions;
import ch.heigvd.amt.team09.util.Configuration;
import ch.heigvd.amt.team09.util.FilesHelper;
import ch.heigvd.amt.team09.util.JsonHelper;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;

import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Consumer;

public class AwsCloudClient implements CloudClient {
    private static AwsCloudClient instance;
    private final AwsDataObjectHelper dataObjectHelper;
    private final AwsLabelHelper labelDetector;

    private AwsCloudClient() {
        String bucketName = Configuration.get("AWS_BUCKET_NAME");

        var profile = Configuration.get("AWS_PROFILE");
        ProfileCredentialsProvider credentialsProvider = ProfileCredentialsProvider.create(profile);

        Region region = Region.of(Configuration.get("AWS_REGION"));

        dataObjectHelper = new AwsDataObjectHelper(credentialsProvider, bucketName, region);
        labelDetector = new AwsLabelHelper(credentialsProvider, region);
    }

    public static AwsCloudClient getInstance() {
        if (instance == null) {
            instance = new AwsCloudClient();
        }

        return instance;
    }

    @Override
    public String analyzeFromBase64(String base64, Consumer<LabelOptions.Builder> options, String remoteFileName) throws IOException {
        var labels = labelDetector.executeFromBase64(base64, options);

        var json = JsonHelper.toJson(labels);

        if (remoteFileName != null) {
            uploadBase64(remoteFileName, base64);
            uploadJson(remoteFileName, json);
        }

        return json;
    }

    @Override
    public String analyzeFromBase64(String base64, Consumer<LabelOptions.Builder> options) throws IOException {
        return analyzeFromBase64(base64, options, null);
    }

    @Override
    public String analyzeFromUrl(String url, Consumer<LabelOptions.Builder> options, String remoteFileName) throws IOException {
        var labels = labelDetector.execute(url, options);

        var json = JsonHelper.toJson(labels);

        if (remoteFileName != null) {
            uploadJson(remoteFileName, json);
        }

        return json;
    }

    @Override
    public String analyzeFromUrl(String url, Consumer<LabelOptions.Builder> options) throws IOException {
        return analyzeFromUrl(url, options, null);
    }

    @Override
    public String analyzeFromObject(String objectName, Consumer<LabelOptions.Builder> options, String remoteFileName) throws IOException {
        if (!dataObjectHelper.exists(objectName)) {
            return String.format("{\"error\": \"Object %s does not exist\"}", objectName);
        }

        var url = dataObjectHelper.publish(objectName);

        return analyzeFromUrl(url.toString(), options, remoteFileName);
    }

    @Override
    public String analyzeFromObject(String objectName, Consumer<LabelOptions.Builder> options) throws IOException {
        return analyzeFromObject(objectName, options, null);
    }

    private void uploadJson(String remoteFileName, String json) throws IOException {
        var jsonPath = FilesHelper.storeToFile(remoteFileName, json);
        dataObjectHelper.create(remoteFileName + ".json", jsonPath);
    }

    private void uploadBase64(String remoteFileName, String base64) throws IOException {
        var imagePath = FilesHelper.storeBase64ToFile(remoteFileName, base64);
        dataObjectHelper.create(remoteFileName, imagePath);
        Files.delete(imagePath);
    }
}
