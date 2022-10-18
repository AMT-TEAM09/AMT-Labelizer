package impl.aws;

import interfaces.DataObjectHelper;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;

public class AwsDataObjectHelper implements DataObjectHelper {
    private S3Client client;

    public AwsDataObjectHelper(ProfileCredentialsProvider credentialsProvider, String bucketName) {
        client = S3Client.builder()
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Override
    public void Create(String objectName) {
//        client.getB;
    }
}
