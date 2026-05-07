import pandas as pd
from database import get_connection

def load_data():
    engine = get_connection()
    df = pd.read_sql("SELECT * FROM tasks", engine)
    return df