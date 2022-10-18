package interfaces;

import java.util.List;

public abstract class BucketManager implements AutoCloseable {
    public abstract List<Bucket> getBuckets();

    public abstract void close();
}
