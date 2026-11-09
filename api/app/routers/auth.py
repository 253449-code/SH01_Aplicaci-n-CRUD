import sqlite3

from fastapi import APIRouter, Depends, HTTPException, status

from app.database import get_db
from app.schemas import AuthResponse, LoginRequest, RegisterRequest, UserOut
from app.security import create_access_token, get_current_user, hash_password, verify_password

router = APIRouter(tags=["Autenticación"])


@router.post("/register", response_model=AuthResponse, status_code=status.HTTP_201_CREATED)
def register(payload: RegisterRequest, db: sqlite3.Connection = Depends(get_db)) -> AuthResponse:
    existing = db.execute("SELECT id FROM users WHERE email = ?", (payload.email,)).fetchone()
    if existing is not None:
        raise HTTPException(
            status_code=status.HTTP_409_CONFLICT,
            detail="Ya existe una cuenta registrada con este correo electrónico",
        )

    password_hash = hash_password(payload.password)
    cursor = db.execute(
        "INSERT INTO users (name, email, password_hash) VALUES (?, ?, ?)",
        (payload.name, payload.email, password_hash),
    )
    db.commit()

    user_id = cursor.lastrowid
    assert user_id is not None
    token = create_access_token(user_id=user_id, email=payload.email)
    return AuthResponse(
        access_token=token,
        user=UserOut(id=user_id, name=payload.name, email=payload.email),
    )


@router.post("/login", response_model=AuthResponse)
def login(payload: LoginRequest, db: sqlite3.Connection = Depends(get_db)) -> AuthResponse:
    user = db.execute(
        "SELECT id, name, email, password_hash FROM users WHERE email = ?",
        (payload.email,),
    ).fetchone()

    if user is None or not verify_password(payload.password, user["password_hash"]):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Correo o contraseña incorrectos",
        )

    token = create_access_token(user_id=user["id"], email=user["email"])
    return AuthResponse(
        access_token=token,
        user=UserOut(id=user["id"], name=user["name"], email=user["email"]),
    )


@router.get("/me", response_model=UserOut)
def get_me(current_user: sqlite3.Row = Depends(get_current_user)) -> UserOut:
    return UserOut(
        id=current_user["id"],
        name=current_user["name"],
        email=current_user["email"],
    )
