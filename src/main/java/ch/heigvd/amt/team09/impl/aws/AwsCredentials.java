package ch.heigvd.amt.team09.impl.aws;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

import java.util.Objects;

public class AwsCredentials {
    private final AwsCredentialsProvider credentialsProvider;

    public AwsCredentials(String accessKey, String secretKey) {
        Objects.requireNonNull(accessKey);
        Objects.requireNonNull(secretKey);

        credentialsProvider = StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                        accessKey,
                        secretKey
                )
        );
    }

    public AwsCredentialsProvider getProvider() {
        return credentialsProvider;
    }
}
