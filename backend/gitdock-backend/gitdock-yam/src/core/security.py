from fastapi import Request
import logging

logger = logging.getLogger(__name__)

def get_current_user_id(request: Request) -> str:
    """
    Extrait l'ID de l'utilisateur depuis les headers injectés par l'API Gateway.
    """
    user_id = request.headers.get("X-User-Id")

    if not user_id:
        logger.warning("⚠️ Requête sans X-User-Id reçue. (Bypass Gateway ?)")
        # En mode dev local, on peut retourner un ID par défaut (ex: 2 pour Ayman)
        return "2"

    return user_id