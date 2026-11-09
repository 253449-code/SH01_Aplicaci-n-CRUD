from datetime import date
from typing import Literal

from pydantic import BaseModel, EmailStr, Field, field_validator

VALID_CATEGORIES = {"estudio": "Estudio", "trabajo": "Trabajo", "personal": "Personal"}
VALID_PRIORITIES = {"alta": "Alta", "media": "Media", "baja": "Baja"}


class RegisterRequest(BaseModel):
    name: str = Field(..., min_length=2, max_length=80)
    email: EmailStr
    password: str = Field(..., min_length=8, max_length=128)

    @field_validator("name")
    @classmethod
    def validate_name(cls, value: str) -> str:
        cleaned = value.strip()
        if len(cleaned) < 2:
            raise ValueError("El nombre debe tener al menos 2 caracteres")
        return cleaned

    @field_validator("email")
    @classmethod
    def normalize_email(cls, value: EmailStr) -> str:
        return str(value).strip().lower()


class LoginRequest(BaseModel):
    email: EmailStr
    password: str = Field(..., min_length=1, max_length=128)

    @field_validator("email")
    @classmethod
    def normalize_email(cls, value: EmailStr) -> str:
        return str(value).strip().lower()


class UserOut(BaseModel):
    id: int
    name: str
    email: str


class AuthResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"
    user: UserOut


class TaskPayload(BaseModel):
    title: str = Field(..., min_length=3, max_length=100)
    description: str = Field(default="", max_length=500)
    category: Literal["Estudio", "Trabajo", "Personal"]
    estimated_time: float = Field(..., ge=0.5, le=24.0)
    priority: Literal["Alta", "Media", "Baja"]
    due_date: str
    is_completed: bool = False

    @field_validator("title")
    @classmethod
    def validate_title(cls, value: str) -> str:
        cleaned = value.strip()
        if len(cleaned) < 3:
            raise ValueError("El título debe tener al menos 3 caracteres")
        return cleaned

    @field_validator("description")
    @classmethod
    def normalize_description(cls, value: str) -> str:
        return value.strip()

    @field_validator("category", mode="before")
    @classmethod
    def normalize_category(cls, value: str) -> str:
        if isinstance(value, str):
            mapped = VALID_CATEGORIES.get(value.strip().lower())
            if mapped:
                return mapped
        raise ValueError("La categoría debe ser Estudio, Trabajo o Personal")

    @field_validator("priority", mode="before")
    @classmethod
    def normalize_priority(cls, value: str) -> str:
        if isinstance(value, str):
            mapped = VALID_PRIORITIES.get(value.strip().lower())
            if mapped:
                return mapped
        raise ValueError("La prioridad debe ser Alta, Media o Baja")

    @field_validator("due_date")
    @classmethod
    def validate_due_date(cls, value: str) -> str:
        cleaned = value.strip()
        try:
            parsed = date.fromisoformat(cleaned)
        except ValueError as exc:
            raise ValueError("La fecha límite debe tener formato YYYY-MM-DD") from exc
        return parsed.isoformat()


class TaskCreate(TaskPayload):
    pass


class TaskUpdate(TaskPayload):
    pass


class TaskOut(BaseModel):
    id: int
    title: str
    description: str
    category: str
    estimated_time: float
    priority: str
    due_date: str
    is_completed: bool
    created_at: str


class MessageResponse(BaseModel):
    message: str
