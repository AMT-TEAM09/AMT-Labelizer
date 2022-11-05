package interfaces;

import models.Label;

import java.io.IOException;

public interface LabelHelper {
    Label[] execute(String imageUrl, int[] params) throws IOException;

    Label[] executeFromBase64(String base64, int[] params);
}
