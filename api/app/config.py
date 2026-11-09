import os
from pathlib import Path

BASE_DIR = Path(__file__).resolve().parent.parent

SECRET_KEY = os.getenv("FOCUSTASK_SECRET_KEY", "focustask-dev-jwt-secret-key-change-in-production-2026")
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = int(os.getenv("FOCUSTASK_TOKEN_EXPIRE_MINUTES", str(60 * 24 * 7)))


def get_db_path() -> str:
    return os.getenv("FOCUSTASK_DB_PATH", str(BASE_DIR / "focustask.db"))
