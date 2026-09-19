import logging
import chromadb
from sentence_transformers import SentenceTransformer
from src.core.config import settings

logger = logging.getLogger(__name__)

class ChromaService:
    def __init__(self):
        logger.info(f"🔌 Connexion ChromaDB ({settings.chroma_host}:{settings.chroma_port})...")
        self.client = None
        self.code_collection = None
        self.embedding_model = None
        self._ready = False

        try:
            self.client = chromadb.HttpClient(host=settings.chroma_host, port=settings.chroma_port)
            self.client.heartbeat()

            # UNIQUEMENT le code (Sentinel)
            self.code_collection = self.client.get_or_create_collection(
                name="gitdock_code_chunks",
                metadata={"hnsw:space": "cosine"}
            )

            logger.info(f"🧠 Chargement du modèle : {settings.embedding_model}...")
            self.embedding_model = SentenceTransformer(settings.embedding_model)

            self._ready = True
            logger.info("✅ ChromaDB connecté et modèle chargé.")

        except Exception as e:
            logger.error(f"⚠️ Erreur ChromaDB : {e}")

    def _check_ready(self):
        if not self._ready:
            raise RuntimeError("ChromaDB n'est pas disponible.")

    def upsert_code_chunk(self, commit_hash: str, project_id: str, code_diff: str):
        self._check_ready()
        vector = self.embedding_model.encode(code_diff).tolist()
        self.code_collection.upsert(
            ids=[commit_hash],
            embeddings=[vector],
            documents=[code_diff],
            metadatas=[{"project_id": str(project_id), "type": "commit_diff"}]
        )
        logger.info(f"🛡️ Code commit {commit_hash[:7]} vectorisé et stocké.")

    def flush_database(self):
        self._check_ready()
        self.client.delete_collection("gitdock_code_chunks")
        self.code_collection = self.client.get_or_create_collection("gitdock_code_chunks", metadata={"hnsw:space": "cosine"})
        logger.info("💥 Base vectorielle réinitialisée.")

chroma_service = ChromaService()