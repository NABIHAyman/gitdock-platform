from data import load_data
from model import train_model
from predict import predict_risk

# load data + train model
df = load_data()

df["risk"] = (
    (df["last_update_days"] > 3).astype(int) +
    (df["deadline_days"] < 2).astype(int) +
    (df["status"] == "todo").astype(int)
)

model = train_model(df)

# nouvelle tâche (simulation)
task = {
    "commits": 2,
    "last_update_days": 5,
    "deadline_days": 1
}

risk = predict_risk(model, task)

print("🔍 Risk prediction:", risk)

if risk == 1:
    print("🔔 ALERT: Task is at risk of delay!")
else:
    print("✅ Task is safe")