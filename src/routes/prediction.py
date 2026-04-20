from fastapi import APIRouter
from src.data.fetcher import GitFetcher
from src.core.anomaly_engine import AnomalyEngine
import pandas as pd
import os

router = APIRouter()
fetcher = GitFetcher()
engine = AnomalyEngine()

@router.get("/analyze-all")
async def analyze_all_projects_auto():
    """
    Cette route est appelée automatiquement par Streamlit.
    Elle fait tout le travail sans intervention humaine.
    """
    # 1. SYNCHRONISATION AUTOMATIQUE
    # Le fetcher va lire PostgreSQL et remplir le CSV immédiatement
    fetcher.run_ingestion_from_db()
    
    # 2. CHARGEMENT DES DONNÉES FRAÎCHES
    if not os.path.exists("data/anomaly_dataset.csv"):
        return {"report": [], "message": "Aucune donnée disponible"}
        
    df = pd.read_csv("data/anomaly_dataset.csv")
    
    # 3. ANALYSE ET GÉNÉRATION DU RAPPORT
    report = []
    for project_name in df['project'].unique():
        proj_df = df[df['project'] == project_name]
        
        # L'IA analyse les lignes du projet
        results = engine.predict_anomalies(proj_df)
        
        # On ne garde que les anomalies pour le manager
        anomalies = [r for r in results if r['is_anomaly']]
        
        report.append({
            "project_name": project_name,
            "total_commits": len(results),
            "anomalies_found": len(anomalies),
            "details": anomalies
        })
        
    return {"report": report}