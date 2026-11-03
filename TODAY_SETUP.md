# SPRINTA POE Part 2 — Today Setup

## 1. Android Studio
Open the `SPRINTA_POE_PART2` folder in Android Studio. Allow Gradle sync to finish.

Create `local.properties` in the project root from `local.properties.example`:

```properties
SPRINTA_API_BASE_URL=https://YOUR-SPRINTA-API.onrender.com/
MAPS_API_KEY=YOUR_GOOGLE_MAPS_API_KEY
```

Use a real public HTTPS API URL and a restricted Google Maps Android API key.

## 2. Host the API
On Render, create a Web Service from the `api` folder:
- Build command: `npm install`
- Start command: `npm start`

Create a PostgreSQL database and set the API environment variables:
- `DATABASE_URL` = Render PostgreSQL connection string
- `JWT_SECRET` = long random secret
- `CORS_ORIGIN` = `*` for a simple demo, or your allowed origin

Test the deployed API at:
`https://YOUR-SPRINTA-API.onrender.com/health`

## 3. Android phone
Enable Developer Options + USB debugging, connect the physical Android phone, select it in Android Studio and press Run.

Allow location permission when prompted. Start a run, move a short distance, stop, add notes and save.

## 4. Offline demonstration
After a successful test run, disable mobile data/Wi-Fi, save another run, and show the workout as Pending. Restore internet and trigger synchronisation.

## 5. GitHub
From the project root:

```bash
git init
git add .
git commit -m "Initial SPRINTA POE Part 2 project"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/YOUR_REPO.git
git push -u origin main
```

Continue with meaningful commits during development. Do not upload `local.properties`.

## 6. Video
Record on the physical phone. Include a voice-over and show registration/login, settings, REST API/database, GPS run, offline sync, personal records, GitHub commits and GitHub Actions.

## Important
The ZIP supplied with this project is for transfer/convenience only. The POE brief says the final GitHub submission must contain the complete source code and that ZIP files are not accepted as the submitted project. Extract the project and push the actual folders/files to GitHub.
