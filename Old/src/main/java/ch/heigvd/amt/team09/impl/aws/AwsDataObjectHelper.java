package ch.heigvd.amt.team09.impl.aws;

import ch.heigvd.amt.team09.interfaces.DataObjectHelper;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AwsDataObjectHelper implements DataObjectHelper {
    private static final Logger LOG = Logger.getLogger(AwsDataObjectHelper.class.getName());
    private final String bucketName;
    private final S3Client client;
    private final S3Presigner presigner;

    public AwsDataObjectHelper(String bucketName, String regionName, AwsCredentials credentials) {
        var region = Region.of(regionName);

        this.bucketName = bucketName;

        client = S3Client.builder()
                .credentialsProvider(credentials.getProvider())
                .region(region)
                .build();
        presigner = S3Presigner.builder()
                .credentialsProvider(credentials.getProvider())
                .region(region)
                .build();
    }

    @Override
    public void create() {
        createBucket();
    }

    @Override
    public void create(String objectName, Path filePath) throws NoSuchFileException {
        if (Files.notExists(filePath)) {
            throw new NoSuchFileException(filePath.toString());
        }

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
    public URL publish(String objectName, Duration urlDuration) throws NoSuchObjectException {
        if (urlDuration.isNegative() || urlDuration.isZero())
            throw new IllegalArgumentException("Duration must be positive");

        if (!objectExists(objectName)) {
            throw new NoSuchObjectException(objectName);
        }

        var presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(urlDuration)
                .getObjectRequest(b -> {
                    b.bucket(bucketName);
                    b.key(objectName);
                })
                .build();

        var result = presigner.presignGetObject(presignRequest);
        return result.url();
    }


    @Override
    public InputStream get(String objectName) throws NoSuchObjectException {
        if (!objectExists(objectName))
            throw new NoSuchObjectException(objectName);

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
        } catch (NoSuchKeyException e) {
            LOG.log(Level.INFO, "Object not found {0}: {1}", new String[]{objectName, e.getMessage()});
            return false;
        }
    }
}