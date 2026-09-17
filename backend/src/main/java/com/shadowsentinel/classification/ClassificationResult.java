package com.shadowsentinel.classification;

import com.shadowsentinel.browser.BrowserActivity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "classification_results")
public class ClassificationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false, unique = true)
    private BrowserActivity activity;

    @Enumerated(EnumType.STRING)
    @Column(name = "class_label", nullable = false, length = 32)
    private ClassLabel classLabel;

    @Column(nullable = false)
    private double confidence;

    @Column(name = "model_version", nullable = false, length = 32)
    private String modelVersion;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public ClassificationResult() {
    }

    public ClassificationResult(Long id, BrowserActivity activity, ClassLabel classLabel,
                                double confidence, String modelVersion, Instant createdAt) {
        this.id = id;
        this.activity = activity;
        this.classLabel = classLabel;
        this.confidence = confidence;
        this.modelVersion = modelVersion;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private BrowserActivity activity;
        private ClassLabel classLabel;
        private double confidence;
        private String modelVersion;
        private Instant createdAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder activity(BrowserActivity activity) {
            this.activity = activity;
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

        public ClassificationResult build() {
            return new ClassificationResult(id, activity, classLabel, confidence, modelVersion, createdAt);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BrowserActivity getActivity() {
        return activity;
    }

    public void setActivity(BrowserActivity activity) {
        this.activity = activity;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassificationResult that = (ClassificationResult) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
