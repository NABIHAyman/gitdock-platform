# test_yam_integration.py

from fastapi.testclient import TestClient
from unittest.mock import patch, MagicMock
import pytest

# POURQUOI : On importe ton application FastAPI principale pour la tester.
# COMMENT : TestClient va envelopper cette 'app' pour simuler des requêtes HTTP sans démarrer de vrai serveur sur le port 8010.
from main import app

# POURQUOI : On importe tes schémas pour fabriquer nos fausses réponses d'IA.
from src.schemas.chat_schemas import IntentClassification, TeamRecommendation

# POURQUOI : On initialise le faux client web HTTP.
client = TestClient(app)

# POURQUOI : C'est notre test d'intégration pour la route complète de chat.
# COMMENT : On utilise @patch pour intercepter (mocker) la fonction 'execute_with_cascade' (qui appelle le LLM)
# et 'chroma_service.search_developers' (qui cherche dans la base vectorielle).
@patch("src.routes.chat.execute_with_cascade")
@patch("src.routes.chat.chroma_service.search_developers")
def test_team_builder_integration(mock_search_developers, mock_execute_cascade):
    # ==========================================
    # 1. ARRANGE : PRÉPARATION (Simulation de l'environnement)
    # ==========================================

    # POURQUOI : On simule la recherche RAG dans ChromaDB.
    # COMMENT : Quand le routeur appellera ChromaDB, il recevra ce texte instantanément.
    mock_search_developers.return_value = "Profil trouvé : Ayman, expert en Spring Boot."

    # POURQUOI : On simule l'intelligence artificielle (le LLM).
    # COMMENT : Ton code appelle l'IA DEUX fois (1. Le Routeur pour trouver l'intention, 2. L'Agent RH pour la réponse).
    # On crée donc des fausses réponses ("FakeResult") qui imitent la structure attendue par ton code.
    class FakeResult:
        def __init__(self, output):
            self.output = output

    # 1er appel simulé : L'IA détecte que l'utilisateur veut recruter une équipe ('team_builder')
    faux_resultat_routeur = FakeResult(IntentClassification(intent="team_builder", confidence=0.99))

    # 2ème appel simulé : L'IA génère sa recommandation de développeurs
    faux_resultat_rh = FakeResult(TeamRecommendation(reasoning="Je recommande Ayman pour ce projet.", team_ids=[101, 102]))

    # COMMENT : 'side_effect' permet de dire à la fausse fonction : "Au 1er appel, renvoie ça, au 2ème appel, renvoie ceci".
    mock_execute_cascade.side_effect = [faux_resultat_routeur, faux_resultat_rh]

    # POURQUOI : On prépare le JSON exact (le DTO) que le frontend enverrait via une requête POST.
    payload = {
        "question": "J'ai besoin de développeurs Java pour un nouveau service.",
        "history": [],
        "provider": "gemini"
    }

    # ==========================================
    # 2. ACT : DÉCLENCHEMENT (Traversée des couches HTTP)
    # ==========================================

    # POURQUOI : On envoie une vraie requête HTTP POST à ton API.
    # COMMENT : Le JSON traverse Pydantic (validation), puis entre dans 'build_team' dans 'chat.py'.
    response = client.post("/api/ai/team-builder", json=payload)

    # ==========================================
    # 3. ASSERT : VÉRIFICATION (Le flux de données)
    # ==========================================

    # POURQUOI : Vérifier que le serveur n'a pas crashé (Code HTTP 200 = OK).
    assert response.status_code == 200

    # POURQUOI : Extraire la réponse JSON renvoyée au frontend.
    data = response.json()

    # POURQUOI : Vérifier que la réponse finale correspond bien à ce que l'Agent RH a généré.
    # COMMENT : Cela prouve que la donnée a bien traversé le routeur HTTP et a été correctement formatée en sortie.
    assert data["answer"] == "Je recommande Ayman pour ce projet."
    assert data["suggested_dev_ids"] == [101, 102]

    # POURQUOI : S'assurer que le système RAG a bien été sollicité avec la question du client.
    mock_search_developers.assert_called_once()

    # POURQUOI : Vérifier que le processus "Cascade" a bien fait ses deux appels à l'IA (Routeur + RH).
    assert mock_execute_cascade.call_count == 2