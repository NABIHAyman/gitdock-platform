import os
from pathlib import Path
from dotenv import load_dotenv

# Définition du chemin racine du projet (Gitdock-AI/)
BASE_DIR = Path(__file__).resolve().parent.parent.parent

# Chargement du fichier .env [cite: 7, 77]
load_dotenv(os.path.join(BASE_DIR, ".env"))

class Settings:
    PROJECT_NAME: str = "Gitdock-AI"
    VERSION: str = "1.0.0"
    
    # Configuration PostgreSQL [cite: 8, 9, 53]
    DB_USER: str = os.getenv("DB_USER", "postgres")
    DB_PASS: str = os.getenv("DB_PASS", "***REMOVED***")
    DB_HOST: str = os.getenv("DB_HOST", "localhost")
    DB_PORT: str = os.getenv("DB_PORT", "5432")
    DB_NAME: str = os.getenv("DB_NAME", "gitdock_project")
    
    DATABASE_URL: str = os.getenv(
        "DATABASE_URL", 
        f"postgresql://{DB_USER}:{DB_PASS}@{DB_HOST}:{DB_PORT}/{DB_NAME}"
    )

    # Tokens API [cite: 5, 7, 54, 77]
    GITHUB_TOKEN: str = os.getenv("GITHUB_TOKEN")
    GITLAB_TOKEN: str = os.getenv("GITLAB_TOKEN")

    # Chemins de stockage [cite: 29, 30, 45, 47]
    DATASET_PATH: str = os.path.join(BASE_DIR, os.getenv("DATASET_PATH", "data/anomaly_dataset.csv"))
    CHROMA_DB_PATH: str = os.path.join(BASE_DIR, os.getenv("CHROMA_DB_PATH", "data/chroma_db"))

    # Sécurité
    SECRET_KEY: str = os.getenv("SECRET_KEY", "fallback_secret_key_for_dev")

# Instanciation globale pour être importée ailleurs [cite: 34]
settings = Settings()