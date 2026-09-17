"""
Shadow Sentinel ML Service API.
Serves classification inference based on the frozen 18-signal feature contract.
"""

from contextlib import asynccontextmanager
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware

from app.model import model_wrapper
from app.schema import ClassificationFeatures, HealthResponse, PredictionResponse


@asynccontextmanager
async def lifespan(app: FastAPI):
    # Load model once at startup
    try:
        model_wrapper.load()
    except Exception as e:
        print(f"Warning: Failed to load model at startup: {e}")
    yield


app = FastAPI(
    title="Shadow Sentinel Classification Service",
    description="Inference microservice for shadow AI activity classification",
    version="1.0.0",
    lifespan=lifespan
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"]
)


@app.get("/health", response_model=HealthResponse)
def health():
    return HealthResponse(
        status="healthy" if model_wrapper.is_loaded else "unhealthy",
        modelVersion=model_wrapper.model_version
    )


@app.post("/predict", response_model=PredictionResponse)
def predict(features: ClassificationFeatures):
    if not model_wrapper.is_loaded:
        raise HTTPException(status_code=503, detail="Model is not loaded or unavailable")

    return model_wrapper.predict(features)
