package ch.heigvd.amt.team09.util;

import io.github.cdimascio.dotenv.Dotenv;

public class Configuration {
    private static final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .systemProperties()
            .load();

    private Configuration() {

    }

    public static String get(String key) {
        var value = dotenv.get(key);

        if (value == null) {
            throw new MissingKeyException(key);
        }

        return value;
    }

    public static long getUnsignedLong(String key) {
        var value = get(key);

        try {
            return Long.parseUnsignedLong(value);
        } catch (NumberFormatException e) {
            throw new NumberFormatException("The value of %s is not a valid unsigned long: %s".formatted(key, e.getMessage()));
        }
    }

    public static class MissingKeyException extends RuntimeException {
        private MissingKeyException(String key) {
            super("Key %s not found in environment variables or .env file".formatted(key));
        }
    }
}
