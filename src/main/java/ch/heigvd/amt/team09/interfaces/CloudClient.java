package ch.heigvd.amt.team09.interfaces;

import ch.heigvd.amt.team09.interfaces.LabelHelper.LabelOptions;

import java.io.IOException;
import java.util.function.Consumer;

public interface CloudClient {
    String analyzeFromBase64(String base64, Consumer<LabelOptions.Builder> options, String remoteFileName) throws IOException;

    String analyzeFromBase64(String base64, Consumer<LabelOptions.Builder> options) throws IOException;

    String analyzeFromUrl(String url, Consumer<LabelOptions.Builder> options, String remoteFileName) throws IOException;

    String analyzeFromUrl(String url, Consumer<LabelOptions.Builder> options) throws IOException;

    String analyzeFromObject(String objectName, Consumer<LabelOptions.Builder> options, String remoteFileName) throws IOException;

    String analyzeFromObject(String objectName, Consumer<LabelOptions.Builder> options) throws IOException;
}
