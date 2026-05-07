from fastapi import FastAPI
import py_eureka_client.eureka_client as eureka_client
from src.core.config import settings
from contextlib import asynccontextmanager

from src.routes.chat import router as chat_router

from src.rag.chroma_service import chroma_service

from src.mongodb.chat_history_service import chat_history_service

# Gestionnaire du cycle de vie du serveur (Lifespan)
@asynccontextmanager
async def lifespan(app: FastAPI):
    # 1. Connexion MongoDB
    await chat_history_service.connect()

    # 🚀 DÉMARRAGE : Enregistrement sur Eureka
    print(f"🚀 Enregistrement de {settings.app_name} sur Eureka ({settings.eureka_server_url})...")

    try:
        await eureka_client.init_async(
            eureka_server=settings.eureka_server_url,
            app_name=settings.app_name,
            instance_port=settings.app_port,
            instance_host="127.0.0.1"
        )
        print("✅ Enregistrement Eureka réussi !")
    except Exception as e:
        print(f"⚠️ Échec de la connexion à Eureka (est-il démarré ?) : {e}")

    yield  # Le serveur FastAPI tourne ici...

    # 🛑 ARRÊT : Désinscription d'Eureka
    print(f"🛑 Désinscription de {settings.app_name} d'Eureka...")
    await eureka_client.stop_async()


# Initialisation de l'application
app = FastAPI(title="GitDock YAM (Yet Another Model)", lifespan=lifespan)

app.include_router(chat_router)

# Petite route de test
@app.get("/api/yam/ping")
async def ping():
    return {
        "status": "ok",
        "service": settings.app_name,
        "mode_simulation_actif": settings.dry_run,
        "moteur_principal": settings.default_llm_provider
    }



if __name__ == "__main__":
    import uvicorn
    uvicorn.run("src.main:app", host="0.0.0.0", port=8012, reload=True)