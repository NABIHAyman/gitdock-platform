import os
from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    app_name: str = os.getenv("APP_NAME", "gitdock-sentinel")
    app_port: int = int(os.getenv("APP_PORT", 8010))

    eureka_server_url: str = os.getenv("EUREKA_SERVER_URL", "http://windows-host:8761/eureka/")

    # ChromaDB (Local Ubuntu)
    chroma_host: str = os.getenv("CHROMA_HOST", "127.0.0.1")
    chroma_port: int = int(os.getenv("CHROMA_PORT", 8005))

    # Kafka (Local Ubuntu)
    kafka_host: str = os.getenv("KAFKA_HOST", "127.0.0.1")
    kafka_port: int = int(os.getenv("KAFKA_PORT", 9092))

    # RabbitMQ (Distant Windows)
    rabbitmq_host: str = os.getenv("RABBITMQ_HOST", "windows-host")
    rabbitmq_port: int = int(os.getenv("RABBITMQ_PORT", 5672))
    rabbitmq_user: str = os.getenv("RABBITMQ_USER", "guest")
    rabbitmq_password: str = os.getenv("RABBITMQ_PASSWORD", "guest")

    # LLM & Embeddings
    ollama_api_url: str = os.getenv("OLLAMA_API_URL", "http://ubuntu-host:11434")
    ollama_model: str = os.getenv("OLLAMA_MODEL", "gemma4:e4b")
    embedding_model: str = os.getenv("EMBEDDING_MODEL", "all-MiniLM-L6-v2")

    gemini_api_key: str = os.getenv("GEMINI_API_KEY", "")
    gemini_model: str = os.getenv("GEMINI_MODEL", "gemini-2.5-flash")

    dry_run: bool = str(os.getenv("DRY_RUN", "True")).lower() in ("true", "1", "t")

    class Config:
        env_file = ".env"
        env_file_encoding = 'utf-8'
        extra = 'ignore'

settings = Settings()