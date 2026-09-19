from pydantic import BaseModel
from typing import List, Optional

class RepoRequest(BaseModel):
    repo_url: str

class AnomalyResult(BaseModel):
    sha: str
    is_anomaly: bool
    confidence_score: float
    message: str
    reason: str

class PredictionResponse(BaseModel):
    repo: str
    total_analyzed: int
    anomalies_detected: int
    results: List[AnomalyResult]