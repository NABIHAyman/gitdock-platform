from pydantic import BaseModel, Field, field_validator
from typing import List, Literal

class Vulnerability(BaseModel):
    severity: Literal['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'] = Field(..., description="Niveau de criticité.")
    type: str = Field(..., description="Type (ex: Injection SQL, Secret en dur)")
    line_snippet: str = Field(..., description="Le code exact qui pose problème.")
    recommendation: str = Field(..., description="Comment corriger.")

    @field_validator('severity', mode='before')
    @classmethod
    def uppercase_severity(cls, v):
        return v.upper() if isinstance(v, str) else v

class SentinelAnalysisReport(BaseModel):
    is_clean: bool = Field(..., description="True si le code est sain, False s'il y a une faille.")
    vulnerabilities: List[Vulnerability] = Field(default_factory=list, description="Liste des failles.")
    summary: str = Field(..., description="Résumé de l'audit (1 phrase).")