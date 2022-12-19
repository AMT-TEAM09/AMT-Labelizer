package ch.heigvd.amt.team09.labelizer.dto;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

public class LabelsModel extends EntityModel<Labels> {
    public LabelsModel(Label[] labels) {
        super(new Labels(labels));
    }

    public LabelsModel withSelf(String self) {
        var link = getRequiredLink(self);
        this.mapLink(link.getRel(), Link::withSelfRel);
        return this;
    }
}
