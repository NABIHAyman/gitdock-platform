import pandas as pd
from data import load_data

def export_to_csv():
    df = load_data()

    # (optionnel) features IA
    df["risk"] = (
        (df["last_update_days"] > 3).astype(int) +
        (df["deadline_days"] < 2).astype(int) +
        (df["status"] == "todo").astype(int)
    )

    # save CSV
    df.to_csv("tasks_dataset.csv", index=False)

    print("✅ CSV created successfully: tasks_dataset.csv")

if __name__ == "__main__":
    export_to_csv()