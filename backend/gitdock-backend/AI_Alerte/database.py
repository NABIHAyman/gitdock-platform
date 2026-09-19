import os
from sqlalchemy import create_engine
from urllib.parse import quote_plus

# Connexion lue depuis l'environnement (voir .env.example) ; les valeurs par
# défaut correspondent à l'instance MySQL locale de développement.
user = os.getenv("DB_USER", "root")
password = quote_plus(os.getenv("DB_PASSWORD", "root"))
host = os.getenv("DB_HOST", "localhost")
port = os.getenv("DB_PORT", "3306")
name = os.getenv("DB_NAME", "gitdock_tasks")
# On définit l'objet 'db_engine' directement pour qu'il soit importable
db_engine = create_engine(
    f"mysql+pymysql://{user}:{password}@{host}:{port}/{name}"
)

def get_connection():
    return db_engine