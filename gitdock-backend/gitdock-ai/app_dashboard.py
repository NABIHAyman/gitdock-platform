import streamlit as st
import plotly.express as px
from src.data.fetcher import GitFetcher
from src.core.anomaly_engine import AnomalyEngine
from src.rag.commit_store import CommitStore

st.set_page_config(page_title="Gitdock-AI", layout="wide", page_icon="🛡️")
st.markdown("""
    <style>
    .stApp { background-color: #F8FAFC; }
    .stMetric { background-color: white; padding: 20px; border-radius: 15px;
                box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); border: 1px solid #E2E8F0; }
    </style>
""", unsafe_allow_html=True)

fetcher = GitFetcher()
engine  = AnomalyEngine()
store   = CommitStore()

st.title("🛡️ Gitdock-AI : Security Analysis")
st.caption("AI-driven platform for Git security auditing and threat detection.")

col_s1, col_s2, col_s3 = st.columns([1, 2, 1])
with col_s2:
    query = st.text_input("", placeholder="🔍 Search project name or full URL...")

if query:
    db_res   = fetcher.get_project_from_db(query)
    url      = db_res[0] if db_res else query
    platform = db_res[1] if db_res else ("GITHUB" if "github" in query.lower() else "GITLAB")

    if not db_res and "http" not in query:
        st.error(f"❌ Project '{query}' not found in database.")
    else:
        with st.spinner("🔄 Analyzing repository..."):
            df = fetcher.fetch_commits(platform, url)

        if df is None or df.empty:
            st.warning(f"⚠️ Repository unreachable.")
        else:
            results = engine.predict(df)
            store.store_commits(results)

            anom_df = results[results['status'].str.strip() == "Anomaly"]

            m1, m2, m3, m4 = st.columns(4)
            m1.metric("Total Commits",    len(results))
            m2.metric("Anomalies Found",  len(anom_df), delta=len(anom_df), delta_color="inverse")
            m3.metric("Reliability",      f"{int((1 - len(anom_df)/len(results))*100)}%")
            m4.metric("Repo",             url.split("/")[-1].replace(".git","").upper())

            cl, cr = st.columns(2, gap="large")
            with cl:
                st.markdown("##### Distribution by Developer")
                fig = px.bar(results, x="author", color="status", barmode="group",
                             color_discrete_map={"Stable": "#10B981", "Anomaly": "#EF4444"},
                             template="plotly_white")
                st.plotly_chart(fig, use_container_width=True)

            with cr:
                st.markdown("##### Timeline Analysis")
                fig2 = px.scatter(results, x="hour", y="total_changes",
                                  size="files_count", color="status",
                                  hover_data=["message", "reason"],
                                  color_discrete_map={"Stable": "#10B981", "Anomaly": "#EF4444"},
                                  template="plotly_white")
                st.plotly_chart(fig2, use_container_width=True)

            st.markdown("### 📋 Detailed Security Audit")
            def style_rows(val):
                if str(val).strip() == "Anomaly":
                    return 'background-color: #FEE2E2; color: #991B1B; font-weight: bold;'
                return 'background-color: #ECFDF5; color: #065F46;'

            st.dataframe(
                results[['sha','author','message','status','reason']].style.map(
                    style_rows, subset=['status']
                ),
                use_container_width=True, height=500, hide_index=True
            )