package ch.heigvd.amt.team09.labelizer.assembler;

import ch.heigvd.amt.team09.labelizer.controller.api.RekognitionController;
import ch.heigvd.amt.team09.labelizer.dto.Label;
import ch.heigvd.amt.team09.labelizer.dto.LabelsModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class LabelsModelAssembler implements RepresentationModelAssembler<Label[], LabelsModel> {
    private static final Optional<Integer> EXAMPLE_MAX_LABELS = Optional.of(10);
    private static final Optional<Float> EXAMPLE_MIN_CONFIDENCE = Optional.of(0.75f);

    @Override
    public LabelsModel toModel(Label[] labels) {
        var model = new LabelsModel(labels);

        model.add(linkTo(methodOn(RekognitionController.class).fromUrl("https://example/myimage",
                EXAMPLE_MIN_CONFIDENCE,
                EXAMPLE_MAX_LABELS
        )).withSelfRel());
        model.add(linkTo(methodOn(RekognitionController.class).fromBase64("dGVzdA==",
                EXAMPLE_MIN_CONFIDENCE,
                EXAMPLE_MAX_LABELS
        )).withSelfRel());

        return model;
    }
}
