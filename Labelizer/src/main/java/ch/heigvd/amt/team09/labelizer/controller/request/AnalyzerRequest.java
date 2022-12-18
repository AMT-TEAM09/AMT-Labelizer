package ch.heigvd.amt.team09.labelizer.controller.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.util.Optional;

public record AnalyzerRequest(@NotBlank String source,
                              Optional<@DecimalMin(value = "0", inclusive = false) @DecimalMax("100") Float> minConfidence,
                              Optional<@Positive Integer> maxLabels) {
}