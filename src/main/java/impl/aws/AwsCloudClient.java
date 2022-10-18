package impl.aws;

import interfaces.CloudClient;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;

public class AwsCloudClient implements CloudClient {
    private static final String BUCKET_NAME = "amt.team09.diduno.education";

    private AwsCloudClient() {
        var credentialsProvider = ProfileCredentialsProvider.create();
    }

    @Override
    public CloudClient getInstance() {
        return null;
    }
}
