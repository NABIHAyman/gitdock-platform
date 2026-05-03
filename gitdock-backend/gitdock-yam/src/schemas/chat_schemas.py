from pydantic import BaseModel, Field
from typing import List, Optional, Literal # 👈 Import vital ajouté

class ChatMessage(BaseModel):
    role: Literal['user', 'ai']
    text: str

class ChatRequest(BaseModel):
    question: str = Field(...)
    history: List[ChatMessage] = Field(default_factory=list)
    project_id: Optional[int] = Field(None)
    provider: Optional[str] = Field("gemini")

# --- 🚦 Decision du Routeur ---
class IntentClassification(BaseModel):
    intent: Literal['team_builder', 'analytics', 'general_chat'] = Field(
        ...,
        description="L'intention détectée."
    )
    confidence: float = Field(..., description="Indice de confiance (0-1)")

# --- Format interne de l'Agent RH ---
class TeamRecommendation(BaseModel):
    reasoning: str = Field(..., description="L'explication détaillée.")
    team_ids: List[int] = Field(default_factory=list, description="IDs des développeurs.")

# --- Réponse finale pour Vue.js ---
class ChatResponse(BaseModel):
    answer: str
    suggested_dev_ids: List[int]
    is_dry_run: bool
    estimated_tokens: int = 0

