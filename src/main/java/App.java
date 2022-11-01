import impl.aws.AwsCloudClient;
import interfaces.CloudClient;

public class App {
    public static void main(String[] args) {
        CloudClient client = AwsCloudClient.getInstance();
    }
}
