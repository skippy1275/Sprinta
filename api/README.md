# SPRINTA REST API

Backend API for the OPSC 6312 SPRINTA Android application.

## Run locally

```bash
npm install
cp .env.example .env
npm start
```

The local API listens on `http://localhost:4000` by default.

## PostgreSQL

Set `DATABASE_URL` in `.env`. The API automatically creates the required tables on startup. The SQL schema is also provided in `db.sql`.

## Endpoints

- `GET /health`
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/runs/sync`
- `GET /api/v1/runs/user/:userId`

## Password security

Passwords are never stored in plain text. The API uses BCrypt hashing with a work factor of 12 before storage in PostgreSQL.

## Production hosting

Deploy the Node.js service to a public HTTPS host such as Render or another approved hosting service. Create a managed PostgreSQL database, set `DATABASE_URL` and a strong `JWT_SECRET`, then place the resulting HTTPS API URL into the Android app's `local.properties`.
