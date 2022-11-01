package impl.aws;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;


class AwsDataObjectHelperTest {
    private static final String BUCKET_NAME = "amt.team09.diduno.education";
    private ProfileCredentialsProvider credentialsProvider;
    private AwsDataObjectHelper objectHelper;

    @BeforeEach
    void setUp() {
        credentialsProvider = ProfileCredentialsProvider.create();
        objectHelper = new AwsDataObjectHelper(credentialsProvider, BUCKET_NAME);
    }

    @AfterEach
    void tearDown() {
        credentialsProvider.close();
    }

    @Test
    void itShouldCreateAFile() {
        objectHelper.Create("test");
    }
}