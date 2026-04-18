import tiktoken
import traceback
import json
from fastapi import APIRouter, HTTPException
from pydantic_ai import Agent
from src.schemas.chat_schemas import ChatRequest, ChatResponse, TeamRecommendation, IntentClassification
from src.core.config import settings
from src.llm.llm_factory import LLMFactory
from src.rag.chroma_service import chroma_service

router = APIRouter(prefix="/api/ai", tags=["Chat & RAG"])

def safe_estimate_tokens(text: str) -> int:
    if not text or not isinstance(text, str): return 0
    try:
        return len(tiktoken.get_encoding("cl100k_base").encode(text))
    except:
        return 0

# 🌟 LA FONCTION DE CLAUDE POUR EXTRAIRE LE TEXTE (VITALE !)
def extract_text_from_result(result) -> str:
    """Extrait le texte de la réponse du LLM, peu importe la version de PydanticAI."""
    try:
        # Version moderne (v1.0+)
        if hasattr(result, 'output'):
            return str(result.output)
        # Version legacy
        elif hasattr(result, 'data'):
            return str(result.data)
        # Fallback: historique des messages
        elif hasattr(result, 'all_messages'):
            messages = result.all_messages()
            if messages:
                last_msg = messages[-1]
                if hasattr(last_msg, 'parts'):
                    for part in last_msg.parts:
                        if hasattr(part, 'content'):
                            return str(part.content)
                        elif hasattr(part, 'text'):
                            return str(part.text)
    except Exception as e:
        print(f"⚠️ Erreur d'extraction : {e}")
    return str(result)


async def execute_with_cascade(models: list, system_prompt: str, prompt: str, output_type=None):
    if output_type:
        schema = output_type.model_json_schema()
        props = schema.get('properties', {})

        # 🧠 LA RUSE : On fabrique un exemple JSON ultra simple pour le modèle
        example = {}
        for k, v in props.items():
            if 'enum' in v:
                example[k] = v['enum'][0]  # On montre une des valeurs possibles
            elif v.get('type') == 'array':
                example[k] = [1, 2]  # Un exemple de tableau
            elif v.get('type') in ['number', 'integer']:
                example[k] = 0.95
            else:
                example[k] = "ton texte ici"

        system_prompt += (
            f"\n\n--- INSTRUCTION DE FORMATAGE OBLIGATOIRE ---"
            f"\nTu es un système automatisé. Tu dois générer un objet JSON valide."
            f"\nINTERDICTION ABSOLUE de recopier un schéma (pas de mots comme 'properties' ou 'type: object')."
            f"\nRemplis ce template avec tes propres résultats. Voici l'EXEMPLE EXACT de la structure attendue :\n"
            f"{json.dumps(example, indent=2)}"
        )

    for idx, model in enumerate(models):
        try:
            print(f"🚀 [CASCADE] Tentative {idx + 1}/{len(models)}...")

            agent = Agent(model, system_prompt=system_prompt, retries=1)
            result = await agent.run(prompt)

            if output_type:
                try:
                    # On utilise la fonction de Claude ici
                    raw_text = extract_text_from_result(result)

                    raw_text = raw_text.replace('```json', '').replace('```', '').strip()
                    if not raw_text.startswith('{'):
                        raw_text = '{' + raw_text.split('{', 1)[1]
                    if not raw_text.endswith('}'):
                        raw_text = raw_text.rsplit('}', 1)[0] + '}'

                    parsed_json = json.loads(raw_text)

                    validated_data = output_type.model_validate(parsed_json)

                    class FakeResult:
                        def __init__(self, data):
                            self.output = data

                    return FakeResult(validated_data)

                except Exception as parse_error:
                    print(f"⚠️ [JSON PARSE ERROR] Modèle a mal formaté le JSON : {parse_error}")
                    try:
                        print(f"🔍 TEXTE BRUT REÇU : {extract_text_from_result(result)}")
                    except:
                        pass
                    raise parse_error

            # Si pas de output_type, on s'assure de renvoyer un objet avec .output
            class TextResult:
                def __init__(self, text):
                    self.output = text

            return TextResult(extract_text_from_result(result))

        except Exception as e:
            print(f"⚠️ [CASCADE] Échec : {str(e)}")
            import traceback
            traceback.print_exc()

    raise HTTPException(status_code=500, detail="Tous les modèles ont échoué. Regarde la console Python.")


# ==============================================================================
# 🗄️ ENDPOINTS CHROMA DB
# ==============================================================================

@router.post("/seed-database", summary="Injecte le dataset JSON dans ChromaDB")
async def seed_database():
    try:
        with open("data/seeds.json", "r", encoding="utf-8") as f:
            devs = json.load(f)

        count = 0
        for dev in devs:
            chroma_service.upsert_developer(dev["id"], dev["text"])
            count += 1

        return {"status": f"Succès ! {count} profils ont été vectorisés et injectés dans ChromaDB."}
    except FileNotFoundError:
        raise HTTPException(status_code=404, detail="Le fichier data/seeds.json est introuvable.")
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Erreur d'ingestion : {str(e)}")

@router.get("/debug-chroma", summary="Affiche TOUT le contenu actuel de ChromaDB")
async def debug_chroma():
    try:
        all_data = chroma_service.collection.get()

        return {
            "total_documents_en_base": len(all_data['ids']) if all_data['ids'] else 0,
            "ids_presents": all_data['ids'],
            "documents_sauvegardes": all_data['documents']
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Erreur de lecture ChromaDB : {str(e)}")

@router.delete("/flush-database", summary="⚠️ DANGER : Vide intégralement ChromaDB")
async def flush_chroma():
    try:
        chroma_service.flush_database()
        return {"status": "💥 Base vectorielle formatée avec succès ! Prêt pour un nouveau Seed."}
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Erreur de formatage : {str(e)}")


# ==============================================================================
# 🤖 ENDPOINT IA
# ==============================================================================

@router.post("/team-builder", response_model=ChatResponse)
async def build_team(request: ChatRequest):
    # 1. Compilation de la mémoire
    memory_string = ""
    if request.history:
        for msg in request.history:
            prefix = "Manager" if msg.role == 'user' else "GitDock-YAM"
            memory_string += f"{prefix}: {msg.text}\n"

    full_query_context = f"""
    --- HISTORIQUE ---
    {memory_string}
    --- FIN HISTORIQUE ---
    DERNIÈRE QUESTION : {request.question}
    """

    models = LLMFactory.get_models(request.provider)

    try:
        # 🚦 AGENT 1 : LE ROUTEUR
        router_prompt = """Tu es le superviseur de GitDock. 
        Classe la demande :
        - 'team_builder' : Recherche développeurs, équipe, compétences.
        - 'analytics' : État des tâches, retards, gestion de projet.
        - 'general_chat' : Politesse ou questions hors contexte."""

        router_result = await execute_with_cascade(models, router_prompt, full_query_context, IntentClassification)
        intent = router_result.output.intent

        total_tokens = safe_estimate_tokens(router_prompt + full_query_context)

        # 👔 FLUX RH
        if intent == 'team_builder':
            real_context = chroma_service.search_developers(f"{memory_string} {request.question}")
            hr_prompt = (f"Tu es GitDock-YAM, expert RH. Réponds à la PREMIÈRE PERSONNE ('Je'). "
                         f"Sois bref, technique et efficace. Pas de blabla inutile.Contexte : {real_context}")

            result = await execute_with_cascade(models, hr_prompt, full_query_context, TeamRecommendation)
            total_tokens += safe_estimate_tokens(hr_prompt + result.output.reasoning)

            return ChatResponse(
                answer=result.output.reasoning,
                suggested_dev_ids=result.output.team_ids,
                is_dry_run=False,
                estimated_tokens=total_tokens
            )

        # 📊 FLUX ANALYTICS
        elif intent == 'analytics':
            mock_data = "Tâche #45 en retard de 3 jours pour Maryam. Ayman est OK sur la Saga."
            analytics_prompt = f"Expert Analytics. Stats réelles : {mock_data}"

            result = await execute_with_cascade(models, analytics_prompt, full_query_context)
            # 🌟 CORRECTION: On utilise result.output au lieu de result.data
            return ChatResponse(answer=result.output, suggested_dev_ids=[], is_dry_run=False)

        # 💬 FLUX GÉNÉRAL
        else:
            chat_prompt = ("Tu es GitDock-YAM. Réponds toujours à la première personne de manière amicale."
                           "Sois bref, technique et efficace. Pas de blabla inutile.")
            result = await execute_with_cascade(models, chat_prompt, full_query_context)
            # 🌟 CORRECTION: On utilise result.output au lieu de result.data
            return ChatResponse(answer=result.output, suggested_dev_ids=[], is_dry_run=False)

    except HTTPException as http_e:
        raise http_e
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))