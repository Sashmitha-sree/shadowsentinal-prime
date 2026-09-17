package com.shadowsentinel.classification.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class ClassificationEvidenceRequest {

    @NotNull(message = "activityId is required")
    private Long activityId;

    private String schemaVersion = "v1";
    private Instant capturedAt;

    // -- metadata signals (6)
    @Min(value = 0, message = "domainLength must be non-negative")
    private int domainLength = 0;

    @Min(value = 0, message = "visitCount must be non-negative")
    private int visitCount = 0;

    @Min(value = 0, message = "durationSeconds must be non-negative")
    private int durationSeconds = 0;

    @com.fasterxml.jackson.annotation.JsonProperty("isKnownAiDomain")
    private boolean isKnownAiDomain = false;

    @Min(value = 0, message = "hourOfDay must be between 0 and 23")
    @Max(value = 23, message = "hourOfDay must be between 0 and 23")
    private int hourOfDay = 0;

    @Min(value = 0, message = "pathDepth must be non-negative")
    private int pathDepth = 0;

    // -- UI signals (7)
    private boolean chatInterfacePresent = false;
    private boolean promptInputPresent = false;
    private boolean generateControlPresent = false;
    private boolean regenerateControlPresent = false;

    @Min(value = 0, message = "aiTermCount must be non-negative")
    private int aiTermCount = 0;

    private boolean streamingOutputPresent = false;
    private boolean fileUploadPresent = false;

    // -- interaction signals (5)
    @Min(value = 0, message = "promptSubmitCount must be non-negative")
    private int promptSubmitCount = 0;

    @Min(value = 0, message = "generateClickCount must be non-negative")
    private int generateClickCount = 0;

    @Min(value = 0, message = "pasteEventCount must be non-negative")
    private int pasteEventCount = 0;

    @Min(value = 0, message = "copyFromResponseCount must be non-negative")
    private int copyFromResponseCount = 0;

    @Min(value = 0, message = "typedCharCountBucket must be between 0 and 4")
    @Max(value = 4, message = "typedCharCountBucket must be between 0 and 4")
    private int typedCharCountBucket = 0;

    public ClassificationEvidenceRequest() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long activityId;
        private String schemaVersion = "v1";
        private Instant capturedAt;

        private int domainLength = 0;
        private int visitCount = 0;
        private int durationSeconds = 0;
        private boolean isKnownAiDomain = false;
        private int hourOfDay = 0;
        private int pathDepth = 0;

        private boolean chatInterfacePresent = false;
        private boolean promptInputPresent = false;
        private boolean generateControlPresent = false;
        private boolean regenerateControlPresent = false;
        private int aiTermCount = 0;
        private boolean streamingOutputPresent = false;
        private boolean fileUploadPresent = false;

        private int promptSubmitCount = 0;
        private int generateClickCount = 0;
        private int pasteEventCount = 0;
        private int copyFromResponseCount = 0;
        private int typedCharCountBucket = 0;

        public Builder activityId(Long activityId) {
            this.activityId = activityId;
            return this;
        }

        public Builder schemaVersion(String schemaVersion) {
            this.schemaVersion = schemaVersion;
            return this;
        }

        public Builder capturedAt(Instant capturedAt) {
            this.capturedAt = capturedAt;
            return this;
        }

        public Builder domainLength(int domainLength) {
            this.domainLength = domainLength;
            return this;
        }

        public Builder visitCount(int visitCount) {
            this.visitCount = visitCount;
            return this;
        }

        public Builder durationSeconds(int durationSeconds) {
            this.durationSeconds = durationSeconds;
            return this;
        }

        public Builder isKnownAiDomain(boolean isKnownAiDomain) {
            this.isKnownAiDomain = isKnownAiDomain;
            return this;
        }

        public Builder hourOfDay(int hourOfDay) {
            this.hourOfDay = hourOfDay;
            return this;
        }

        public Builder pathDepth(int pathDepth) {
            this.pathDepth = pathDepth;
            return this;
        }

        public Builder chatInterfacePresent(boolean chatInterfacePresent) {
            this.chatInterfacePresent = chatInterfacePresent;
            return this;
        }

        public Builder promptInputPresent(boolean promptInputPresent) {
            this.promptInputPresent = promptInputPresent;
            return this;
        }

        public Builder generateControlPresent(boolean generateControlPresent) {
            this.generateControlPresent = generateControlPresent;
            return this;
        }

        public Builder regenerateControlPresent(boolean regenerateControlPresent) {
            this.regenerateControlPresent = regenerateControlPresent;
            return this;
        }

        public Builder aiTermCount(int aiTermCount) {
            this.aiTermCount = aiTermCount;
            return this;
        }

        public Builder streamingOutputPresent(boolean streamingOutputPresent) {
            this.streamingOutputPresent = streamingOutputPresent;
            return this;
        }

        public Builder fileUploadPresent(boolean fileUploadPresent) {
            this.fileUploadPresent = fileUploadPresent;
            return this;
        }

        public Builder promptSubmitCount(int promptSubmitCount) {
            this.promptSubmitCount = promptSubmitCount;
            return this;
        }

        public Builder generateClickCount(int generateClickCount) {
            this.generateClickCount = generateClickCount;
            return this;
        }

        public Builder pasteEventCount(int pasteEventCount) {
            this.pasteEventCount = pasteEventCount;
            return this;
        }

        public Builder copyFromResponseCount(int copyFromResponseCount) {
            this.copyFromResponseCount = copyFromResponseCount;
            return this;
        }

        public Builder typedCharCountBucket(int typedCharCountBucket) {
            this.typedCharCountBucket = typedCharCountBucket;
            return this;
        }

        public ClassificationEvidenceRequest build() {
            ClassificationEvidenceRequest req = new ClassificationEvidenceRequest();
            req.setActivityId(this.activityId);
            req.setSchemaVersion(this.schemaVersion != null ? this.schemaVersion : "v1");
            req.setCapturedAt(this.capturedAt);

            req.setDomainLength(this.domainLength);
            req.setVisitCount(this.visitCount);
            req.setDurationSeconds(this.durationSeconds);
            req.setKnownAiDomain(this.isKnownAiDomain);
            req.setHourOfDay(this.hourOfDay);
            req.setPathDepth(this.pathDepth);

            req.setChatInterfacePresent(this.chatInterfacePresent);
            req.setPromptInputPresent(this.promptInputPresent);
            req.setGenerateControlPresent(this.generateControlPresent);
            req.setRegenerateControlPresent(this.regenerateControlPresent);
            req.setAiTermCount(this.aiTermCount);
            req.setStreamingOutputPresent(this.streamingOutputPresent);
            req.setFileUploadPresent(this.fileUploadPresent);

            req.setPromptSubmitCount(this.promptSubmitCount);
            req.setGenerateClickCount(this.generateClickCount);
            req.setPasteEventCount(this.pasteEventCount);
            req.setCopyFromResponseCount(this.copyFromResponseCount);
            req.setTypedCharCountBucket(this.typedCharCountBucket);

            return req;
        }
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public Instant getCapturedAt() {
        return capturedAt;
    }

    public void setCapturedAt(Instant capturedAt) {
        this.capturedAt = capturedAt;
    }

    public int getDomainLength() {
        return domainLength;
    }

    public void setDomainLength(int domainLength) {
        this.domainLength = domainLength;
    }

    public int getVisitCount() {
        return visitCount;
    }

    public void setVisitCount(int visitCount) {
        this.visitCount = visitCount;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = durationSeconds;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("isKnownAiDomain")
    public boolean isKnownAiDomain() {
        return isKnownAiDomain;
    }

    @com.fasterxml.jackson.annotation.JsonProperty("isKnownAiDomain")
    public void setKnownAiDomain(boolean knownAiDomain) {
        isKnownAiDomain = knownAiDomain;
    }

    public int getHourOfDay() {
        return hourOfDay;
    }

    public void setHourOfDay(int hourOfDay) {
        this.hourOfDay = hourOfDay;
    }

    public int getPathDepth() {
        return pathDepth;
    }

    public void setPathDepth(int pathDepth) {
        this.pathDepth = pathDepth;
    }

    public boolean isChatInterfacePresent() {
        return chatInterfacePresent;
    }

    public void setChatInterfacePresent(boolean chatInterfacePresent) {
        this.chatInterfacePresent = chatInterfacePresent;
    }

    public boolean isPromptInputPresent() {
        return promptInputPresent;
    }

    public void setPromptInputPresent(boolean promptInputPresent) {
        this.promptInputPresent = promptInputPresent;
    }

    public boolean isGenerateControlPresent() {
        return generateControlPresent;
    }

    public void setGenerateControlPresent(boolean generateControlPresent) {
        this.generateControlPresent = generateControlPresent;
    }

    public boolean isRegenerateControlPresent() {
        return regenerateControlPresent;
    }

    public void setRegenerateControlPresent(boolean regenerateControlPresent) {
        this.regenerateControlPresent = regenerateControlPresent;
    }

    public int getAiTermCount() {
        return aiTermCount;
    }

    public void setAiTermCount(int aiTermCount) {
        this.aiTermCount = aiTermCount;
    }

    public boolean isStreamingOutputPresent() {
        return streamingOutputPresent;
    }

    public void setStreamingOutputPresent(boolean streamingOutputPresent) {
        this.streamingOutputPresent = streamingOutputPresent;
    }

    public boolean isFileUploadPresent() {
        return fileUploadPresent;
    }

    public void setFileUploadPresent(boolean fileUploadPresent) {
        this.fileUploadPresent = fileUploadPresent;
    }

    public int getPromptSubmitCount() {
        return promptSubmitCount;
    }

    public void setPromptSubmitCount(int promptSubmitCount) {
        this.promptSubmitCount = promptSubmitCount;
    }

    public int getGenerateClickCount() {
        return generateClickCount;
    }

    public void setGenerateClickCount(int generateClickCount) {
        this.generateClickCount = generateClickCount;
    }

    public int getPasteEventCount() {
        return pasteEventCount;
    }

    public void setPasteEventCount(int pasteEventCount) {
        this.pasteEventCount = pasteEventCount;
    }

    public int getCopyFromResponseCount() {
        return copyFromResponseCount;
    }

    public void setCopyFromResponseCount(int copyFromResponseCount) {
        this.copyFromResponseCount = copyFromResponseCount;
    }

    public int getTypedCharCountBucket() {
        return typedCharCountBucket;
    }

    public void setTypedCharCountBucket(int typedCharCountBucket) {
        this.typedCharCountBucket = typedCharCountBucket;
    }
}
