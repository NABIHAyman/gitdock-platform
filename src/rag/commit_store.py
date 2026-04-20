import chromadb
from sentence_transformers import SentenceTransformer

class CommitStore:
    def __init__(self):
        self.client = chromadb.PersistentClient(path="data/chroma_db")
        self.collection = self.client.get_or_create_collection("git_memory")
        self.model = SentenceTransformer('all-MiniLM-L6-v2')

    def store_commits(self, df):
        for _, row in df.iterrows():
            embedding = self.model.encode(row['message']).tolist()
            self.collection.upsert(
                ids=[row['sha']],
                embeddings=[embedding],
                metadatas=[{"status": row['status'], "project": row['project']}],
                documents=[row['message']]
            )