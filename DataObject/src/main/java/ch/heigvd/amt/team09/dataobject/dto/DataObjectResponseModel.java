package ch.heigvd.amt.team09.dataobject.dto;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;

public class DataObjectResponseModel extends EntityModel<DataObjectResponse> {
    public DataObjectResponseModel(DataObjectResponse dataObjectResponse) {
        super(dataObjectResponse);
    }

    public DataObjectResponseModel withSelf(String self) {
        var link = getRequiredLink(self);
        this.mapLink(link.getRel(), Link::withSelfRel);
        return this;
    }
}
