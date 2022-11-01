package interfaces;

public interface LabelDetector {
    String Execute(String imageUri, int[] params);

    String ExecuteFromBase64(String base64, int[] params);
}
