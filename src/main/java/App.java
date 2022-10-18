import impl.aws.AwsBucketManager;
import interfaces.Bucket;
import interfaces.BucketManager;

public class App {
    public static void main(String[] args) {
        try (BucketManager manager = new AwsBucketManager()) {
            manager.getBuckets().stream().map(Bucket::name).forEach(System.out::println);
        }
    }
}
