package impl.aws;

import interfaces.DataObjectHelper;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

public class AwsDataObjectHelper implements DataObjectHelper {
    private final String bucketName;
    private final S3Client client;

    public AwsDataObjectHelper(ProfileCredentialsProvider credentialsProvider, String bucketName) {
        client = S3Client.builder()
                .credentialsProvider(credentialsProvider)
                .build();
        this.bucketName = bucketName;
    }

    @Override
    public void Create(String objectName) {
        var request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectName)
                .build();
        client.putObject(request, RequestBody.empty());
    }
}
