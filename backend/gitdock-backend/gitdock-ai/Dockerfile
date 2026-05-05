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
# Change la commande pour utiliser le vrai nom du fichier à la racine
# Au lieu de streamlit run...
# Si main.py est au même niveau que routes/ et core/
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000", "--reload"]