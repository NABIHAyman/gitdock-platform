import chromadb
from sentence_transformers import SentenceTransformer
from src.core.config import settings

class CommitStore:
    def __init__(self):
        self.client = chromadb.PersistentClient(path=settings.CHROMA_DB_PATH)
        self.collection = self.client.get_or_create_collection("git_memory")
        self.model = SentenceTransformer('all-MiniLM-L6-v2')

    def store_commits(self, df):
        for _, row in df.iterrows():
            embedding = self.model.encode(row['message']).tolist()
            self.collection.upsert(
                ids=[str(row['sha'])],
                embeddings=[embedding],
                metadatas=[{"status": row['status'], "project": row['project']}],
                documents=[row['message']]
            )