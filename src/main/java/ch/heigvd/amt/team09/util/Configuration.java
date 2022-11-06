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
        return dotenv.get(key);
    }
}
