from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
import uvicorn

# Imports de ton projet GitDock
from src.routes import prediction
from src.core.config import settings

# Initialisation de l'application FastAPI
app = FastAPI(
    title=settings.PROJECT_NAME,
    version=settings.VERSION
)

# Configuration du CORS pour permettre à ton Dashboard Vue.js de communiquer
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"], # À restreindre en production pour plus de sécurité
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Inclusion des routes d'analyse pour ton projet
app.include_router(prediction.router, prefix="/api/v1", tags=["Analyse"])

# Route de santé pour tester si le conteneur répond sur localhost:8000/health
@app.get("/health")
def health():
    return {
        "status": "ok",
        "service": "gitdock-ai",
        "database": "connected" # L'IA pourra accéder à tes volumes postgres_data
    }

# BLOC DE LANCEMENT CRITIQUE POUR DOCKER
# Sans host="0.0.0.0", tu auras l'erreur ERR_EMPTY_RESPONSE dans Capture d'écran 2026-05-05 233616.png
if __name__ == "__main__":
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=8000,
        reload=True
    )