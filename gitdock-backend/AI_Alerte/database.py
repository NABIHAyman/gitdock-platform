from sqlalchemy import create_engine
from urllib.parse import quote_plus

def get_connection():
    password = quote_plus("***REMOVED***")

    engine = create_engine(
        f"mysql+pymysql://root:{password}@localhost/travel"
    )

    return engine