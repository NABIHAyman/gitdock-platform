import pandas as pd

def predict_risk(model, task):
    df = pd.DataFrame([task])

    # 🔥 mêmes transformations que train
    df["commits"] = pd.to_numeric(df["commits"], errors="coerce").fillna(0)
    df["last_update_days"] = pd.to_numeric(df["last_update_days"], errors="coerce").fillna(0)
    df["deadline_days"] = pd.to_numeric(df["deadline_days"], errors="coerce").fillna(1)

    df["delay_ratio"] = df["last_update_days"] / (df["deadline_days"] + 1)
    df["work_rate"] = df["commits"] / (df["last_update_days"] + 1)

    X = df[[
        "commits",
        "last_update_days",
        "deadline_days",
        "delay_ratio",
        "work_rate"
    ]]

    return model.predict(X)[0]