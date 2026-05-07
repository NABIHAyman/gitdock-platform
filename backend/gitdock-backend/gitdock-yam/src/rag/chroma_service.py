import os
import logging
import chromadb
from sentence_transformers import SentenceTransformer
from src.core.config import settings

logger = logging.getLogger(__name__)


class ChromaService:
    def __init__(self):
        logger.info("🔌 Connexion à ChromaDB et chargement du modèle d'Embedding...")

        self.client = None
        self.collection = None
        self.embedding_model = None
        self._ready = False  # ← flag de santé

        try:
            chroma_host = os.getenv("CHROMA_HOST", "127.0.0.1")
            chroma_port = int(os.getenv("CHROMA_PORT", "8010"))

            logger.info(f"Tentative de connexion ChromaDB sur http://{chroma_host}:{chroma_port}")

            self.client = chromadb.HttpClient(host=chroma_host, port=chroma_port)
            # Test de connectivité réel (heartbeat)
            self.client.heartbeat()

            self.collection = self.client.get_or_create_collection(
                name="gitdock_devs",
                metadata={"hnsw:space": "cosine"}
            )

            self.embedding_model = SentenceTransformer(settings.embedding_model)

            self._ready = True
            logger.info("✅ ChromaDB connecté et modèle d'Embedding chargé.")

        except Exception as e:
            logger.error(f"⚠️ ChromaDB indisponible : {e}")
            logger.warning("🟡 Le service démarre en mode dégradé (ChromaDB hors ligne).")

    def _check_ready(self):
        """Lève une exception claire si le service n'est pas initialisé."""
        if not self._ready:
            raise RuntimeError(
                "ChromaDB n'est pas disponible. "
                "Vérifiez que le conteneur Docker tourne sur le port 8000."
            )

    def upsert_developer(self, dev_id: int, profile_text: str, metadata: dict = None):
        self._check_ready()  # ← garde-fou

        vector = self.embedding_model.encode(profile_text).tolist()
        self.collection.upsert(
            ids=[str(dev_id)],
            embeddings=[vector],
            documents=[profile_text],
            metadatas=[metadata if metadata else {"source": "gitdock"}]
        )
        logger.info(f"📝 Profil dev {dev_id} vectorisé et sauvegardé.")

    def search_developers(self, query: str, n_results: int = 5) -> str:
        self._check_ready()  # ← garde-fou

        query_vector = self.embedding_model.encode(query).tolist()
        results = self.collection.query(
            query_embeddings=[query_vector],
            n_results=n_results
        )

        context = ""
        if results and results["documents"] and len(results["documents"][0]) > 0:
            for i, doc in enumerate(results["documents"][0]):
                dev_id = results["ids"][0][i]
                context += f"- Profil ID {dev_id} : {doc}\n"

        return context if context else "Aucun profil pertinent trouvé dans la base."

    def flush_database(self):
        """Détruit la collection entière et la recrée de zéro."""
        self._check_ready()
        try:
            # On supprime littéralement la table vectorielle
            self.client.delete_collection("gitdock_devs")

            # On la recrée immédiatement, toute propre
            self.collection = self.client.get_or_create_collection(
                name="gitdock_devs",
                metadata={"hnsw:space": "cosine"}
            )
            logger.info("💥 BOOM ! ChromaDB a été formaté avec succès.")
        except Exception as e:
            logger.error(f"❌ Erreur lors du flush de ChromaDB : {e}")
            raise

chroma_service = ChromaService()