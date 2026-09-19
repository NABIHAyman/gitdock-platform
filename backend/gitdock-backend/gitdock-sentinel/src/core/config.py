import os
from pydantic_settings import BaseSettings
from pydantic import Field


class Settings(BaseSettings):
    app_name: str = Field(default="gitdock-sentinel", env="APP_NAME")
    app_port: int = Field(default=8010, env="APP_PORT")

    eureka_server_url: str = Field(default="http://windows-host:8761/eureka/", env="EUREKA_SERVER_URL")

    chroma_host: str = Field(default="127.0.0.1", env="CHROMA_HOST")
    chroma_port: int = Field(default=8005, env="CHROMA_PORT")

    kafka_host: str = Field(default="127.0.0.1", env="KAFKA_HOST")
    kafka_port: int = Field(default=9092, env="KAFKA_PORT")

    rabbitmq_host: str = Field(default="windows-host", env="RABBITMQ_HOST")
    rabbitmq_port: int = Field(default=5672, env="RABBITMQ_PORT")
    rabbitmq_user: str = Field(default="guest", env="RABBITMQ_USER")
    rabbitmq_password: str = Field(default="guest", env="RABBITMQ_PASSWORD")

    ollama_api_url: str = Field(default="http://ubuntu-host:11434", env="OLLAMA_API_URL")
    ollama_model: str = Field(default="gemma4:e4b", env="OLLAMA_MODEL")
    embedding_model: str = Field(default="all-MiniLM-L6-v2", env="EMBEDDING_MODEL")

    gemini_api_key: str = Field(default="", env="GEMINI_API_KEY")
    gemini_model: str = Field(default="gemini-2.5-flash", env="GEMINI_MODEL")

    class Config:
        env_file = ".env"
        env_file_encoding = 'utf-8'
        extra = 'ignore'


settings = Settings()