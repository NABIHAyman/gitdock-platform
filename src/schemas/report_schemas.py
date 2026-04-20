from pydantic import BaseModel, HttpUrl
from typing import List, Optional

# Ce que le manager envoie à l'API
class RepoRequest(BaseModel):
    repo_url: str  # Exemple: "https://github.com/user/repo"

# Détail pour chaque commit analysé
class AnomalyResult(BaseModel):
    sha: str
    is_anomaly: bool
    confidence_score: float
    message: str
    reason: str

# Ce que l'API renvoie au manager
class PredictionResponse(BaseModel):
    repo: str
    total_analyzed: int
    anomalies_detected: int
    results: List[AnomalyResult]