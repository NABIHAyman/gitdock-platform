FROM python:3.11-slim
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir --timeout=300 xgboost==3.2.0 --extra-index-url https://pypi.org/simple/ 
RUN pip install --no-cache-dir --timeout=300 --retries=5 -r requirements.txt
COPY . .
CMD ["python", "app.py"]
