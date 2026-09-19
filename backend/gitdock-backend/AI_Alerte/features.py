import pandas as pd

def create_features(df):
    # ---------------------------
    # CLEAN NUMERIC
    # ---------------------------
    df["commits"] = pd.to_numeric(df["commits"], errors="coerce").fillna(0)
    df["last_update_days"] = pd.to_numeric(df["last_update_days"], errors="coerce").fillna(0)
    df["deadline_days"] = pd.to_numeric(df["deadline_days"], errors="coerce").fillna(1)

    # ---------------------------
    # FEATURES EXISTANTES
    # ---------------------------
    df["delay_ratio"] = df["last_update_days"] / (df["deadline_days"] + 1)
    df["work_rate"] = df["commits"] / (df["last_update_days"] + 1)

    # ---------------------------
    # 🔥 NEW FEATURE : URGENT DEADLINE (48h)
    # ---------------------------
    df["urgent_48h"] = df["deadline_days"] <= 2

    return df