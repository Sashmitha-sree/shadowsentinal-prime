package com.shadowsentinel.classification.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shadowsentinel.classification.ClassLabel;

import java.time.Instant;

public class ClassificationResultResponse {

    private Long id;
    private Long activityId;
    private ClassLabel classLabel;
    private double confidence;
    private String modelVersion;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant createdAt;

    public ClassificationResultResponse() {
    }

    public ClassificationResultResponse(Long id, Long activityId, ClassLabel classLabel,
                                        double confidence, String modelVersion, Instant createdAt) {
        this.id = id;
        this.activityId = activityId;
        this.classLabel = classLabel;
        this.confidence = confidence;
        this.modelVersion = modelVersion;
        this.createdAt = createdAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long activityId;
        private ClassLabel classLabel;
        private double confidence;
        private String modelVersion;
        private Instant createdAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder activityId(Long activityId) {
            this.activityId = activityId;
            return this;
        }

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

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ClassificationResultResponse build() {
            return new ClassificationResultResponse(id, activityId, classLabel, confidence, modelVersion, createdAt);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
