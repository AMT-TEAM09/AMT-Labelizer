package ch.heigvd.amt.team09.labelizer.controller.request;

import jakarta.validation.constraints.NotBlank;

import java.util.Optional;

public record RekognitionRequest(@NotBlank String source,
                                 Optional<Float> minConfidence,
                                 Optional<Integer> maxLabels) {
}