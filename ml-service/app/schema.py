"""
Pydantic schemas for the Shadow Sentinel ML service.
Strictly enforces the frozen 18-feature contract from docs/feature-schema.md.
"""

from typing import Dict
from pydantic import BaseModel, ConfigDict, Field


class ClassificationFeatures(BaseModel):
    model_config = ConfigDict(extra="forbid")

    # -- metadata signals (6)
    domainLength: int = Field(..., ge=0, description="Domain character length")
    visitCount: int = Field(..., ge=0, description="Visit frequency count")
    durationSeconds: int = Field(..., ge=0, description="Active engagement time in seconds")
    isKnownAiDomain: bool = Field(..., description="Whether domain is in known AI directory")
    hourOfDay: int = Field(..., ge=0, le=23, description="Access hour of day (0-23)")
    pathDepth: int = Field(..., ge=0, description="URL path segment depth")

    # -- UI signals (7)
    chatInterfacePresent: bool = Field(..., description="Presence of conversational interface")
    promptInputPresent: bool = Field(..., description="Presence of prompt input or textarea")
    generateControlPresent: bool = Field(..., description="Presence of generate/send button")
    regenerateControlPresent: bool = Field(..., description="Presence of regenerate control")
    aiTermCount: int = Field(..., ge=0, description="Count of AI keywords in UI labels")
    streamingOutputPresent: bool = Field(..., description="Presence of streaming token DOM stream")
    fileUploadPresent: bool = Field(..., description="Presence of file attachment input/control")

    # -- interaction signals (5)
    promptSubmitCount: int = Field(..., ge=0, description="Count of prompt submit events")
    generateClickCount: int = Field(..., ge=0, description="Count of generate button clicks")
    pasteEventCount: int = Field(..., ge=0, description="Count of paste events into inputs")
    copyFromResponseCount: int = Field(..., ge=0, description="Count of copy events from responses")
    typedCharCountBucket: int = Field(..., ge=0, le=4, description="Bucketed typing volume (0-4)")


class PredictionResponse(BaseModel):
    classLabel: str
    confidence: float
    modelVersion: str
    probabilities: Dict[str, float]


class HealthResponse(BaseModel):
    status: str
    modelVersion: str
