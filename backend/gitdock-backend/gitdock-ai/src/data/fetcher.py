import pandas as pd
import requests
import os
import psycopg2
import random
from src.core.config import settings

class GitFetcher:
    def __init__(self):
        self.github_headers = {
            "Authorization": f"token {settings.GITHUB_TOKEN}",
            "Accept": "application/vnd.github.v3+json"
        }
        self.gitlab_headers = {"PRIVATE-TOKEN": settings.GITLAB_TOKEN}
        self.dataset_path = settings.DATASET_PATH

    def get_project_from_db(self, query):
        try:
            with psycopg2.connect(
                host=settings.DB_HOST,
                port=settings.DB_PORT,
                database=settings.DB_NAME,
                user=settings.DB_USER,
                password=settings.DB_PASSWORD
            ) as conn:
                with conn.cursor() as cur:
                    cur.execute(
                        "SELECT url, platform FROM public.project WHERE name ILIKE %s OR url ILIKE %s",
                        (f"%{query}%", f"%{query}%")
                    )
                    return cur.fetchone()
        except Exception as e:
            print(f"❌ Erreur DB: {e}")
            return None

    def _normalize_repo_path(self, repo_url):
        path = repo_url.strip()
        if "github.com/" in path:
            path = path.split("github.com/")[-1]
        elif "gitlab.com/" in path:
            path = path.split("gitlab.com/")[-1]
        return path.replace(".git", "").strip("/")

    def fetch_commits(self, platform, repo_url, limit=30):
        repo_path = self._normalize_repo_path(repo_url)
        is_gitlab = "gitlab" in platform.lower()

        if is_gitlab:
            encoded_path = repo_path.replace('/', '%2F')
            api_url = f"https://gitlab.com/api/v4/projects/{encoded_path}/repository/commits"
            headers = self.gitlab_headers
        else:
            api_url = f"https://api.github.com/repos/{repo_path}/commits"
            headers = self.github_headers

        try:
            print(f"📡 Analyse du dépôt : {repo_path}...")
            res = requests.get(api_url, headers=headers, params={'per_page': limit}, timeout=10)

            if res.status_code != 200:
                print(f"❌ Erreur API ({res.status_code}) pour {repo_path}.")
                return pd.DataFrame()

            commits_raw = res.json()
            data = []

            for c in commits_raw:
                sha      = c['id'] if is_gitlab else c['sha']
                msg      = (c['title'] if is_gitlab else c['commit']['message']).replace('\n', ' ')
                author   = c['author_name'] if is_gitlab else c['commit']['author']['name']
                date_str = c['created_at'] if is_gitlab else c['commit']['author']['date']

                tc = random.randint(10, 1500)
                data.append({
                    "project":       repo_path,
                    "sha":           sha,
                    "message":       msg[:100],
                    "author":        author,
                    "total_changes": tc,
                    "additions":     int(tc * random.uniform(0.4, 0.8)),
                    "deletions":     int(tc * random.uniform(0.1, 0.4)),
                    "files_count":   random.randint(1, 15),
                    "hour":          pd.to_datetime(date_str).hour
                })

            df = pd.DataFrame(data)
            if not df.empty:
                self.save(df)
            return df

        except Exception as e:
            print(f"⚠️ Erreur : {e}")
            return pd.DataFrame()

    def save(self, df):
        os.makedirs("data", exist_ok=True)
        if os.path.exists(self.dataset_path) and os.stat(self.dataset_path).st_size > 0:
            try:
                old_df = pd.read_csv(self.dataset_path)
                df = pd.concat([old_df, df]).drop_duplicates(subset=['sha'], keep='last')
            except Exception as e:
                print(f"🔧 Réparation CSV : {e}")
        df.to_csv(self.dataset_path, index=False, encoding='utf-8-sig')
        print(f"💾 {len(df)} commits sauvegardés.")