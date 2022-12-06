package ch.heigvd.amt.team09.labelizer.dto;

import org.springframework.hateoas.EntityModel;

public class LabelsModel extends EntityModel<Label[]> {
    public LabelsModel(Label[] labels) {
        super(labels);
    }
}
