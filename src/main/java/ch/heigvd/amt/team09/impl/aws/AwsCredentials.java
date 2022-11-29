package ch.heigvd.amt.team09.impl.aws;

import ch.heigvd.amt.team09.util.Configuration;
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

    public static AwsCredentials fromConfig() {
        var accessKey = Configuration.get("AWS_ACCESS_KEY_ID");
        var secretKey = Configuration.get("AWS_SECRET_ACCESS_KEY");

        return new AwsCredentials(accessKey, secretKey);
    }

    public AwsCredentialsProvider getProvider() {
        return credentialsProvider;
    }
}
