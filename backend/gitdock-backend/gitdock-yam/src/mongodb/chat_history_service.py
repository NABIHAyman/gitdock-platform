import logging
from datetime import datetime, timezone
from motor.motor_asyncio import AsyncIOMotorClient
from src.core.config import settings

logger = logging.getLogger(__name__)


class ChatHistoryService:
    def __init__(self):
        self.client = None
        self.db = None
        self.collection = None
        self._ready = False

    async def connect(self):
        """Initialisation asynchrone (appelée dans le lifespan de FastAPI)"""
        try:
            logger.info(f"🔌 Connexion à MongoDB sur {settings.mongo_uri}...")
            self.client = AsyncIOMotorClient(settings.mongo_uri, serverSelectionTimeoutMS=5000)
            self.db = self.client.gitdock_yam
            self.collection = self.db.chat_history

            # Création des index vitaux pour la performance
            await self.collection.create_index("session_id")
            await self.collection.create_index([("timestamp", -1)])

            self._ready = True
            logger.info("✅ MongoDB connecté et indexé pour l'historique.")
        except Exception as e:
            logger.error(f"❌ Impossible de se connecter à MongoDB : {e}")
            logger.warning("🟡 Le chat tournera sans mémoire à long terme.")

    async def add_message(self, session_id: str, role: str, content: str):
        if not self._ready: return
        try:
            message = {
                "session_id": session_id,
                "role": role,
                "content": content,
                "timestamp": datetime.now(timezone.utc)
            }
            await self.collection.insert_one(message)
        except Exception as e:
            logger.error(f"Erreur insertion MongoDB : {e}")

    async def get_history(self, session_id: str, limit: int = 10) -> list[dict]:
        if not self._ready: return []
        try:
            # Le {"_id": 0} est crucial pour éviter l'erreur de sérialisation ObjectId !
            cursor = self.collection.find({"session_id": session_id}, {"_id": 0}).sort("timestamp", -1).limit(limit)
            messages = await cursor.to_list(length=limit)
            return list(reversed(messages))
        except Exception as e:
            logger.error(f"Erreur lecture MongoDB : {e}")
            return []

    async def clear_history(self, session_id: str):
        if not self._ready: return
        await self.collection.delete_many({"session_id": session_id})


chat_history_service = ChatHistoryService()