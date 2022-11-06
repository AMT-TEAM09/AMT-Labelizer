package ch.heigvd.amt.team09.impl.aws;

import ch.heigvd.amt.team09.interfaces.DataObjectHelper;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.time.Duration;

public class AwsDataObjectHelper implements DataObjectHelper {
    private final String bucketName;
    private final S3Client client;
    private final S3Presigner presigner;

    public AwsDataObjectHelper(ProfileCredentialsProvider credentialsProvider, String bucketName, Region region) {
        client = S3Client.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
        presigner = S3Presigner.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();
        this.bucketName = bucketName;
    }

    @Override
    public void create() {
        createBucket();
    }

    @Override
    public void create(String objectName, Path filePath) {
        if (!bucketExists()) {
            createBucket();
        }

        var request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectName)
                .build();
        client.putObject(request, RequestBody.fromFile(filePath));
    }

    @Override
    public boolean exists() {
        return bucketExists();
    }

    @Override
    public boolean exists(String objectName) {
        return objectExists(objectName);
    }

    @Override
    public void delete() {
        if (!bucketExists())
            return;

        deleteBucket();
    }

    @Override
    public void delete(String objectName) {
        if (!objectExists(objectName))
            return;

        var request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(objectName)
                .build();
        client.deleteObject(request);
    }

    @Override
    public URL publish(String objectName) {
        if (!objectExists(objectName))
            return null;

        var presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(60))
                .getObjectRequest(b -> {
                    b.bucket(bucketName);
                    b.key(objectName);
                })
                .build();

        var result = presigner.presignGetObject(presignRequest);
        return result.url();
    }


    @Override
    public InputStream get(String objectName) {
        var request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectName)
                .build();

        return client.getObject(request);
    }

    private void createBucket() {
        var request = CreateBucketRequest.builder()
                .bucket(bucketName)
                .build();
        client.createBucket(request);
    }

    private void deleteBucket() {
        var request = DeleteBucketRequest.builder()
                .bucket(bucketName)
                .build();
        client.deleteBucket(request);
    }

    private boolean bucketExists() {
        var request = HeadBucketRequest.builder()
                .bucket(bucketName)
                .build();
        try {
            client.headBucket(request);
            return true;
        } catch (NoSuchBucketException ignored) {
            return false;
        }
    }

    private boolean objectExists(String objectName) {
        var request = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(objectName)
                .build();
        try {
            client.headObject(request);
            return true;
        } catch (NoSuchKeyException ignored) {
            return false;
        }
    }
}
