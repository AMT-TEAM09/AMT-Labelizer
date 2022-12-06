package ch.heigvd.amt.team09.labelizer.service.interfaces;


import ch.heigvd.amt.team09.labelizer.dto.Label;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Consumer;

public interface AnalyzerService {
    Label[] execute(String imageUrl, Consumer<Options.Builder> options) throws IOException;

    Label[] executeFromBase64(String base64, Consumer<Options.Builder> options);

    class Options {
        private Float minConfidence;
        private Integer maxLabels;

        private Options() {
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
            private List<Consumer<Options>> operations = new ArrayList<>();

            public Builder minConfidence(float minConfidence) {
                operations.add(options -> options.minConfidence = minConfidence);
                return this;
            }

            public Builder maxLabels(int maxLabels) {
                operations.add(options -> options.maxLabels = maxLabels);
                return this;
            }

            public Options build() {
                var options = new Options();
                operations.forEach(op -> op.accept(options));
                return options;
            }
        }
    }
}
