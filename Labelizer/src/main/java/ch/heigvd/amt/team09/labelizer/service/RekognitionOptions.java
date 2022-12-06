package ch.heigvd.amt.team09.labelizer.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Consumer;

public class RekognitionOptions {private Float minConfidence;
    private Integer maxLabels;

    private RekognitionOptions() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public Optional<Float> minConfidence() {
        return minConfidence == null ? Optional.empty() : Optional.of(minConfidence);
    }

    public OptionalInt maxLabels() {
        return maxLabels == null ? OptionalInt.empty() : OptionalInt.of(maxLabels);
    }

    public static class Builder {
        private List<Consumer<RekognitionOptions>> operations = new ArrayList<>();

        public Builder minConfidence(float minConfidence) {
            operations.add(options -> options.minConfidence = minConfidence);
            return this;
        }

        public Builder maxLabels(int maxLabels) {
            operations.add(options -> options.maxLabels = maxLabels);
            return this;
        }

        public RekognitionOptions build() {
            var options = new RekognitionOptions();
            operations.forEach(op -> op.accept(options));
            return options;
        }
    }
}
