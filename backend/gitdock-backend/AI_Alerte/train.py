import pandas as pd
import joblib

from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score, classification_report
from xgboost import XGBClassifier

from data import load_data
from features import create_features

# ---------------------------
# LOAD DATA
# ---------------------------
df = load_data()
df = create_features(df)

df = df.dropna()

# ---------------------------
# CLEAN NUMERIC
# ---------------------------
df["commits"] = pd.to_numeric(df["commits"], errors="coerce").fillna(0)
df["last_update_days"] = pd.to_numeric(df["last_update_days"], errors="coerce").fillna(0)
df["deadline_days"] = pd.to_numeric(df["deadline_days"], errors="coerce").fillna(1)

# ---------------------------
# FEATURES
# ---------------------------
df["delay_ratio"] = df["last_update_days"] / (df["deadline_days"] + 1)
df["work_rate"] = df["commits"] / (df["last_update_days"] + 1)

# ---------------------------
# LABEL (REALISTE + LOGIQUE BUSINESS)
# ---------------------------
df["risk"] = (
    (df["last_update_days"] > df["deadline_days"]) |   # retard
    (df["commits"] < 2)                                # faible activité
).astype(int)

# ---------------------------
# FEATURES LIST
# ---------------------------
X = df[
    [
        "commits",
        "last_update_days",
        "deadline_days",
        "delay_ratio",
        "work_rate"
    ]
]

y = df["risk"]

# ---------------------------
# SPLIT
# ---------------------------
X_train, X_test, y_train, y_test = train_test_split(
    X,
    y,
    test_size=0.3,
    random_state=42,
    stratify=y
)

# ---------------------------
# MODEL
# ---------------------------
model = XGBClassifier(
    n_estimators=100,
    max_depth=3,
    learning_rate=0.1,
    eval_metric="logloss"
)

model.fit(X_train, y_train)

# ---------------------------
# PREDICTION
# ---------------------------
y_pred = model.predict(X_test)

# ---------------------------
# EVALUATION
# ---------------------------
accuracy = accuracy_score(y_test, y_pred)

print("📊 Accuracy:", round(accuracy * 100, 2), "%")
print("\n📌 Classification Report:\n")
print(classification_report(y_test, y_pred))

# ---------------------------
# SAVE MODEL
# ---------------------------
joblib.dump(model, "model.pkl")

print("✅ Model saved successfully")