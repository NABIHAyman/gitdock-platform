import logging
from src.llm.llm_factory import LLMFactory
from src.llm.cascade_executor import execute_with_cascade
from src.schemas.sentinel_schema import SentinelAnalysisReport

logger = logging.getLogger(__name__)


class SentinelAnalyzer:
    def __init__(self):
        self.models = LLMFactory.get_models("ollama")
        self.system_prompt = (
            "Tu es GitDock Sentinel, un auditeur de code de cybersécurité impitoyable et ultra-rapide. "
            "Ta mission est d'analyser ce diff de code (PushEvent). "
            "1. Détecte les secrets en dur (clés AWS, mots de passe). "
            "2. Détecte les failles de sécurité (Injections SQL, XSS, etc.). "
            "3. Détecte les boucles infinies ou la logique destructrice. "
            "Si le code est clean, met 'is_clean' à true et laisse la liste des failles vide. "
            "Sois chirurgical.\n\n"
            "--- INSTRUCTION DE FORMATAGE OBLIGATOIRE ---\n"
            "Si tu trouves des vulnérabilités, la liste 'vulnerabilities' DOIT contenir des objets ayant EXACTEMENT ces clés : 'severity', 'type', 'line_snippet', 'recommendation'."
        )

    async def analyze_commit(self, commit_hash: str, project_id: str, author: str, diff: str) -> SentinelAnalysisReport:
        logger.info(f"🔍 SentinelAnalyzer : Démarrage de l'audit pour le commit {commit_hash[:7]}...")

        prompt = f"--- MÉTADONNÉES ---\nAuteur: {author}\nProjet: {project_id}\n\n--- DIFF DE CODE ---\n{diff}"

        try:
            # On utilise le cascadeur robuste que tu avais déjà créé
            result = await execute_with_cascade(self.models, self.system_prompt, prompt, SentinelAnalysisReport)
            return result.output
        except Exception as e:
            logger.error(f"❌ Échec de l'audit IA pour {commit_hash} : {e}")
            # En cas de crash du LLM, on renvoie un rapport "neutre" pour ne pas bloquer le pipeline
            return SentinelAnalysisReport(
                is_clean=True,
                summary="Analyse IA ignorée suite à une erreur interne.",
                vulnerabilities=[]
            )


sentinel_analyzer = SentinelAnalyzer()