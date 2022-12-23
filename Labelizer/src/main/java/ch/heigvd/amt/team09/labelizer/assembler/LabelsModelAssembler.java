package ch.heigvd.amt.team09.labelizer.assembler;

import ch.heigvd.amt.team09.labelizer.controller.api.AnalyzerController;
import ch.heigvd.amt.team09.labelizer.controller.request.AnalyzerRequest;
import ch.heigvd.amt.team09.labelizer.dto.Label;
import ch.heigvd.amt.team09.labelizer.dto.LabelsModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class LabelsModelAssembler implements RepresentationModelAssembler<Label[], LabelsModel> {
    public static final String LINK_URL = "url";
    public static final String LINK_BASE64 = "base64";

    @Override
    public LabelsModel toModel(Label[] labels) {
        var model = new LabelsModel(labels);

        model.add(linkTo(methodOn(AnalyzerController.class).fromUrl(
                new AnalyzerRequest("{url to image}", Optional.empty(), Optional.empty())
        )).withRel(LINK_URL));
        model.add(linkTo(methodOn(AnalyzerController.class).fromBase64(
                new AnalyzerRequest("{image in base64}", Optional.empty(), Optional.empty())
        )).withRel(LINK_BASE64));

        return model;
    }
}
