import json
from pydantic_ai import Agent


def extract_text_from_result(result) -> str:
    try:
        if hasattr(result, 'output'):
            return str(result.output)
        elif hasattr(result, 'data'):
            return str(result.data)
    except Exception:
        pass
    return str(result)


async def execute_with_cascade(models: list, system_prompt: str, prompt: str, output_type=None):
    if output_type:
        # On supprime la génération hasardeuse et on donne le format parfait
        system_prompt += (
            f"\n\n--- INSTRUCTION STRICTE ---"
            f"\nGénère UNIQUEMENT un JSON valide respectant cette structure exacte :"
            f"\n{{"
            f"\n  \"is_clean\": true,"
            f"\n  \"vulnerabilities\": ["
            f"\n    {{"
            f"\n      \"severity\": \"CRITICAL\","
            f"\n      \"type\": \"Nom de la faille\","
            f"\n      \"line_snippet\": \"Code vulnérable\","
            f"\n      \"recommendation\": \"Comment corriger\""
            f"\n    }}"
            f"\n  ],"
            f"\n  \"summary\": \"Résumé de l'audit\""
            f"\n}}"
        )

    for idx, model in enumerate(models):
        try:
            agent = Agent(model, system_prompt=system_prompt, retries=1)
            result = await agent.run(prompt)

            if output_type:
                raw_text = extract_text_from_result(result).replace("```json", "").replace("```", "").strip()
                if not raw_text.startswith('{'): raw_text = '{' + raw_text.split('{', 1)[1]
                if not raw_text.endswith('}'): raw_text = raw_text.rsplit('}', 1)[0] + '}'

                parsed_json = json.loads(raw_text)
                validated_data = output_type.model_validate(parsed_json)

                class FakeResult:
                    def __init__(self, data): self.output = data

                return FakeResult(validated_data)

            class TextResult:
                def __init__(self, text): self.output = text

            return TextResult(extract_text_from_result(result))

        except Exception as e:
            print(f"⚠️ [CASCADE] Modèle {idx + 1} échoué : {str(e)}")

    raise RuntimeError("Tous les modèles ont échoué.")