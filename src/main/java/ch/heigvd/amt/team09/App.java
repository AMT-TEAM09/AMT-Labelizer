package ch.heigvd.amt.team09;

import ch.heigvd.amt.team09.impl.aws.AwsCloudClient;
import ch.heigvd.amt.team09.interfaces.CloudClient;
import ch.heigvd.amt.team09.interfaces.DataObjectHelper;
import ch.heigvd.amt.team09.interfaces.LabelHelper.LabelOptions;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.function.Consumer;

public class App {
    private final CloudClient client = AwsCloudClient.getInstance();
    @Parameter(names = {"--help", "-h"}, help = true, order = -3)
    private boolean help;
    @Parameter(names = {"--labels", "-l"}, description = "Number of labels to return")
    private Integer maxLabels;
    @Parameter(names = {"--confidence", "-c"}, description = "Minimum confidence for a label to be returned")
    private Float minConfidence;
    @Parameter(names = {"--remote", "-r"}, description = "Remote file name. If set, results will be uploaded to S3")
    private String remoteFileName;
    @Parameter(required = true, description = "<content>")
    private String content;

    @Parameter(names = {"--mode", "-m"}, required = true, description = "Mode to use (0 (base64) | 1 (object)| 2 " +
            "(url))", order = -2)
    private Integer mode;

    public static void main(String[] args) throws DataObjectHelper.NoSuchObjectException {
        var app = new App();
        var jcommander = JCommander.newBuilder()
                .addObject(app)
                .programName("app")
                .build();

        try {
            jcommander.parse(args);
        } catch (ParameterException e) {
            System.out.println(e.getMessage());
            jcommander.usage();
            return;
        }
        
        if (app.help) {
            jcommander.usage();
            return;
        }

        System.out.println(app.analyze());
    }

    private String toPrettyJson(String json) throws JsonProcessingException {
        var mapper = new ObjectMapper();
        var object = mapper.readValue(json, Object.class);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }

    public String analyze() throws DataObjectHelper.NoSuchObjectException {
        try {
            return switch (mode) {
                case 0 -> toPrettyJson(client.analyzeFromBase64(content, getOptions(), remoteFileName));
                case 1 -> toPrettyJson(client.analyzeFromObject(content, getOptions(), remoteFileName));
                case 2 -> toPrettyJson(client.analyzeFromUrl(content, getOptions(), remoteFileName));
                default -> "Unknown mode";
            };
        } catch (IOException e) {
            return "An error occurred: " + e.getMessage();
        }
    }

    private Consumer<LabelOptions.Builder> getOptions() {
        return options -> {
            if (maxLabels != null) {
                options.maxLabels(maxLabels);
            }
            if (minConfidence != null) {
                options.minConfidence(minConfidence);
            }
        };
    }
}
