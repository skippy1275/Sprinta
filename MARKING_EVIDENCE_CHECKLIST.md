# SPRINTA POE Part 2 — Marking Evidence Checklist

## App runs on mobile device — 5 marks
- Install APK on phone
- Launch app
- Register/login
- Navigate between screens
- Complete one run without a crash

## Sign in — 10 marks
- Register screen
- Required-field validation
- Email validation
- Strong-password validation
- Password mismatch validation
- Successful registration
- Successful login
- Invalid credentials message
- BCrypt hash visible in PostgreSQL (never the plain password)

## Settings — 10 marks
- Light/dark theme changes
- Kilometres/miles changes
- Audio interval changes
- English/isiZulu/Afrikaans changes
- Settings persist after restart

## Creation/use of REST API — 10 marks
- Hosted API URL
- `/health` response
- Register request
- Login request
- Workout sync request
- PostgreSQL data visible

## REST API integration — 10 marks
- Android registers/logs in against hosted API
- Android sends workout data
- Android retrieves workout history
- Authorization header/JWT is used
- API errors are handled

## User Defined Feature 1 — 10 marks
- Start run
- GPS points are recorded
- Live distance/time/pace
- Google Maps route displayed
- Stop and save

## User Defined Feature 2 — 10 marks
- Disable internet
- Save completed run
- Show pending local data
- Restore internet
- WorkManager synchronises
- Database receives workout

## User Defined Feature 3 — 10 marks
- Complete a qualifying workout
- Compare against previous record
- Detect faster performance
- Display new personal record
- Save record locally

## User interface — 10 marks
- Consistent SPRINTA branding
- Consistent spacing and typography
- Clear navigation
- Readable cards/buttons
- Dark/light mode
- No overlapping or clipped controls
- Helpful error messages

## GitHub, README and automated testing — 10 marks
- Public/private repository as required by lecturer
- README complete
- Images/screenshots in README
- Multiple meaningful commits
- `.github/workflows/build.yml`
- Successful Actions run
- Unit tests pass
- Complete Kotlin source uploaded

## Demonstration video — 5 marks
- Physical phone shown
- Voice-over included
- Registration/login shown
- Settings shown
- REST API/database shown
- User-defined features shown
- GitHub/Actions shown
- Professional presentation
