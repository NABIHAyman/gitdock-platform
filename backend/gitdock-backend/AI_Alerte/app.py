import streamlit as st
import joblib
import pandas as pd
from streamlit_autorefresh import st_autorefresh

from data import load_data
from features import create_features

# ---------------------------
# CONFIG
# ---------------------------
st.set_page_config(page_title="AI Reminder Notifier", layout="wide")
st.title("🔔 AI Reminder - Task Notification System")

# 🔁 AUTO REFRESH (5 sec)
st_autorefresh(interval=5000, key="refresh")

# ---------------------------
# LOAD DATA
# ---------------------------
def get_data():
    df = load_data()
    df = create_features(df)
    return df

@st.cache_resource
def get_model():
    return joblib.load("model.pkl")

df = get_data()
model = get_model()

# ---------------------------
# ML PREDICTION
# ---------------------------
def predict_risk(df):
    X = df[[
        "commits",
        "last_update_days",
        "deadline_days",
        "delay_ratio",
        "work_rate"
    ]]
    return model.predict(X)

df["risk"] = predict_risk(df)

# ---------------------------
# RULE ENGINE (48H ALERT)
# ---------------------------
df["urgent_48h"] = df["deadline_days"] <= 2

# ---------------------------
# DASHBOARD
# ---------------------------
st.subheader("📊 User Overview")

col1, col2, col3, col4 = st.columns(4)

col1.metric("Total Tasks", len(df))
col2.metric("ML High Risk", len(df[df["risk"] == 1]))
col3.metric("Urgent (48h)", len(df[df["urgent_48h"] == True]))
col4.metric("Safe Tasks", len(df[df["risk"] == 0]))

# ---------------------------
# 🚨 ALERTS (ML + RULES)
# ---------------------------
st.subheader("🚨 Alerts")

alerts = df[(df["risk"] == 1) | (df["urgent_48h"] == True)]

if alerts.empty:
    st.success("🟢 No alerts - Everything is under control")
else:
    for _, row in alerts.iterrows():
        delay = row["last_update_days"] - row["deadline_days"]

        if row["urgent_48h"]:
            st.warning(
                f"""
⏳ URGENT TASK (≤ 48h):
- Task: {row['task_name']}
- Remaining days: {row['deadline_days']}
- Commits: {row['commits']}
"""
            )

        if row["risk"] == 1:
            st.error(
                f"""
🔴 ML ALERT:
- Task: {row['task_name']}
- Delay: {delay} days
- Commits: {row['commits']}
- Status: {row['status']}
"""
            )

# ---------------------------
# TABLE
# ---------------------------
st.subheader("📋 All Tasks")

st.dataframe(df, width="stretch")