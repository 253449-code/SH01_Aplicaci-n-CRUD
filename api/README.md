# FocusTask API (FastAPI + SQLite + JWT)

Backend REST API para la app Android **FocusTask** (Proyecto Unidad 3).

## Endpoints

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| `POST` | `/register` | No | Crea cuenta en SQLite y devuelve JWT (`access_token`) + usuario |
| `POST` | `/login` | No | Autentica credenciales y devuelve JWT (`access_token`) + usuario |
| `GET` | `/me` | Bearer JWT | Devuelve datos del usuario autenticado |
| `GET` | `/tasks` | Bearer JWT | Lista todas las tareas del usuario autenticado |
| `GET` | `/tasks/{id}` | Bearer JWT | Obtiene el detalle de una tarea |
| `POST` | `/tasks` | Bearer JWT | Crea una nueva tarea validada |
| `PUT` | `/tasks/{id}` | Bearer JWT | Actualiza una tarea existente |
| `DELETE` | `/tasks/{id}` | Bearer JWT | Elimina una tarea |
| `GET` | `/health` | No | Verifica estado del servidor |

## Cómo ejecutar

```bash
cd api
python3 -m venv .venv
.venv/bin/pip install -r requirements.txt
.venv/bin/uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

- URL desplegada por HTTPS: `https://focustask.44-220-6-229.sslip.io/` (proxy inverso con Caddy y certificado Let's Encrypt).
- Documentación interactiva Swagger UI: `https://focustask.44-220-6-229.sslip.io/docs`.
- Para desarrollo local en emulador Android también puede usarse `http://10.0.2.2:8000/`.

## Pruebas

```bash
cd api
.venv/bin/pytest -v
```
