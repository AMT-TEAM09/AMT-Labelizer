package impl.aws;

import interfaces.Bucket;
import interfaces.BucketManager;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.services.s3.S3Client;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class AwsBucketManager extends BucketManager {
    private final S3Client client;

    public AwsBucketManager() {
        ProfileCredentialsProvider credentialsProvider = ProfileCredentialsProvider.create();
        client = S3Client.builder()
                .credentialsProvider(credentialsProvider)
                .build();
    }

    @Override
    public List<Bucket> getBuckets() {
        return client.listBuckets().buckets().stream()
                .map(AwsBucket::new)
                .collect(toList());
    }

    @Override
    public void close() {
        client.close();
    }
}
