package impl.aws;

import interfaces.CloudClient;
import io.github.cdimascio.dotenv.Dotenv;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;

public class AwsCloudClient implements CloudClient {
    private static String bucketName;
    private static ProfileCredentialsProvider credentialsProvider;
    private static AwsCloudClient instance;

    private AwsDataObjectHelper dataObjectHelper;
    private AwsLabelDetector labelDetector;

    private AwsCloudClient() {
        Dotenv dotenv = Dotenv.configure().load();

        bucketName = dotenv.get("AWS_BUCKET_NAME");

        var profile = dotenv.get("AWS_PROFILE");
        credentialsProvider = ProfileCredentialsProvider.create(profile);
    }

    public static AwsCloudClient getInstance() {
        if (instance == null) {
            instance = new AwsCloudClient();
        }

        return instance;
    }

    private static void throwIfMissingCredentials() {
        if (credentialsProvider == null) {
            throw new RuntimeException("Credentials provider not set");
        }
    }

    private static void throwIfMissingBucketName() {
        if (bucketName == null) {
            throw new RuntimeException("Bucket name not set");
        }
    }

    @Override
    public AwsDataObjectHelper getDataObjectHelper() {
        if (dataObjectHelper != null) {
            return dataObjectHelper;
        }

        throwIfMissingBucketName();
        throwIfMissingCredentials();

        return dataObjectHelper = new AwsDataObjectHelper(credentialsProvider, bucketName);
    }

    @Override
    public AwsLabelDetector getLabelDetector() {
        if (labelDetector != null) {
            return labelDetector;
        }

        throwIfMissingCredentials();

        return labelDetector = new AwsLabelDetector(credentialsProvider);
    }
}
