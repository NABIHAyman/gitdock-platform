from fastapi import FastAPI
from fastapi.testclient import TestClient
from unittest.mock import patch, AsyncMock
from src.routes.chat import router
from src.schemas.chat_schemas import IntentClassification, TeamRecommendation

# -----------------------------------------------------------------
# 1. CONFIGURATION DE L'APPLICATION DE TEST
# -----------------------------------------------------------------
app = FastAPI()
app.include_router(router) # On attache ton routeur à notre fausse app
client = TestClient(app)   # C'est l'équivalent de MockMvc en Java !

# -----------------------------------------------------------------
# 2. LE TEST DU FLUX RAG (Multi-Agent)
# -----------------------------------------------------------------
@patch("src.routes.chat.LLMFactory.get_models")
@patch("src.routes.chat.chroma_service.search_developers")
@patch("src.routes.chat.execute_with_cascade", new_callable=AsyncMock) # AsyncMock car ta fonction est async !
def test_team_builder_flow(mock_cascade, mock_search, mock_get_models):

    # --- ARRANGE (Préparation des doublures) ---
    mock_get_models.return_value = ["fake-model-1"]
    mock_search.return_value = "Profil 101: Experte Java Spring."

    # On recrée la structure d'objet attendue par ton code
    class FakeResult:
        def __init__(self, output):
            self.output = output

    # 🌟 LA MAGIE EST ICI : side_effect permet de définir plusieurs réponses différentes
    # pour les appels successifs à la même fonction.
    mock_cascade.side_effect = [
        # Appel 1 (Agent Routeur) : Il décide que c'est une demande RH
        FakeResult(IntentClassification(intent="team_builder", confidence=0.99)),
        # Appel 2 (Agent RH) : Il fait sa recommandation
        FakeResult(TeamRecommendation(reasoning="Je vous conseille Amal pour le Java.", team_ids=[101]))
    ]

    # On prépare le faux JSON que l'utilisateur taperait sur son clavier
    payload = {
        "question": "J'ai besoin d'une experte en Java",
        "history": [],
        "provider": "google"
    }

    # --- ACT (On simule le POST HTTP) ---
    response = client.post("/api/ai/team-builder", json=payload)

    # --- ASSERT (Les vérifications) ---
    # 1. L'API doit répondre 200 OK
    assert response.status_code == 200

    data = response.json()

    # 2. On vérifie que la réponse envoyée au Frontend est la bonne
    assert data["answer"] == "Je vous conseille Amal pour le Java."
    assert data["suggested_dev_ids"] == [101]

    # 3. On prouve au jury que le "Routing" a bien fonctionné
    assert mock_cascade.call_count == 2 # 1 fois pour l'intention, 1 fois pour la réponse finale
    mock_search.assert_called_once()    # La base vectorielle a bien été sollicitée
    @patch("src.routes.chat.LLMFactory.get_models")
    @patch("src.routes.chat.execute_with_cascade", new_callable=AsyncMock)
    async def test_chat_unknown_intent(mock_cascade, mock_get_models):
        """
        TEST DE SÉCURITÉ : Vérifie que le système gère proprement une question hors-sujet.
        """
        # --- ARRANGE ---
        mock_get_models.return_value = ["fake-model-1"]

        class FakeResult:
            def __init__(self, output):
                self.output = output

        # On simule un seul appel : le routeur qui dit "Je ne connais pas cette intention"
        mock_cascade.side_effect = [
            FakeResult(IntentClassification(intent="unknown", confidence=1.0))
        ]

        # --- ACT ---
        async with AsyncClient(app=app, base_url="http://test") as ac:
            response = await ac.post("/api/v1/chat/build-team", json={
                "message": "Quel est le score du match de foot ?"
            })

        # --- ASSERT ---
        assert response.status_code == 200
        data = response.json()
        # On vérifie que le système renvoie un message d'impuissance poli
        assert "désolé" in data["answer"].lower() or "pas en mesure" in data["answer"].lower()
        # On vérifie que le cascade n'a été appelé QU'UNE SEULE FOIS (le routeur a stoppé le flux)
        assert mock_cascade.call_count == 1