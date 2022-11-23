package ch.heigvd.amt.team09.util;

import ch.heigvd.amt.team09.impl.aws.AwsCredentials;
import io.github.cdimascio.dotenv.Dotenv;

public class Configuration {
    private static final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .systemProperties()
            .load();

    private static AwsCredentials awsCredentials;

    private Configuration() {

    }

    public static String get(String key) {
        return dotenv.get(key);
    }

    private static String getOrThrow(String key) {
        var value = get(key);
        if (value == null) {
            throw new MissingKeyException(key);
        }
        return value;
    }

    public static AwsCredentials getAwsCredentials() {
        if (awsCredentials == null) {
            var accessKey = getOrThrow("AWS_ACCESS_KEY_ID");
            var secretKey = getOrThrow("AWS_SECRET_ACCESS_KEY");

            awsCredentials = new AwsCredentials(
                    accessKey,
                    secretKey
            );
        }
        return awsCredentials;
    }

    public static class MissingKeyException extends RuntimeException {
        private MissingKeyException(String key) {
            super("Key %s not found in environment variables or .env file".formatted(key));
        }
    }
}
