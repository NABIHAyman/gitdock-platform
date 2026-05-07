from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
import uvicorn

from src.routes import prediction
from src.core.config import settings

app = FastAPI(
    title=settings.PROJECT_NAME,
    version=settings.VERSION
)


# app.add_middleware(
  #  CORSMiddleware,
   # allow_origins=["*"], # À restreindre en production
   # allow_credentials=True,
   # allow_methods=["*"],
    #allow_headers=["*"],
#)

app.include_router(prediction.router, prefix="/api/v1", tags=["Analyse"])

@app.get("/health")
def health():
    return {
        "status": "ok",
        "service": "gitdock-ai",
        "database": "connected"
    }

if __name__ == "__main__":
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=8000,
        reload=True
    )