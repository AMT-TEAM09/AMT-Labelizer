package ch.heigvd.amt.team09.simpleclient.scenario;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Scenario1 extends Scenario {


    @Override
    protected String name() {
        return "Scenario 1 : tout est nouveau";
    }

    @Override
    protected String description() {
        return """
                Premier appel au service.
                -----
                -   Le bucket doit être créé
                -   L'image doit être uploadée
                -   Publication
                -   Analyse
                -   Livraison du résultat sur le bucket hébergeant l'image.
                """;
    }

    @Override
    protected void setup() {
        var deleteStatus = assertDoesNotThrow(this::deleteRootRequest);
        assertTrue(deleteStatus == 204 || deleteStatus == 404);
    }

    @Override
    protected void run() {
        uploadImage();
        var url = publishImage();
        var labels = analyzeImage(url);
        uploadResults(labels);
    }
}
