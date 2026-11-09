from pathlib import Path
import sys

from fastapi.testclient import TestClient
import pytest

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from app.database import init_db
from app.main import app


@pytest.fixture()
def client(tmp_path: Path, monkeypatch: pytest.MonkeyPatch) -> TestClient:
    db_file = tmp_path / "test_focustask.db"
    monkeypatch.setenv("FOCUSTASK_DB_PATH", str(db_file))
    init_db()
    with TestClient(app) as test_client:
        yield test_client


def _register_and_get_headers(
    client: TestClient,
    name: str = "Emma López",
    email: str = "emma@example.com",
    password: str = "Password123",
) -> dict[str, str]:
    resp = client.post("/register", json={"name": name, "email": email, "password": password})
    assert resp.status_code == 201
    token = resp.json()["access_token"]
    return {"Authorization": f"Bearer {token}"}


def test_register_login_and_duplicate_email(client: TestClient) -> None:
    reg = client.post(
        "/register",
        json={"name": "Ana", "email": "ana@test.com", "password": "secret123"},
    )
    assert reg.status_code == 201
    body = reg.json()
    assert body["token_type"] == "bearer"
    assert body["user"]["email"] == "ana@test.com"
    assert body["access_token"]

    dup = client.post(
        "/register",
        json={"name": "Ana 2", "email": "ANA@test.com", "password": "secret123"},
    )
    assert dup.status_code == 409

    bad_login = client.post(
        "/login",
        json={"email": "ana@test.com", "password": "wrongpassword"},
    )
    assert bad_login.status_code == 401

    ok_login = client.post(
        "/login",
        json={"email": "ana@test.com", "password": "secret123"},
    )
    assert ok_login.status_code == 200
    assert ok_login.json()["user"]["name"] == "Ana"


def test_tasks_require_jwt(client: TestClient) -> None:
    resp = client.get("/tasks")
    assert resp.status_code == 401

    bad_token = client.get("/tasks", headers={"Authorization": "Bearer invalid.jwt.token"})
    assert bad_token.status_code == 401


def test_full_crud_and_user_isolation(client: TestClient) -> None:
    headers_u1 = _register_and_get_headers(client, email="u1@test.com")
    headers_u2 = _register_and_get_headers(client, name="Carlos", email="u2@test.com")

    # List initially empty
    assert client.get("/tasks", headers=headers_u1).json() == []

    # Create
    create_resp = client.post(
        "/tasks",
        headers=headers_u1,
        json={
            "title": "Estudiar Jetpack Compose",
            "description": "Repasar MVVM y StateFlow",
            "category": "Estudio",
            "estimated_time": 2.5,
            "priority": "Alta",
            "due_date": "2026-11-20",
            "is_completed": False,
        },
    )
    assert create_resp.status_code == 201
    task = create_resp.json()
    task_id = task["id"]
    assert task["title"] == "Estudiar Jetpack Compose"
    assert task["is_completed"] is False

    # Read list & detail
    assert len(client.get("/tasks", headers=headers_u1).json()) == 1
    assert client.get(f"/tasks/{task_id}", headers=headers_u1).json()["id"] == task_id

    # Isolation: user 2 cannot see or modify user 1's task
    assert client.get("/tasks", headers=headers_u2).json() == []
    assert client.get(f"/tasks/{task_id}", headers=headers_u2).status_code == 404

    # Update
    update_resp = client.put(
        f"/tasks/{task_id}",
        headers=headers_u1,
        json={
            "title": "Dominar Jetpack Compose",
            "description": "Proyecto Unidad 3 terminado",
            "category": "Trabajo",
            "estimated_time": 4.0,
            "priority": "Media",
            "due_date": "2026-11-21",
            "is_completed": True,
        },
    )
    assert update_resp.status_code == 200
    updated = update_resp.json()
    assert updated["title"] == "Dominar Jetpack Compose"
    assert updated["is_completed"] is True
    assert updated["category"] == "Trabajo"

    # Delete
    del_resp = client.delete(f"/tasks/{task_id}", headers=headers_u1)
    assert del_resp.status_code == 200
    assert client.get(f"/tasks/{task_id}", headers=headers_u1).status_code == 404
    assert client.get("/tasks", headers=headers_u1).json() == []


def test_validation_rules_and_health(client: TestClient) -> None:
    assert client.get("/health").json() == {"status": "ok"}

    short_pw = client.post(
        "/register",
        json={"name": "Luis", "email": "luis@test.com", "password": "123"},
    )
    assert short_pw.status_code == 422

    headers = _register_and_get_headers(client, email="val@test.com")
    bad_task = client.post(
        "/tasks",
        headers=headers,
        json={
            "title": "Ab",
            "description": "",
            "category": "Estudio",
            "estimated_time": 0.1,
            "priority": "Alta",
            "due_date": "fecha-invalida",
            "is_completed": False,
        },
    )
    assert bad_task.status_code == 422

