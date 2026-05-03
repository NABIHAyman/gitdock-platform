from sklearn.ensemble import IsolationForest
from sklearn.preprocessing import StandardScaler
import pandas as pd

class AnomalyEngine:
    def __init__(self):
        self.model = IsolationForest(contamination=0.15, random_state=42)
        self.scaler = StandardScaler()

    def predict(self, df):
        features = ['total_changes', 'additions', 'deletions', 'files_count', 'hour']
        X = self.scaler.fit_transform(df[features])
        preds = self.model.fit_predict(X)

        results = []
        for i in range(len(df)):
            row = df.iloc[i]
            is_anomaly = preds[i] == -1

            reasons = []
            if row['total_changes'] > 1200:     reasons.append("🚀 Massive Volume")
            if row['files_count'] > 12:          reasons.append("📁 Too many files")
            if row['hour'] < 7 or row['hour'] > 22: reasons.append("🌙 Off-hours commit")
            if any(w in row['message'].lower() for w in ['fix', 'bug', 'error']):
                if is_anomaly: reasons.append("⚠️ Critical Fix")

            status = "Anomaly" if is_anomaly else "Stable"
            reason_text = " / ".join(reasons) if (is_anomaly and reasons) else ("Unusual Pattern" if is_anomaly else "Healthy")

            results.append({**row.to_dict(), "status": status, "reason": reason_text})

        return pd.DataFrame(results)