import uvicorn
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from src.routes import prediction
from src.core.config import settings

app = FastAPI(
    title=settings.PROJECT_NAME,
    version=settings.VERSION,
    description="API de détection d'anomalies dans les commits Git (PFA Gitdock-AI)"
)

# Configuration CORS pour permettre à ton Frontend (Vue.js ou Streamlit) 
# de communiquer avec l'API sans être bloqué par le navigateur
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"], # En production, précise l'URL de ton front
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Inclusion des routes
app.include_router(prediction.router, prefix="/api/v1", tags=["Analyse"])

@app.get("/")
async def root():
    return {
        "message": "Bienvenue sur l'API Gitdock-AI",
        "status": "online",
        "docs": "/docs" # Lien vers la documentation interactive Swagger
    }

if __name__ == "__main__":
    # Commande pour lancer : python src/main.py
    uvicorn.run("src.main:app", host="0.0.0.0", port=8000, reload=True)