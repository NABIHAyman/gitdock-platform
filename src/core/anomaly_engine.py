from sklearn.ensemble import IsolationForest
from sklearn.preprocessing import StandardScaler
import pandas as pd
import numpy as np

class AnomalyEngine:
    def __init__(self):
        # contamination=0.15 means we expect roughly 15% of commits to be flagged as anomalies
        self.model = IsolationForest(contamination=0.15, random_state=42)
        self.scaler = StandardScaler()

    def predict(self, df):
        features = ['total_changes', 'additions', 'deletions', 'files_count', 'hour']
        X = self.scaler.fit_transform(df[features])
        
        # Fit and Predict on the current project for maximum accuracy
        preds = self.model.fit_predict(X)
        
        results = []
        for i in range(len(df)):
            row = df.iloc[i]
            is_anomaly = preds[i] == -1
            
            reasons = []
            # Rule-based explanations to support the AI decision
            if row['total_changes'] > 1200: 
                reasons.append("🚀 Massive Volume")
            if row['files_count'] > 12: 
                reasons.append("📁 Too many files")
            if row['hour'] < 7 or row['hour'] > 22: 
                reasons.append("🌙 Off-hours commit")
            
            # Contextual keywords check
            if any(word in row['message'].lower() for word in ['fix', 'bug', 'error']): 
                if is_anomaly: 
                    reasons.append("⚠️ Critical Fix")

            # Final status and justification logic
            status = "Anomaly" if is_anomaly else "Stable"
            
            if is_anomaly:
                # If AI found an anomaly but no specific rule was triggered
                reason_text = " / ".join(reasons) if reasons else "Unusual Pattern"
            else:
                reason_text = "Healthy"

            results.append({
                **row.to_dict(),
                "status": status,
                "reason": reason_text
            })
            
        return pd.DataFrame(results)