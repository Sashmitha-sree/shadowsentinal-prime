"""
Model wrapper for loading trained classifier and making inferences.
Enforces feature ordering and class probability mapping.
"""

import json
import os
from typing import Any, Dict, List, Optional
import joblib
import pandas as pd

from app.schema import ClassificationFeatures, PredictionResponse


class ModelWrapper:
    def __init__(self, model_version: str = "v1"):
        self.model_version = model_version
        self.pipeline: Optional[Any] = None
        self.feature_order: List[str] = []
        self.classes: List[str] = []
        self._is_loaded = False

    def load(self, base_path: Optional[str] = None) -> None:
        if self._is_loaded:
            return

        if base_path is None:
            base_path = os.path.dirname(os.path.dirname(__file__))

        models_dir = os.path.join(base_path, "models")
        model_path = os.path.join(models_dir, f"classifier-{self.model_version}.joblib")
        sidecar_path = os.path.join(models_dir, f"classifier-{self.model_version}.json")

        if not os.path.exists(model_path):
            raise FileNotFoundError(f"Model file not found at: {model_path}")
        if not os.path.exists(sidecar_path):
            raise FileNotFoundError(f"Sidecar metadata file not found at: {sidecar_path}")

        with open(sidecar_path, "r", encoding="utf-8") as f:
            sidecar = json.load(f)

        self.feature_order = sidecar["featureOrder"]
        self.classes = sidecar["classes"]
        self.pipeline = joblib.load(model_path)
        self._is_loaded = True
        print(f"Loaded classifier {self.model_version} with {len(self.feature_order)} features.")

    @property
    def is_loaded(self) -> bool:
        if not self._is_loaded:
            try:
                self.load()
            except Exception:
                pass
        return self._is_loaded

    def predict(self, features: ClassificationFeatures) -> PredictionResponse:
        if not self.is_loaded or self.pipeline is None:
            raise RuntimeError("Model is not loaded.")

        data_dict = features.model_dump()
        ordered_data = {col: [data_dict[col]] for col in self.feature_order}
        input_df = pd.DataFrame(ordered_data)

        probabilities_raw = self.pipeline.predict_proba(input_df)[0]
        classes_list = list(self.pipeline.classes_)

        prob_dict: Dict[str, float] = {}
        for cls_name, prob in zip(classes_list, probabilities_raw):
            prob_dict[cls_name] = round(float(prob), 4)

        best_index = int(probabilities_raw.argmax())
        best_label = classes_list[best_index]
        confidence = round(float(probabilities_raw[best_index]), 4)

        return PredictionResponse(
            classLabel=best_label,
            confidence=confidence,
            modelVersion=self.model_version,
            probabilities=prob_dict
        )


# Singleton instance
model_wrapper = ModelWrapper(model_version="v1")
