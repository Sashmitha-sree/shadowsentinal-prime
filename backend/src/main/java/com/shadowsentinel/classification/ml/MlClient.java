package com.shadowsentinel.classification.ml;

import com.shadowsentinel.classification.ClassificationEvidence;

public interface MlClient {

    /**
     * Obtains a prediction from the ML inference service based on the 18 evidence signals.
     *
     * @param evidence the captured classification evidence
     * @return the prediction containing classLabel, confidence, modelVersion, and probabilities
     */
    MlPrediction predict(ClassificationEvidence evidence);
}
