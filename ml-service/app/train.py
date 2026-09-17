"""
Training script for Shadow Sentinel classification model (v1).
Trains a RandomForestClassifier on 18 features, generates metrics, and saves artifacts.
"""

import json
import os
from datetime import datetime, timezone

import joblib
import pandas as pd
from sklearn.compose import ColumnTransformer
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import accuracy_score, classification_report, confusion_matrix
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import StandardScaler

FEATURE_ORDER = [
    "domainLength",
    "visitCount",
    "durationSeconds",
    "isKnownAiDomain",
    "hourOfDay",
    "pathDepth",
    "chatInterfacePresent",
    "promptInputPresent",
    "generateControlPresent",
    "regenerateControlPresent",
    "aiTermCount",
    "streamingOutputPresent",
    "fileUploadPresent",
    "promptSubmitCount",
    "generateClickCount",
    "pasteEventCount",
    "copyFromResponseCount",
    "typedCharCountBucket"
]

NUMERIC_COLUMNS = [
    "domainLength",
    "visitCount",
    "durationSeconds",
    "hourOfDay",
    "pathDepth",
    "aiTermCount",
    "promptSubmitCount",
    "generateClickCount",
    "pasteEventCount",
    "copyFromResponseCount",
    "typedCharCountBucket"
]

BOOLEAN_COLUMNS = [
    "isKnownAiDomain",
    "chatInterfacePresent",
    "promptInputPresent",
    "generateControlPresent",
    "regenerateControlPresent",
    "streamingOutputPresent",
    "fileUploadPresent"
]


def train():
    base_dir = os.path.dirname(os.path.dirname(__file__))
    data_path = os.path.join(base_dir, "data", "training.csv")
    models_dir = os.path.join(base_dir, "models")
    os.makedirs(models_dir, exist_ok=True)

    print(f"Loading training data from: {data_path}")
    df = pd.read_csv(data_path)

    X = df[FEATURE_ORDER]
    y = df["label"]

    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.20, stratify=y, random_state=42
    )

    print(f"Dataset split: {len(X_train)} training rows, {len(X_test)} testing rows")

    preprocessor = ColumnTransformer(
        transformers=[
            ("numeric", StandardScaler(), NUMERIC_COLUMNS),
            ("boolean", "passthrough", BOOLEAN_COLUMNS)
        ],
        verbose_feature_names_out=False
    )

    pipeline = Pipeline(
        steps=[
            ("preprocessor", preprocessor),
            (
                "classifier",
                RandomForestClassifier(
                    n_estimators=200,
                    class_weight="balanced",
                    random_state=42,
                    n_jobs=-1
                )
            )
        ]
    )

    print("Fitting RandomForest pipeline...")
    pipeline.fit(X_train, y_train)

    y_pred = pipeline.predict(X_test)
    accuracy = float(accuracy_score(y_test, y_pred))
    report_dict = classification_report(y_test, y_pred, output_dict=True)
    report_text = classification_report(y_test, y_pred)
    cm = confusion_matrix(y_test, y_pred, labels=pipeline.classes_).tolist()

    print("\n" + "=" * 50)
    print("MODEL EVALUATION RESULTS")
    print("=" * 50)
    print(f"Overall Accuracy: {accuracy:.4f}")
    print("\nClassification Report:")
    print(report_text)
    print("\nConfusion Matrix (Classes:", list(pipeline.classes_), "):")
    for row in cm:
        print(" ", row)
    print("=" * 50)

    # Save joblib artifact
    model_artifact_path = os.path.join(models_dir, "classifier-v1.joblib")
    joblib.dump(pipeline, model_artifact_path)
    print(f"\nSaved model artifact to: {model_artifact_path}")

    # Save sidecar JSON
    sidecar_data = {
        "modelVersion": "v1",
        "trainedAt": datetime.now(timezone.utc).isoformat(),
        "featureOrder": FEATURE_ORDER,
        "classes": list(pipeline.classes_),
        "metrics": {
            "accuracy": accuracy,
            "classificationReport": report_dict,
            "confusionMatrix": cm
        }
    }

    sidecar_path = os.path.join(models_dir, "classifier-v1.json")
    with open(sidecar_path, "w", encoding="utf-8") as f:
        json.dump(sidecar_data, f, indent=2)
    print(f"Saved sidecar metadata to: {sidecar_path}")


if __name__ == "__main__":
    train()
