from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI(
    title="Shadow Sentinel ML Service",
    description="Inference and analysis microservice for Shadow Sentinel",
    version="0.1.0"
)


class HealthResponse(BaseModel):
    status: str
    service: str
    version: str


@app.get("/", response_model=HealthResponse)
def root():
    return HealthResponse(
        status="ok",
        service="shadow-sentinel-ml-service",
        version="0.1.0"
    )


@app.get("/health", response_model=HealthResponse)
def health_check():
    return HealthResponse(
        status="healthy",
        service="shadow-sentinel-ml-service",
        version="0.1.0"
    )
