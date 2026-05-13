import os
from dotenv import load_dotenv
from pydantic_settings import BaseSettings

load_dotenv()

class Settings(BaseSettings):
    app_name: str = os.getenv("APP_NAME", "gitdock-yam")
    # app_port: int = int(os.getenv("APP_PORT", 8010))
    app_port: int = int(os.getenv("APP_PORT", 8012)) # <-- CORRIGÉ de 8010 à 8012

    mongo_uri: str = os.getenv("MONGO_URI", "mongodb://127.0.0.1:27017")

    chroma_host: str = os.getenv("CHROMA_HOST", "localhost")
    chroma_port: int = int(os.getenv("CHROMA_PORT", 8010))

    # URL de ton serveur Spring Eureka
    eureka_server_url: str = os.getenv("EUREKA_SERVER_URL", "http://localhost:8761/eureka/")

    rabbitmq_host: str = os.getenv("RABBITMQ_HOST", "localhost")
    rabbitmq_port: int = int(os.getenv("RABBITMQ_PORT", 5672))
    rabbitmq_user: str = os.getenv("RABBITMQ_USER", "guest")
    rabbitmq_password: str = os.getenv("RABBITMQ_PASSWORD", "guest")

    # Paramètres du Double Moteur
    default_llm_provider: str = os.getenv("DEFAULT_LLM_PROVIDER", "gemini")

    # ✅ FIX : lecture depuis .env au lieu de valeurs hardcodées
    gemini_api_key: str = os.getenv("GEMINI_API_KEY", "")
    gemini_model: str = os.getenv("GEMINI_MODEL", "gemini-2.5-flash")

    # ✅ FIX : plus d'IP Tailscale hardcodée
    ollama_api_url: str = os.getenv("OLLAMA_API_URL", "http://localhost:11434")
    ollama_model: str = os.getenv("OLLAMA_MODEL", "gemma4:e4b")

    embedding_model: str = os.getenv("EMBEDDING_MODEL", "all-MiniLM-L6-v2")

    hf_token: str = os.getenv("HF_TOKEN")

    # Mode Simulation (DRY_RUN)
    dry_run: bool = str(os.getenv("DRY_RUN", "True")).lower() in ("true", "1", "t")

    class Config:
        env_file = ".env"
        env_file_encoding = 'utf-8'
        extra = 'ignore'

settings = Settings()
