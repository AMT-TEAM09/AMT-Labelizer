package ch.heigvd.amt.team09.labelizer.service.interfaces;


import ch.heigvd.amt.team09.labelizer.dto.Label;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public interface AnalyzerService {
    int DEFAULT_MAX_LABELS = 10;
    float DEFAULT_MIN_CONFIDENCE = 90.0f;

    Label[] execute(String imageUrl, Consumer<Options.Builder> options) throws IOException;

    Label[] executeFromBase64(String base64, Consumer<Options.Builder> options);

    class Options {
        private float minConfidence = DEFAULT_MIN_CONFIDENCE;
        private int maxLabels = DEFAULT_MAX_LABELS;

        private Options() {
        }

        public static Builder builder() {
            return new Builder();
        }

        public float minConfidence() {
            return minConfidence;
        }

        public int maxLabels() {
            return maxLabels;
        }

        public static class Builder {
            private List<Consumer<Options>> operations = new ArrayList<>();

            public Builder minConfidence(float minConfidence) {
                if (minConfidence < 0 || minConfidence > 100) {
                    throw new IllegalArgumentException("minConfidence must be between 0 and 100");
                }

                operations.add(options -> options.minConfidence = minConfidence);
                return this;
            }

            public Builder maxLabels(int maxLabels) {
                if (maxLabels < 1) {
                    throw new IllegalArgumentException("maxLabels must be greater than 0");
                }

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
