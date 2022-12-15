package ch.heigvd.amt.team09.dataobject.service.impl;

import ch.heigvd.amt.team09.dataobject.service.interfaces.DataObjectService;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class AwsDataObjectService implements DataObjectService {
    private static final Logger LOG = LoggerFactory.getLogger(AwsDataObjectService.class.getName());
    private final String bucketName;
    private final S3Client client;
    private final S3Presigner presigner;

    private AwsDataObjectService(Optional<String> bucketName) {
        var dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        var regionName = Objects.requireNonNull(dotenv.get("AWS_REGION"), "AWS_REGION is not set");
        var region = Region.of(regionName);
        var credentials = getCredentialsProvider(dotenv);

        this.bucketName = bucketName.orElseGet(() -> Objects.requireNonNull(dotenv.get("AWS_BUCKET_NAME"), "AWS_BUCKET_NAME is not set"));

        client = S3Client.builder()
                .credentialsProvider(credentials)
                .region(region)
                .build();
        presigner = S3Presigner.builder()
                .credentialsProvider(credentials)
                .region(region)
                .build();
    }

    public AwsDataObjectService() {
        this(Optional.empty());
    }

    public AwsDataObjectService(String bucketName) {
        this(Optional.of(bucketName));
    }

    private static AwsCredentialsProvider getCredentialsProvider(Dotenv dotenv) {
        var accessKey = Objects.requireNonNull(dotenv.get("AWS_ACCESS_KEY_ID"), "AWS_ACCESS_KEY_ID is not set");
        var secretKey = Objects.requireNonNull(dotenv.get("AWS_SECRET_ACCESS_KEY"), "AWS_SECRET_ACCESS_KEY is not set");

        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                        accessKey,
                        secretKey
                )
        );
    }

    public void create(String objectName, byte[] content) throws ObjectAlreadyExistsException {
        if (!bucketExists()) {
            createBucket();
        }

        if (objectExists(objectName)) {
            throw new ObjectAlreadyExistsException(objectName);
        }

        var request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectName)
                .build();
        client.putObject(request, RequestBody.fromBytes(content));
    }

    public boolean exists() {
        return bucketExists();
    }

    public boolean exists(String objectName) {
        return exists() && (objectExists(objectName) || folderExists(objectName));
    }

    public void delete(boolean recursive) throws ObjectNotFoundException, ObjectNotEmptyException {
        if (!bucketExists()) {
            throw new ObjectNotFoundException(bucketName);
        }

        var objects = listObjectsInBucket();

        if (!recursive && !objects.isEmpty()) {
            throw new ObjectNotEmptyException(bucketName);
        }

        for (var object : objects) {
            delete(object.key());
        }

        deleteBucket();
    }

    public void delete(String objectName, boolean recursive) throws ObjectNotFoundException, ObjectNotEmptyException {
        if (!bucketExists()) {
            throw new ObjectNotFoundException(bucketName);
        }

        if (objectExists(objectName)) {
            deleteObject(objectName);
            return;
        }
        deleteFolder(objectName, recursive);
    }

    public URL publish(String objectName, Duration urlDuration) throws ObjectNotFoundException {
        if (urlDuration.isNegative() || urlDuration.isZero())
            throw new IllegalArgumentException("Duration must be positive");

        if (!bucketExists()) {
            throw new ObjectNotFoundException(bucketName);
        }

        if (!objectExists(objectName)) {
            throw new ObjectNotFoundException(objectName);
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

    public InputStream get(String objectName) throws ObjectNotFoundException {
        if (!bucketExists()) {
            throw new ObjectNotFoundException(bucketName);
        }

        if (!objectExists(objectName))
            throw new ObjectNotFoundException(objectName);

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
            LOG.info("Object not found {}: {}", objectName, e.getMessage());
            return false;
        }
    }

    private boolean folderExists(String folderName) {
        return !listObjects(folderName).isEmpty();
    }

    private List<S3Object> listObjectsInBucket() {
        return listObjects("");
    }

    private List<S3Object> listObjects(String folderName) {
        var request = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(folderName)
                .build();
        var response = client.listObjectsV2(request);
        return response.contents();
    }

    private void deleteObject(String objectName) throws ObjectNotFoundException {
        if (!objectExists(objectName))
            throw new ObjectNotFoundException(objectName);

        var request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(objectName)
                .build();
        client.deleteObject(request);
    }

    private void deleteFolder(String folderName, boolean recursive)
            throws ObjectNotFoundException, ObjectNotEmptyException {
        var objects = listObjects(folderName);

        if (objects.isEmpty()) // S'il est vide, c'est qu'il n'existe pas
            throw new ObjectNotFoundException(folderName);

        if (!recursive)
            throw new ObjectNotEmptyException(folderName);

        for (var object : objects) {
            deleteObject(object.key());
        }
    }
}
