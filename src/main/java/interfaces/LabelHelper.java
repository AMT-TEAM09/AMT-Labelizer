package interfaces;

import java.io.IOException;

public interface LabelHelper {
    String Execute(String imageUrl, int[] params) throws IOException;

    String ExecuteFromBase64(String base64, int[] params);
}
