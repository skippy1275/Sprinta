# SPRINTA — OPSC 6312 POE Part 2

## Project

**Application:** SPRINTA  
**Tagline:** Track Every Stride, Anywhere.  
**Module:** Open-Source Coding — OPSC 6312  
**Student:** Darren Keanan Skippers  
**Qualification:** Diploma in Software Development

## Purpose

SPRINTA is a native Android running application developed in Kotlin. It allows runners to register and log in, configure application preferences, record outdoor runs using GPS, view route information, save workout data locally, synchronise completed workouts with a REST API, review workout history and detect personal records.

## Part 2 user-defined features

### User Defined Feature 1 — Real-Time GPS Tracking

SPRINTA uses the Android fused location provider and a foreground location service to collect route points, calculate distance, duration and average pace, and display the route on Google Maps.

### User Defined Feature 2 — Offline-First Synchronisation

Workout data is stored in Room when the device is offline. The workout is marked as pending and Android WorkManager automatically attempts synchronisation when network connectivity is available.

### User Defined Feature 3 — Personal Records

SPRINTA evaluates completed workouts against existing 1 km, 5 km and 10 km records. A faster pace creates a new personal record and displays an achievement dialog.

## Technology stack

- Android Studio
- Kotlin
- Jetpack Compose + Material 3
- Room / SQLite
- WorkManager
- Jetpack DataStore
- Retrofit + OkHttp
- Google Maps SDK
- FusedLocationProviderClient
- Node.js + Express
- PostgreSQL
- BCrypt password hashing
- JWT authentication
- GitHub
- GitHub Actions

## Architecture

Android UI → ViewModel → Repository → Room / Retrofit → REST API → PostgreSQL

GPS tracking runs through a foreground service. WorkManager handles deferred synchronisation. DataStore stores user preferences.

## Setup

### Android

1. Open the repository root `SPRINTA_POE_PART2` in Android Studio.
2. Create `local.properties` from `local.properties.example` inside the project root.
3. Set `SPRINTA_API_BASE_URL` to the public HTTPS API URL.
4. Set `MAPS_API_KEY` to a Google Maps API key.
5. Sync Gradle.
6. Run the app on an Android phone.

### API

```bash
cd api
npm install
cp .env.example .env
npm start
```

Set `DATABASE_URL` and `JWT_SECRET` in `.env`.

## REST API

- `GET /health`
- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/runs/sync`
- `GET /api/v1/runs/user/:userId`

## Security

Passwords are validated before submission and BCrypt hashed before storage. JWT access tokens are stored in Android encrypted preferences. API access is performed over HTTPS in the deployed environment.

## Testing

The Android project contains unit tests for validation, distance calculation, pace calculation and personal-record detection. The Node.js API contains validation tests. GitHub Actions runs API tests, Android unit tests and an Android debug build on every push and pull request.

## GitHub development

The repository should show regular commits as development progresses. Recommended commit sequence includes authentication, settings, Room, API integration, GPS tracking, offline synchronisation, personal records, tests, GitHub Actions and documentation.

## Screenshots

Add actual screenshots from the physical-device demonstration to this section before final submission. Recommended evidence: Login/Register, Dashboard, Settings, Live GPS Tracking, Workout Summary, Offline Pending workout, Personal Record, GitHub Actions and PostgreSQL.

## Demonstration video

Add the final unlisted YouTube demonstration link here:

`VIDEO_LINK_HERE`

The demonstration should show the app running on a physical mobile phone, registration/login, settings, REST API/database data, GPS tracking, workout saving, offline mode, synchronisation, personal records, GitHub commits, automated tests and README documentation.

## AI usage declaration

Use the same declaration already included in POE Part 1, updated only so that it accurately describes the assistance used during Part 2. The declaration should remain truthful and should not claim work was independently created when AI assistance materially contributed.
