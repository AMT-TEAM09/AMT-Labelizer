import impl.aws.AwsCloudClient;
import interfaces.CloudClient;

import java.nio.file.Paths;

public class App {
    public static void main(String[] args) {
        CloudClient client = AwsCloudClient.getInstance();

        client.getDataObjectHelper().create("test/test-object", Paths.get("src", "test", "resources", "image.jpg"));
    }
}
