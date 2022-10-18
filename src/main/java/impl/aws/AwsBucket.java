package impl.aws;

import software.amazon.awssdk.services.s3.model.Bucket;

public class AwsBucket implements interfaces.Bucket {
    private final Bucket bucket;

    public AwsBucket(Bucket bucket) {
        this.bucket = bucket;
    }

    public String name() {
        return bucket.name();
    }
}
