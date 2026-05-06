from fastapi import APIRouter, HTTPException
from src.data.fetcher import GitFetcher
from src.core.anomaly_engine import AnomalyEngine
import pandas as pd
import os

router = APIRouter()
fetcher = GitFetcher()
engine = AnomalyEngine()

@router.get("/analyze-all")
async def analyze_all_projects(projects: str = None):
    if not os.path.exists("data/anomaly_dataset.csv"):
        return {"report": [], "message": "Aucune donnée disponible"}

    df = pd.read_csv("data/anomaly_dataset.csv")
    project_list = [p.strip() for p in projects.split(",")] if projects else None

    report = []
    for project_name in df['project'].unique():
        if project_list:
            match = any(p.lower() in project_name.lower() for p in project_list)
            if not match:
                continue
        proj_df = df[df['project'] == project_name]
        results = engine.predict(proj_df)
        anomalies = results[results['status'] == "Anomaly"]
        report.append({
            "project_name": project_name,
            "total_commits": len(proj_df),
            "anomalies_found": len(anomalies),
            "details": anomalies[['sha','message','status','reason']].to_dict(orient='records')
        })

    return {"report": report}

@router.get("/analyze-project")
async def analyze_project(project_name: str):
    result = fetcher.get_project_from_db(project_name)
    if not result:
        raise HTTPException(status_code=404, detail="Projet introuvable")

    url, platform = result
    df = fetcher.fetch_commits(platform, url)
    if df.empty:
        raise HTTPException(status_code=400, detail="Aucun commit récupéré")

    results = engine.predict(df)
    anomalies = results[results['status'] == "Anomaly"]
    return {
        "project": project_name,
        "repo": url,
        "total": len(results),
        "anomalies": len(anomalies),
        "details": anomalies[['sha','message','status','reason']].to_dict(orient='records')
    }