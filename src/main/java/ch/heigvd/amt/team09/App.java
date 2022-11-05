package ch.heigvd.amt.team09;

import ch.heigvd.amt.team09.impl.aws.AwsCloudClient;

public class App {
    public static void main(String[] args) {
        var client = AwsCloudClient.getInstance();
    }
}
