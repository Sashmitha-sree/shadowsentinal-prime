"""
Integration tests for the ML service FastAPI application.
"""

from fastapi.testclient import TestClient
from app.main import app


def test_api():
    with TestClient(app) as client:
        # 1. Health check
        resp_health = client.get("/health")
        assert resp_health.status_code == 200
        health_data = resp_health.json()
        assert health_data["status"] == "healthy"
        assert health_data["modelVersion"] == "v1"

        # 2. Predict NON_AI
        non_ai_payload = {
            "domainLength": 12,
            "visitCount": 5,
            "durationSeconds": 30,
            "isKnownAiDomain": False,
            "hourOfDay": 10,
            "pathDepth": 1,
            "chatInterfacePresent": False,
            "promptInputPresent": False,
            "generateControlPresent": False,
            "regenerateControlPresent": False,
            "aiTermCount": 0,
            "streamingOutputPresent": False,
            "fileUploadPresent": False,
            "promptSubmitCount": 0,
            "generateClickCount": 0,
            "pasteEventCount": 0,
            "copyFromResponseCount": 0,
            "typedCharCountBucket": 0
        }
        resp_non_ai = client.post("/predict", json=non_ai_payload)
        assert resp_non_ai.status_code == 200
        data_non_ai = resp_non_ai.json()
        assert data_non_ai["classLabel"] == "NON_AI"
        assert data_non_ai["confidence"] > 0.80
        assert data_non_ai["modelVersion"] == "v1"
        assert "probabilities" in data_non_ai
        assert "NON_AI" in data_non_ai["probabilities"]

        # 3. Predict AI_GENERATION
        ai_gen_payload = {
            "domainLength": 11,
            "visitCount": 15,
            "durationSeconds": 300,
            "isKnownAiDomain": True,
            "hourOfDay": 14,
            "pathDepth": 2,
            "chatInterfacePresent": True,
            "promptInputPresent": True,
            "generateControlPresent": True,
            "regenerateControlPresent": True,
            "aiTermCount": 8,
            "streamingOutputPresent": True,
            "fileUploadPresent": True,
            "promptSubmitCount": 3,
            "generateClickCount": 3,
            "pasteEventCount": 1,
            "copyFromResponseCount": 2,
            "typedCharCountBucket": 3
        }
        resp_ai_gen = client.post("/predict", json=ai_gen_payload)
        assert resp_ai_gen.status_code == 200
        data_ai_gen = resp_ai_gen.json()
        assert data_ai_gen["classLabel"] == "AI_GENERATION"
        assert data_ai_gen["confidence"] > 0.80
        assert data_ai_gen["modelVersion"] == "v1"

        # 4. Predict missing field -> 422
        resp_missing = client.post("/predict", json={"domainLength": 11})
        assert resp_missing.status_code == 422

        # 5. Predict extra field -> 422
        extra_payload = {**non_ai_payload, "unexpectedField": "test"}
        resp_extra = client.post("/predict", json=extra_payload)
        assert resp_extra.status_code == 422

        # 6. Predict out of range -> 422
        invalid_range_payload = {**non_ai_payload, "hourOfDay": 25}
        resp_range = client.post("/predict", json=invalid_range_payload)
        assert resp_range.status_code == 422

        print("All ML service integration tests passed successfully!")


if __name__ == "__main__":
    test_api()
