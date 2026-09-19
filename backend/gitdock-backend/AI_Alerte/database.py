from sqlalchemy import create_engine
from urllib.parse import quote_plus

password = quote_plus("root")
# On définit l'objet 'db_engine' directement pour qu'il soit importable
db_engine = create_engine(
    f"mysql+pymysql://root:{password}@localhost/gitdock_tasks"
)

def get_connection():
    return db_engine