package impl.aws;

import interfaces.CloudClient;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;

public class AwsCloudClient implements CloudClient {
    private static final String BUCKET_NAME = "amt.team09.diduno.education";
    private static final ProfileCredentialsProvider CREDENTIALS_PROVIDER = ProfileCredentialsProvider.create();

    private static AwsCloudClient instance;

    private AwsDataObjectHelper dataObjectHelper;
    private AwsLabelDetector labelDetector;

    private AwsCloudClient() {
        dataObjectHelper = new AwsDataObjectHelper(CREDENTIALS_PROVIDER, BUCKET_NAME);
        labelDetector = new AwsLabelDetector(CREDENTIALS_PROVIDER);
    }

    public static AwsCloudClient getInstance() {
        if (instance == null) {
            instance = new AwsCloudClient();
        }
        return instance;
    }

    @Override
    public AwsDataObjectHelper getDataObjectHelper() {
        return dataObjectHelper;
    }

    @Override
    public AwsLabelDetector getLabelDetector() {
        return labelDetector;
    }
}
