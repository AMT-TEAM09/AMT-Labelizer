package interfaces;

import models.Label;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Consumer;

public interface LabelHelper {
    Label[] execute(String imageUrl, Consumer<LabelOptions.Builder> options) throws IOException;

    Label[] executeFromBase64(String base64, Consumer<LabelOptions.Builder> options);

    class LabelOptions {
        private Float minConfidence;
        private Integer maxLabels;

        private LabelOptions() {
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
            private List<Consumer<LabelOptions>> operations = new ArrayList<>();

            public Builder minConfidence(float minConfidence) {
                operations.add(options -> options.minConfidence = minConfidence);
                return this;
            }

            public Builder maxLabels(int maxLabels) {
                operations.add(options -> options.maxLabels = maxLabels);
                return this;
            }

            public LabelOptions build() {
                var options = new LabelOptions();
                operations.forEach(op -> op.accept(options));
                return options;
            }
        }
    }
}
