import sqlite3

from fastapi import APIRouter, Depends, HTTPException, status

from app.database import get_db
from app.schemas import MessageResponse, TaskCreate, TaskOut, TaskUpdate
from app.security import get_current_user

router = APIRouter(prefix="/tasks", tags=["Tareas"])


def _row_to_task(row: sqlite3.Row) -> TaskOut:
    return TaskOut(
        id=row["id"],
        title=row["title"],
        description=row["description"],
        category=row["category"],
        estimated_time=float(row["estimated_time"]),
        priority=row["priority"],
        due_date=row["due_date"],
        is_completed=bool(row["is_completed"]),
        created_at=row["created_at"],
    )


@router.get("", response_model=list[TaskOut])
def list_tasks(
    db: sqlite3.Connection = Depends(get_db),
    current_user: sqlite3.Row = Depends(get_current_user),
) -> list[TaskOut]:
    rows = db.execute(
        """
        SELECT id, title, description, category, estimated_time, priority, due_date, is_completed, created_at
        FROM tasks
        WHERE user_id = ?
        ORDER BY id DESC
        """,
        (current_user["id"],),
    ).fetchall()
    return [_row_to_task(row) for row in rows]


@router.get("/{task_id}", response_model=TaskOut)
def get_task(
    task_id: int,
    db: sqlite3.Connection = Depends(get_db),
    current_user: sqlite3.Row = Depends(get_current_user),
) -> TaskOut:
    row = db.execute(
        """
        SELECT id, title, description, category, estimated_time, priority, due_date, is_completed, created_at
        FROM tasks
        WHERE id = ? AND user_id = ?
        """,
        (task_id, current_user["id"]),
    ).fetchone()
    if row is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Tarea no encontrada",
        )
    return _row_to_task(row)


@router.post("", response_model=TaskOut, status_code=status.HTTP_201_CREATED)
def create_task(
    payload: TaskCreate,
    db: sqlite3.Connection = Depends(get_db),
    current_user: sqlite3.Row = Depends(get_current_user),
) -> TaskOut:
    cursor = db.execute(
        """
        INSERT INTO tasks (user_id, title, description, category, estimated_time, priority, due_date, is_completed)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """,
        (
            current_user["id"],
            payload.title,
            payload.description,
            payload.category,
            payload.estimated_time,
            payload.priority,
            payload.due_date,
            1 if payload.is_completed else 0,
        ),
    )
    db.commit()

    task_id = cursor.lastrowid
    row = db.execute(
        """
        SELECT id, title, description, category, estimated_time, priority, due_date, is_completed, created_at
        FROM tasks
        WHERE id = ? AND user_id = ?
        """,
        (task_id, current_user["id"]),
    ).fetchone()
    assert row is not None
    return _row_to_task(row)


@router.put("/{task_id}", response_model=TaskOut)
def update_task(
    task_id: int,
    payload: TaskUpdate,
    db: sqlite3.Connection = Depends(get_db),
    current_user: sqlite3.Row = Depends(get_current_user),
) -> TaskOut:
    existing = db.execute(
        "SELECT id FROM tasks WHERE id = ? AND user_id = ?",
        (task_id, current_user["id"]),
    ).fetchone()
    if existing is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Tarea no encontrada",
        )

    db.execute(
        """
        UPDATE tasks
        SET title = ?,
            description = ?,
            category = ?,
            estimated_time = ?,
            priority = ?,
            due_date = ?,
            is_completed = ?,
            updated_at = datetime('now')
        WHERE id = ? AND user_id = ?
        """,
        (
            payload.title,
            payload.description,
            payload.category,
            payload.estimated_time,
            payload.priority,
            payload.due_date,
            1 if payload.is_completed else 0,
            task_id,
            current_user["id"],
        ),
    )
    db.commit()

    row = db.execute(
        """
        SELECT id, title, description, category, estimated_time, priority, due_date, is_completed, created_at
        FROM tasks
        WHERE id = ? AND user_id = ?
        """,
        (task_id, current_user["id"]),
    ).fetchone()
    assert row is not None
    return _row_to_task(row)


@router.delete("/{task_id}", response_model=MessageResponse)
def delete_task(
    task_id: int,
    db: sqlite3.Connection = Depends(get_db),
    current_user: sqlite3.Row = Depends(get_current_user),
) -> MessageResponse:
    cursor = db.execute(
        "DELETE FROM tasks WHERE id = ? AND user_id = ?",
        (task_id, current_user["id"]),
    )
    db.commit()
    if cursor.rowcount == 0:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Tarea no encontrada",
        )
    return MessageResponse(message="Tarea eliminada correctamente")
