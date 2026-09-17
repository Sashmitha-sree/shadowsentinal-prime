package com.shadowsentinel.classification;

import com.shadowsentinel.classification.ml.HttpMlClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HttpMlClientTest {

    @Test
    @DisplayName("HttpMlClient retries and throws RuntimeException when endpoint unreachable")
    void predict_WhenUnreachable_RetriesAndThrows() {
        HttpMlClient client = new HttpMlClient("http://127.0.0.1:59998");

        ClassificationEvidence evidence = ClassificationEvidence.builder()
                .domainLength(10)
                .visitCount(1)
                .durationSeconds(30)
                .isKnownAiDomain(true)
                .hourOfDay(12)
                .pathDepth(1)
                .chatInterfacePresent(true)
                .promptInputPresent(false)
                .generateControlPresent(false)
                .regenerateControlPresent(false)
                .aiTermCount(1)
                .streamingOutputPresent(false)
                .fileUploadPresent(false)
                .promptSubmitCount(0)
                .generateClickCount(0)
                .pasteEventCount(0)
                .copyFromResponseCount(0)
                .typedCharCountBucket(0)
                .build();

        RuntimeException ex = assertThrows(RuntimeException.class, () -> client.predict(evidence));
        assertTrue(ex.getMessage().contains("ML service call failed after retry"));
    }
}
