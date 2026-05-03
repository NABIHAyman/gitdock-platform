import os
from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    app_name: str = os.getenv("APP_NAME", "gitdock-yam")
    app_port: int = int(os.getenv("APP_PORT", 8010))

    # URL de ton serveur Spring Eureka
    eureka_server_url: str = os.getenv("EUREKA_SERVER_URL", "http://localhost:8761/eureka/")

    # Paramètres du Double Moteur
    default_llm_provider: str = os.getenv("DEFAULT_LLM_PROVIDER", "gemini")
    #gemini_api_key: str = os.getenv("GEMINI_API_KEY", "YourApiKey")
    #gemini_model: str = os.getenv("GEMINI_MODEL", "gemini-2.5-flash-lite")
    gemini_api_key: str = ""
    gemini_model: str = "gemini-2.5-flash"

    #ollama_api_url: str = os.getenv("OLLAMA_API_URL", "http://localhost:11434")
    #ollama_model: str = os.getenv("OLLAMA_MODEL", "gemma4:e4b")
    ollama_api_url: str = "http://ubuntu-host:11434"
    ollama_model: str = "gemma4:e4b"
    embedding_model: str = os.getenv("EMBEDDING_MODEL", "all-MiniLM-L6-v2") # "jina-embeddings-v5-text-nano"
    # hf_token: str = os.getenv("HF_TOKEN", "")

    # Mode Simulation (DRY_RUN)
    dry_run: bool = str(os.getenv("DRY_RUN", "True")).lower() in ("true", "1", "t")

    class Config:
        env_file = ".env"
        env_file_encoding = 'utf-8'

settings = Settings()