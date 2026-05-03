import os
from pathlib import Path
from dotenv import load_dotenv

BASE_DIR = Path(__file__).resolve().parent.parent.parent
load_dotenv(os.path.join(BASE_DIR, ".env"))

class Settings:
    PROJECT_NAME: str = "Gitdock-AI"
    VERSION: str = "1.0.0"

    DB_USER:     str = os.getenv("DB_USER", "postgres")
    DB_PASSWORD: str = os.getenv("DB_PASSWORD", "postgres")
    DB_HOST:     str = os.getenv("DB_HOST", "gitdock-postgres")
    DB_PORT:     str = os.getenv("DB_PORT", "5432")
    DB_NAME:     str = os.getenv("DB_NAME", "gitdock_project")

    GITHUB_TOKEN: str = os.getenv("GITHUB_TOKEN", "")
    GITLAB_TOKEN: str = os.getenv("GITLAB_TOKEN", "")

    DATASET_PATH:  str = os.path.join(BASE_DIR, "data/anomaly_dataset.csv")
    CHROMA_DB_PATH: str = os.path.join(BASE_DIR, "data/chroma_db")

settings = Settings()