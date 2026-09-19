#Ce test unitaire vérifie le fonctionnement de la recherche de développeurs
#dans une base de données vectorielle (ChromaDB)
#sans avoir besoin d'allumer de vrais serveurs ou d'utiliser une connexion internet (utiliser patch)
import pytest
from unittest.mock import MagicMock, patch
# On importe ma classe depuis mon fichier (le chemin exact dépend de mon arborescence)
from src.rag.chroma_service import ChromaService

#  @patch est l'équivalent parfait de @MockBean en Java
# On bloque la connexion au vrai ChromaDB et au vrai modèle IA.
@patch("src.rag.chroma_service.chromadb.HttpClient")
@patch("src.rag.chroma_service.SentenceTransformer")
def test_search_developers_success(mock_sentence_transformer, mock_http_client):

    # -----------------------------------------------------------------
    # 1. PRÉPARATION (ARRANGE)
    # -----------------------------------------------------------------
    # On fabrique notre doublure pour le modèle IA (il renverra un faux vecteur)
    mock_model_instance = MagicMock()
    # Après (Option A : simuler un objet avec .tolist())
    mock_model_instance.encode.return_value = MagicMock()
    mock_model_instance.encode.return_value.tolist.return_value = [0.1, 0.2, 0.3]
    mock_sentence_transformer.return_value = mock_model_instance

    # On fabrique notre doublure pour ChromaDB (elle renverra de faux développeurs)
    mock_collection = MagicMock()
    mock_collection.query.return_value = {
        "ids": [["101", "102"]],
        "documents": [["Amal : Experte Java Spring", "youssef : Pro React JS"]]
    }

    mock_client_instance = MagicMock()
    mock_client_instance.get_or_create_collection.return_value = mock_collection
    mock_http_client.return_value = mock_client_instance

    # On instancie mon service (il utilisera les doublures automatiquement grâce aux @patch)
    service = ChromaService()

    # -----------------------------------------------------------------
    # 2. EXÉCUTION (ACT)
    # -----------------------------------------------------------------
    # On simule une question posée par un utilisateur
    result = service.search_developers("Qui connait Java ?")

    # -----------------------------------------------------------------
    # 3. VÉRIFICATION (ASSERT)
    # -----------------------------------------------------------------
    # On vérifie que mon code a bien formaté la réponse comme prévu
    assert "Profil ID 101 : Amal : Experte Java Spring" in result
    assert "Profil ID 102 : youssef : Pro React JS" in result

    # On vérifie que la question a bien été traduite en vecteur mathématique (1 seule fois)
    mock_model_instance.encode.assert_called_once_with("Qui connait Java ?")