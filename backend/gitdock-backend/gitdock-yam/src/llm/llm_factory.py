import logging
import os
import httpx
from pydantic_ai.models import Model
from pydantic_ai.models.google import GoogleModel
from pydantic_ai.providers.google import GoogleProvider
from pydantic_ai.models.openai import OpenAIChatModel
from src.core.config import settings

# 🛡️ LE BOUCLIER ANTI-PROXY (Crucial pour Windows/Tailscale)
os.environ["HTTP_PROXY"] = ""
os.environ["HTTPS_PROXY"] = ""
os.environ["NO_PROXY"] = "127.0.0.1,localhost,ubuntu-host,*"

logger = logging.getLogger(__name__)


class LLMFactory:

    @staticmethod
    def _build_ollama() -> Model:
        from pydantic_ai.providers.openai import OpenAIProvider  # 👈 Import important

        clean_url = settings.ollama_api_url.replace("localhost", "127.0.0.1").rstrip('/')
        logger.info(f"🔒 Configuration Ollama via Provider sur : {clean_url}")

        # On crée le bulldozer HTTP
        custom_client = httpx.AsyncClient(verify=False, timeout=180.0)

        # On encapsule l'URL et la clé dans un Provider
        provider = OpenAIProvider(
            base_url=f"{clean_url}/v1",
            api_key="ollama-local",
            # http_client=custom_client
        )

        # On instancie le modèle avec son provider
        return OpenAIChatModel(
            settings.ollama_model,
            provider=provider
        )

    @staticmethod
    def _build_gemini() -> Model | None:
        if not settings.gemini_api_key or settings.gemini_api_key == "TaCleGeminiIci":
            return None
        logger.info(f"☁️ Configuration Gemini ({settings.gemini_model})")

        # GoogleModel (SDK google-genai) remplace GeminiModel, retiré de pydantic-ai.
        provider = GoogleProvider(api_key=settings.gemini_api_key)
        return GoogleModel(settings.gemini_model, provider=provider)

    @staticmethod
    def get_models(user_preference: str = None) -> list[Model]:
        # Force l'ordre indépendamment des préférences pour tes tests
        models = []

        # 🚀 1. OLLAMA TOUJOURS EN PREMIER
        ollama = LLMFactory._build_ollama()
        models.append(ollama)

        # ☁️ 2. GEMINI UNIQUEMENT EN FALLBACK
        gemini = LLMFactory._build_gemini()
        if gemini:
            models.append(gemini)

        # Log pour debug interne (tu verras ça dans ta console)
        print(f"🧬 Cascade ordonnée : {[m.__class__.__name__ for m in models]}")
        return models
