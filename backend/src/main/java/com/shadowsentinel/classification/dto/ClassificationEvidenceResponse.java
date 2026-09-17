package com.shadowsentinel.classification.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.Instant;

public class ClassificationEvidenceResponse {

    private Long id;
    private Long activityId;
    private String schemaVersion;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant capturedAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant createdAt;

    // -- metadata signals (6)
    private int domainLength;
    private int visitCount;
    private int durationSeconds;

    @com.fasterxml.jackson.annotation.JsonProperty("isKnownAiDomain")
    private boolean isKnownAiDomain;

    private int hourOfDay;
    private int pathDepth;

    // -- UI signals (7)
    private boolean chatInterfacePresent;
    private boolean promptInputPresent;
    private boolean generateControlPresent;
    private boolean regenerateControlPresent;
    private int aiTermCount;
    private boolean streamingOutputPresent;
    private boolean fileUploadPresent;

    // -- interaction signals (5)
    private int promptSubmitCount;
    private int generateClickCount;
    private int pasteEventCount;
    private int copyFromResponseCount;
    private int typedCharCountBucket;

    public ClassificationEvidenceResponse() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long activityId;
        private String schemaVersion;
        private Instant capturedAt;
        private Instant createdAt;

        private int domainLength;
        private int visitCount;
        private int durationSeconds;
        private boolean isKnownAiDomain;
        private int hourOfDay;
        private int pathDepth;

        private boolean chatInterfacePresent;
        private boolean promptInputPresent;
        private boolean generateControlPresent;
        private boolean regenerateControlPresent;
        private int aiTermCount;
        private boolean streamingOutputPresent;
        private boolean fileUploadPresent;

        private int promptSubmitCount;
        private int generateClickCount;
        private int pasteEventCount;
        private int copyFromResponseCount;
        private int typedCharCountBucket;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

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

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
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

        public ClassificationEvidenceResponse build() {
            ClassificationEvidenceResponse resp = new ClassificationEvidenceResponse();
            resp.setId(this.id);
            resp.setActivityId(this.activityId);
            resp.setSchemaVersion(this.schemaVersion);
            resp.setCapturedAt(this.capturedAt);
            resp.setCreatedAt(this.createdAt);

            resp.setDomainLength(this.domainLength);
            resp.setVisitCount(this.visitCount);
            resp.setDurationSeconds(this.durationSeconds);
            resp.setKnownAiDomain(this.isKnownAiDomain);
            resp.setHourOfDay(this.hourOfDay);
            resp.setPathDepth(this.pathDepth);

            resp.setChatInterfacePresent(this.chatInterfacePresent);
            resp.setPromptInputPresent(this.promptInputPresent);
            resp.setGenerateControlPresent(this.generateControlPresent);
            resp.setRegenerateControlPresent(this.regenerateControlPresent);
            resp.setAiTermCount(this.aiTermCount);
            resp.setStreamingOutputPresent(this.streamingOutputPresent);
            resp.setFileUploadPresent(this.fileUploadPresent);

            resp.setPromptSubmitCount(this.promptSubmitCount);
            resp.setGenerateClickCount(this.generateClickCount);
            resp.setPasteEventCount(this.pasteEventCount);
            resp.setCopyFromResponseCount(this.copyFromResponseCount);
            resp.setTypedCharCountBucket(this.typedCharCountBucket);

            return resp;
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

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
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
