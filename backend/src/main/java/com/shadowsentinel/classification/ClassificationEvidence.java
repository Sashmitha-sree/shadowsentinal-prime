package com.shadowsentinel.classification;

import com.shadowsentinel.browser.BrowserActivity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "classification_evidence")
public class ClassificationEvidence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false, unique = true)
    private BrowserActivity activity;

    @Column(name = "schema_version", nullable = false)
    private String schemaVersion = "v1";

    @Column(name = "captured_at", nullable = false)
    private Instant capturedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // -- metadata signals (6)
    @Column(name = "domain_length", nullable = false)
    private int domainLength = 0;

    @Column(name = "visit_count", nullable = false)
    private int visitCount = 0;

    @Column(name = "duration_seconds", nullable = false)
    private int durationSeconds = 0;

    @Column(name = "is_known_ai_domain", nullable = false)
    private boolean isKnownAiDomain = false;

    @Column(name = "hour_of_day", nullable = false)
    private int hourOfDay = 0;

    @Column(name = "path_depth", nullable = false)
    private int pathDepth = 0;

    // -- UI signals (7)
    @Column(name = "chat_interface_present", nullable = false)
    private boolean chatInterfacePresent = false;

    @Column(name = "prompt_input_present", nullable = false)
    private boolean promptInputPresent = false;

    @Column(name = "generate_control_present", nullable = false)
    private boolean generateControlPresent = false;

    @Column(name = "regenerate_control_present", nullable = false)
    private boolean regenerateControlPresent = false;

    @Column(name = "ai_term_count", nullable = false)
    private int aiTermCount = 0;

    @Column(name = "streaming_output_present", nullable = false)
    private boolean streamingOutputPresent = false;

    @Column(name = "file_upload_present", nullable = false)
    private boolean fileUploadPresent = false;

    // -- interaction signals (5)
    @Column(name = "prompt_submit_count", nullable = false)
    private int promptSubmitCount = 0;

    @Column(name = "generate_click_count", nullable = false)
    private int generateClickCount = 0;

    @Column(name = "paste_event_count", nullable = false)
    private int pasteEventCount = 0;

    @Column(name = "copy_from_response_count", nullable = false)
    private int copyFromResponseCount = 0;

    @Column(name = "typed_char_count_bucket", nullable = false)
    private int typedCharCountBucket = 0;

    public ClassificationEvidence() {
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.capturedAt == null) {
            this.capturedAt = Instant.now();
        }
        if (this.schemaVersion == null) {
            this.schemaVersion = "v1";
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private BrowserActivity activity;
        private String schemaVersion = "v1";
        private Instant capturedAt;
        private Instant createdAt;

        // metadata (6)
        private int domainLength = 0;
        private int visitCount = 0;
        private int durationSeconds = 0;
        private boolean isKnownAiDomain = false;
        private int hourOfDay = 0;
        private int pathDepth = 0;

        // UI (7)
        private boolean chatInterfacePresent = false;
        private boolean promptInputPresent = false;
        private boolean generateControlPresent = false;
        private boolean regenerateControlPresent = false;
        private int aiTermCount = 0;
        private boolean streamingOutputPresent = false;
        private boolean fileUploadPresent = false;

        // interaction (5)
        private int promptSubmitCount = 0;
        private int generateClickCount = 0;
        private int pasteEventCount = 0;
        private int copyFromResponseCount = 0;
        private int typedCharCountBucket = 0;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder activity(BrowserActivity activity) {
            this.activity = activity;
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

        public ClassificationEvidence build() {
            ClassificationEvidence entity = new ClassificationEvidence();
            entity.setId(this.id);
            entity.setActivity(this.activity);
            entity.setSchemaVersion(this.schemaVersion != null ? this.schemaVersion : "v1");
            entity.setCapturedAt(this.capturedAt);
            entity.setCreatedAt(this.createdAt);

            entity.setDomainLength(this.domainLength);
            entity.setVisitCount(this.visitCount);
            entity.setDurationSeconds(this.durationSeconds);
            entity.setKnownAiDomain(this.isKnownAiDomain);
            entity.setHourOfDay(this.hourOfDay);
            entity.setPathDepth(this.pathDepth);

            entity.setChatInterfacePresent(this.chatInterfacePresent);
            entity.setPromptInputPresent(this.promptInputPresent);
            entity.setGenerateControlPresent(this.generateControlPresent);
            entity.setRegenerateControlPresent(this.regenerateControlPresent);
            entity.setAiTermCount(this.aiTermCount);
            entity.setStreamingOutputPresent(this.streamingOutputPresent);
            entity.setFileUploadPresent(this.fileUploadPresent);

            entity.setPromptSubmitCount(this.promptSubmitCount);
            entity.setGenerateClickCount(this.generateClickCount);
            entity.setPasteEventCount(this.pasteEventCount);
            entity.setCopyFromResponseCount(this.copyFromResponseCount);
            entity.setTypedCharCountBucket(this.typedCharCountBucket);

            return entity;
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

    public boolean isKnownAiDomain() {
        return isKnownAiDomain;
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassificationEvidence that = (ClassificationEvidence) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
