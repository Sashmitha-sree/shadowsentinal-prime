package com.shadowsentinel.classification.ml;

import com.shadowsentinel.classification.ClassLabel;

import java.util.Map;

public class MlPrediction {

    private ClassLabel classLabel;
    private double confidence;
    private String modelVersion;
    private Map<String, Double> probabilities;

    public MlPrediction() {
    }

    public MlPrediction(ClassLabel classLabel, double confidence, String modelVersion, Map<String, Double> probabilities) {
        this.classLabel = classLabel;
        this.confidence = confidence;
        this.modelVersion = modelVersion;
        this.probabilities = probabilities;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private ClassLabel classLabel;
        private double confidence;
        private String modelVersion;
        private Map<String, Double> probabilities;

        public Builder classLabel(ClassLabel classLabel) {
            this.classLabel = classLabel;
            return this;
        }

        public Builder confidence(double confidence) {
            this.confidence = confidence;
            return this;
        }

        public Builder modelVersion(String modelVersion) {
            this.modelVersion = modelVersion;
            return this;
        }

        public Builder probabilities(Map<String, Double> probabilities) {
            this.probabilities = probabilities;
            return this;
        }

        public MlPrediction build() {
            return new MlPrediction(classLabel, confidence, modelVersion, probabilities);
        }
    }

    public ClassLabel getClassLabel() {
        return classLabel;
    }

    public void setClassLabel(ClassLabel classLabel) {
        this.classLabel = classLabel;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public Map<String, Double> getProbabilities() {
        return probabilities;
    }

    public void setProbabilities(Map<String, Double> probabilities) {
        this.probabilities = probabilities;
    }
}
