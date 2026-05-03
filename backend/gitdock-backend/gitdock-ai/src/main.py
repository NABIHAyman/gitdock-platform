from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from src.routes import prediction
from src.core.config import settings

app = FastAPI(title=settings.PROJECT_NAME, version=settings.VERSION)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(prediction.router, prefix="/api/v1", tags=["Analyse"])

@app.get("/health")
def health():
    return {"status": "ok", "service": "gitdock-ai"}