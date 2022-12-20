package ch.heigvd.amt.team09.simpleclient.scenario;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class Scenario3 extends Scenario {
    @Override
    protected String name() {
        return "Scenario 3 : tout existe";
    }

    @Override
    protected String description() {
        return """
                -   Le bucket ne doit pas être créé
                -   L'image ne doit être uploadée
                -----
                -   Publication
                -   Analyse
                -   Livraison du résultat sur le bucket hébergeant l'image.
                """;
    }

    @Override
    protected void setup() {
        assertDoesNotThrow(this::uploadImage);
    }

    @Override
    protected void run() {
        var url = publishImage();
        var labels = analyzeImage(url);
        uploadResults(labels);
    }
}
