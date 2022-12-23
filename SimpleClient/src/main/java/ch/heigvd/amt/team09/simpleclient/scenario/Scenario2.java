package ch.heigvd.amt.team09.simpleclient.scenario;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Scenario2 extends Scenario {
    @Override
    protected String name() {
        return "Scenario 2 : seul le bucket existe";
    }
    
    @Override
    protected String description() {
        return """
                -   Le bucket ne doit pas être créé
                -----
                -   L'image doit être uploadée
                -   Publication
                -   Analyse
                -   Livraison du résultat sur le bucket hébergeant l'image.
                """;
    }

    @Override
    protected void setup() {
        assertTrue(createRootObject());
    }

    @Override
    protected void run() {
        uploadImage();
        var url = publishImage();
        var labels = analyzeImage(url);
        uploadResults(labels);
    }
}
