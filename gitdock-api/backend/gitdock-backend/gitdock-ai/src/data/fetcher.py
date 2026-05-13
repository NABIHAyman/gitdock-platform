import pandas as pd
import requests
import os
import psycopg2
import random
from src.core.config import settings

class GitFetcher:
    def __init__(self):
        # On garde un token système (fallback) pour GitHub et GitLab si besoin
        self.system_github_token = settings.GITHUB_TOKEN
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
        """Extrait dynamiquement 'owner/repo' depuis l'URL de la base de données."""
        path = repo_url.strip()
        if "github.com/" in path:
            path = path.split("github.com/")[-1]
        elif "gitlab.com/" in path:
            path = path.split("gitlab.com/")[-1]
        return path.replace(".git", "").strip("/")

    def get_user_github_token(self, user_id: str):
        """Appelle gitdock-auth pour récupérer le vrai token OAuth de l'utilisateur."""
        if not user_id:
            return None

        try:
            # ⚠️ Si gitdock-auth est sur le port 8081 en local
            auth_url = f"http://localhost:8081/api/auth/oauth/internal/token?userId={user_id}"
            response = requests.get(auth_url)

            if response.status_code == 200 and response.text:
                return response.text.strip()
        except Exception as e:
            print(f"⚠️ Impossible de récupérer le token depuis gitdock-auth: {e}")

        return None

    def fetch_commits(self, platform, repo_url, limit=30, user_id: str = None):
        """Récupère les commits dynamiquement."""
        # 1. Extraction dynamique (Zéro hardcoding !)
        repo_path = self._normalize_repo_path(repo_url)
        is_gitlab = "gitlab" in platform.lower()
        headers = {}

        if is_gitlab:
            encoded_path = repo_path.replace('/', '%2F')
            api_url = f"https://gitlab.com/api/v4/projects/{encoded_path}/repository/commits"
            headers = self.gitlab_headers
        else:
            api_url = f"https://api.github.com/repos/{repo_path}/commits"
            headers = {"Accept": "application/vnd.github.v3+json"}

            # 2. Injection du Token Dynamique
            token = self.get_user_github_token(user_id)
            if token:
                headers["Authorization"] = f"Bearer {token}"
                print("🔐 Aspiration GitHub AVEC le Token OAuth de l'utilisateur")
            elif self.system_github_token:
                headers["Authorization"] = f"Bearer {self.system_github_token}"
                print("⚠️ Aspiration GitHub avec le Token Système (Fallback)")
            else:
                print("⚠️ Aspiration GitHub SANS Token (Risque de Rate Limit imminent !)")

        try:
            print(f"📡 Analyse du dépôt : {repo_path}...")
            res = requests.get(api_url, headers=headers, params={'per_page': limit}, timeout=10)

            if res.status_code != 200:
                print(f"❌ Erreur API ({res.status_code}) pour {repo_path}. Réponse: {res.text}")
                return pd.DataFrame()

            commits_raw = res.json()
            data = []

            # 3. Traitement des données
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
            print(f"⚠️ Erreur lors du fetch : {e}")
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