package ch.heigvd.amt.team09.dataobject.assembler;

import ch.heigvd.amt.team09.dataobject.controller.api.DataObjectController;
import ch.heigvd.amt.team09.dataobject.dto.DataObjectResponse;
import ch.heigvd.amt.team09.dataobject.dto.DataObjectResponseModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ResponseModelAssembler implements RepresentationModelAssembler<DataObjectResponse, DataObjectResponseModel> {
    public static final String LINK_UPLOAD = "upload";
    public static final String LINK_PUBLISH = "publish";
    public static final String LINK_DELETE = "delete";
    public static final String LINK_DELETE_ROOT = "deleteRoot";

    @Override
    public DataObjectResponseModel toModel(DataObjectResponse content) {
        var model = new DataObjectResponseModel(content);

        model.add(linkTo(methodOn(DataObjectController.class).upload(
                content.objectName(), null
        )).withRel(LINK_UPLOAD));
        model.add(linkTo(methodOn(DataObjectController.class).publish(
                content.objectName(), Optional.empty()
        )).withRel(LINK_PUBLISH));
        model.add(linkTo(methodOn(DataObjectController.class).delete(
                content.objectName(), Optional.empty()
        )).withRel(LINK_DELETE));
        model.add(linkTo(methodOn(DataObjectController.class).delete(
                Optional.empty()
        )).withRel(LINK_DELETE_ROOT));

        return model;
    }
}
