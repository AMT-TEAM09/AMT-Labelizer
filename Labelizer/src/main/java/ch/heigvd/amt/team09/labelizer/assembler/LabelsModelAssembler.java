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
    @Override
    public LabelsModel toModel(Label[] labels) {
        var model = new LabelsModel(labels);

        model.add(linkTo(methodOn(AnalyzerController.class).fromUrl(
                new AnalyzerRequest("{url to image}", Optional.empty(), Optional.empty())
        )).withSelfRel());
        model.add(linkTo(methodOn(AnalyzerController.class).fromBase64(
                new AnalyzerRequest("{image in base64}", Optional.empty(), Optional.empty())
        )).withSelfRel());

        return model;
    }
}
