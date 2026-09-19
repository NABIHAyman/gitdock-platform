from data import load_data

def check_deadlines():
    df = load_data()

    for _, row in df.iterrows():
        task_name = row["task_name"]
        deadline = row["deadline_days"]

        if deadline <= 2:
            print(f"🔴 URGENT: '{task_name}' deadline very close ({deadline} days)")
        elif deadline <= 5:
            print(f"⚠️ WARNING: '{task_name}' deadline approaching ({deadline} days)")
        else:
            print(f"🟢 OK: '{task_name}' is safe")

if __name__ == "__main__":
    check_deadlines()