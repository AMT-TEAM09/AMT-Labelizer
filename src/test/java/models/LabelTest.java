package models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LabelTest {

    @Test
    void toJson_nominalCase_success() {
        // given
        var label = new Label("Beaver", 0.99f);

        // when
        var json = label.toJson();

        // then
        assertEquals("{\"name\":\"Beaver\",\"confidence\":0.99}", json);
    }

    @Test
    void toJson_nullName_success() {
        // given
        var label = new Label(null, 0.99f);

        // when
        var json = label.toJson();

        // then
        assertEquals("{\"name\":null,\"confidence\":0.99}", json);
    }
}