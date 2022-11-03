package impl.aws;

import interfaces.CloudClient;
import io.github.cdimascio.dotenv.Dotenv;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;

public class AwsCloudClient implements CloudClient {
    private static AwsCloudClient instance;
    private final String bucketName;
    private final ProfileCredentialsProvider credentialsProvider;
    private final Region region;
    private AwsDataObjectHelper dataObjectHelper;
    private AwsLabelDetector labelDetector;

    private AwsCloudClient() {
        Dotenv dotenv = Dotenv.configure().load();

        bucketName = dotenv.get("AWS_BUCKET_NAME");

        var profile = dotenv.get("AWS_PROFILE");
        credentialsProvider = ProfileCredentialsProvider.create(profile);

        region = Region.of(dotenv.get("AWS_REGION"));
    }

    public static AwsCloudClient getInstance() {
        if (instance == null) {
            instance = new AwsCloudClient();
        }

        return instance;
    }

    @Override
    public AwsDataObjectHelper getDataObjectHelper() {
        if (dataObjectHelper != null) {
            return dataObjectHelper;
        }

        return dataObjectHelper = new AwsDataObjectHelper(credentialsProvider, bucketName, region);
    }

    @Override
    public AwsLabelDetector getLabelDetector() {
        if (labelDetector != null) {
            return labelDetector;
        }

        return labelDetector = new AwsLabelDetector(credentialsProvider, region);
    }
}
