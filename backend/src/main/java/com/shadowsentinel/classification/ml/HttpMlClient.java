package com.shadowsentinel.classification.ml;

import com.shadowsentinel.classification.ClassificationEvidence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
public class HttpMlClient implements MlClient {

    private static final Logger log = LoggerFactory.getLogger(HttpMlClient.class);

    private final RestClient restClient;
    private final String mlServiceUrl;

    public HttpMlClient(@Value("${ml.service.url:http://localhost:8000}") String mlServiceUrl) {
        this.mlServiceUrl = mlServiceUrl;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(3));
        requestFactory.setReadTimeout(Duration.ofSeconds(3));

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .baseUrl(mlServiceUrl)
                .build();
    }

    @Override
    public MlPrediction predict(ClassificationEvidence evidence) {
        Map<String, Object> requestBody = mapEvidenceToFeatures(evidence);

        Exception lastException = null;
        for (int attempt = 1; attempt <= 2; attempt++) {
            try {
                MlPrediction prediction = restClient.post()
                        .uri("/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(requestBody)
                        .retrieve()
                        .body(MlPrediction.class);

                if (prediction != null) {
                    return prediction;
                }
            } catch (RestClientException ex) {
                lastException = ex;
                log.warn("ML client predict call attempt {} failed for activity {}: {}",
                        attempt,
                        evidence.getActivity() != null ? evidence.getActivity().getId() : "null",
                        ex.getMessage());
            }
        }

        throw new RuntimeException(
                "ML service call failed after retry: " + (lastException != null ? lastException.getMessage() : "null response"),
                lastException
        );
    }

    private Map<String, Object> mapEvidenceToFeatures(ClassificationEvidence evidence) {
        Map<String, Object> map = new HashMap<>();
        map.put("domainLength", evidence.getDomainLength());
        map.put("visitCount", evidence.getVisitCount());
        map.put("durationSeconds", evidence.getDurationSeconds());
        map.put("isKnownAiDomain", evidence.isKnownAiDomain());
        map.put("hourOfDay", evidence.getHourOfDay());
        map.put("pathDepth", evidence.getPathDepth());

        map.put("chatInterfacePresent", evidence.isChatInterfacePresent());
        map.put("promptInputPresent", evidence.isPromptInputPresent());
        map.put("generateControlPresent", evidence.isGenerateControlPresent());
        map.put("regenerateControlPresent", evidence.isRegenerateControlPresent());
        map.put("aiTermCount", evidence.getAiTermCount());
        map.put("streamingOutputPresent", evidence.isStreamingOutputPresent());
        map.put("fileUploadPresent", evidence.isFileUploadPresent());

        map.put("promptSubmitCount", evidence.getPromptSubmitCount());
        map.put("generateClickCount", evidence.getGenerateClickCount());
        map.put("pasteEventCount", evidence.getPasteEventCount());
        map.put("copyFromResponseCount", evidence.getCopyFromResponseCount());
        map.put("typedCharCountBucket", evidence.getTypedCharCountBucket());
        return map;
    }

    public String getMlServiceUrl() {
        return mlServiceUrl;
    }
}
