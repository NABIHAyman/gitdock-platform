import pandas as pd
from database import db_engine as engine
import os

def load_data():
    query = """
    SELECT
        title as task_name,
        0 as commits,
        DATEDIFF(NOW(), updated_at) as last_update_days,
        DATEDIFF(due_date, NOW()) as deadline_days,
        status,
        priority
    FROM task
    """
    try:
        df = pd.read_sql(query, engine)
    except Exception:
        df = pd.DataFrame()

    # TACTIQUE DE SECOURS : Si MySQL est vide, on prend le CSV
    if df.empty:
        print("⚠️ Base de données vide ou inaccessible. Chargement du CSV de secours...")
        if os.path.exists("tasks_dataset.csv"):
            df = pd.read_csv("tasks_dataset.csv")
            # On renomme pour coller à la structure attendue
            if "task_name" not in df.columns and "title" in df.columns:
                df = df.rename(columns={"title": "task_name"})
        else:
            # Si même le CSV manque, on crée une ligne bidon pour ne pas faire crash train.py
            print("❌ Aucune donnée trouvée. Création d'une donnée fictive.")
            df = pd.DataFrame([{
                "task_name": "Initialisation", "commits": 0,
                "last_update_days": 0, "deadline_days": 10,
                "status": "todo", "priority": "medium"
            }])

    # Sécurité : Si DATEDIFF donne un nombre négatif, on met 0
    if 'deadline_days' in df.columns:
        df['deadline_days'] = pd.to_numeric(df['deadline_days'], errors='coerce').fillna(0)
        df['deadline_days'] = df['deadline_days'].apply(lambda x: x if x > 0 else 0)

    return df