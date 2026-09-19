from fastapi import FastAPI, WebSocket, WebSocketDisconnect
from fastapi.middleware.cors import CORSMiddleware
from contextlib import asynccontextmanager
import logging
import asyncio
import subprocess
import threading
import sys
import os

from src.core.config import settings
from src.messaging.kafka_consumer import sentinel_consumer
from src.rag.chroma_service import chroma_service
from src.messaging.websocket_manager import ws_manager
import py_eureka_client.eureka_client as eureka_client

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Variable globale pour la boucle d'événements (Fix Claude Bug #3)
main_loop = None


@asynccontextmanager
async def lifespan(app: FastAPI):
    global main_loop
    main_loop = asyncio.get_running_loop()

    # --- 🗺️ ENREGISTREMENT EUREKA ---
    logger.info(f"📡 Enregistrement sur Eureka : {settings.eureka_server_url}")
    try:
        await eureka_client.init_async(
            eureka_server=settings.eureka_server_url,
            app_name=settings.app_name,
            instance_port=settings.app_port,
            instance_host="ubuntu-host" # Ton IP Ubuntu
        )
    except Exception as e:
        logger.error(f"⚠️ Eureka non joint : {e}")

    # --- 🛡️ DÉMARRAGE DU CONSUMER KAFKA ---
    sentinel_consumer.start()
    yield

    logger.info("🛑 Fermeture de GitDock Sentinel...")
    sentinel_consumer.stop()
    await eureka_client.stop_async()

app = FastAPI(title="GitDock Sentinel", lifespan=lifespan)

app.add_middleware(
    CORSMiddleware, allow_origins=["*"], allow_credentials=True, allow_methods=["*"], allow_headers=["*"]
)


@app.get("/api/sentinel/health")
async def health():
    return {
        "status": "online",
        "kafka_listening": not sentinel_consumer._stop_event.is_set(),
        "main_loop_active": main_loop is not None
    }


@app.delete("/api/sentinel/flush-chroma")
async def flush_db():
    chroma_service.flush_database()
    return {"status": "Base vectorielle réinitialisée."}


@app.websocket("/api/ws/sentinel")
async def websocket_sentinel_endpoint(websocket: WebSocket):
    await ws_manager.connect(websocket)
    try:
        while True:
            data = await websocket.receive_text()
            if data == "ping":
                await websocket.send_text("pong")
    except WebSocketDisconnect:
        ws_manager.disconnect(websocket)


if __name__ == "__main__":
    import uvicorn

    # On utilise settings.app_port (8010)
    uvicorn.run("src.main:app", host="0.0.0.0", port=settings.app_port, reload=True)