# Utilise une image Python légère
FROM python:3.11-slim

WORKDIR /app

# Copie les fichiers de dépendances et installe-les
COPY requirements.txt .
RUN pip install --no-cache-dir --upgrade pip && \
    pip install --no-cache-dir -r requirements.txt

# Copie TOUT le contenu du dossier local vers /app dans le conteneur
COPY . .

# Expose le port Streamlit
EXPOSE 8501

# Commande de lancement
CMD ["streamlit", "run", "src/app.py", "--server.port=8501", "--server.address=0.0.0.0"]