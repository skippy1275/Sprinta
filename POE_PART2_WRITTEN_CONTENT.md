# SPRINTA — POE Part 2 Written Content

## 1. Purpose of the Application

SPRINTA is a native Android running application developed using Kotlin and Android Studio. The application is designed to help runners track outdoor workouts using GPS, review running statistics, save workout information locally and synchronise data with an online REST API and database. The prototype builds directly on the SPRINTA design established in POE Part 1.

## 2. Design Considerations

The design follows a layered architecture to separate the user interface, application logic, local persistence and remote communication. Room is used for local workout storage, while Retrofit communicates with the REST API. WorkManager is used for background synchronisation when network connectivity becomes available. Jetpack DataStore stores user preferences such as distance units, audio update frequency, language and theme. A foreground location service allows GPS tracking to continue while the device is locked or the application is not the active screen.

## 3. User Defined Features

### Feature 1: Real-Time GPS Tracking

The application collects high-accuracy location data, calculates distance and pace, and draws a route on Google Maps. The tracking screen displays distance, time and pace in real time.

### Feature 2: Offline Synchronisation

When internet access is unavailable, completed workout data remains safely stored in Room. WorkManager waits for a connected network before sending pending workout data to the REST API. Successful synchronisation updates the local record as synchronised.

### Feature 3: Personal Record Detection

After a workout is saved, SPRINTA evaluates the workout against stored personal records for 1 km, 5 km and 10 km distances. A faster pace creates a new record and the application informs the user through an achievement message.

## 4. REST API and Database

The REST API is implemented using Node.js and Express. PostgreSQL stores user accounts, password hashes, workouts and route points. Registration and login are secured using BCrypt password hashing and JWT authentication. Workout data is submitted through the synchronisation endpoint and retrieved through the user workout history endpoint.

## 5. GitHub and GitHub Actions

The complete source code is maintained in a GitHub repository. Meaningful commits are used throughout development to show progress. GitHub Actions automatically executes automated tests and builds the Android application after code changes. This provides continuous verification that the application remains buildable and that the tested application logic continues to pass.

## 6. Testing and Validation

Validation is implemented for required fields, email addresses and passwords. GPS and workout calculation logic is covered by unit tests. Personal-record detection is also tested. Network and server errors are handled without crashing the application. Pending offline workouts are retained locally when synchronisation fails and are retried by WorkManager.

## 7. Demonstration

The final video demonstrates the prototype operating on a physical Android mobile device. The demonstration covers registration and login, settings, REST API communication, database storage, GPS tracking, workout saving, offline operation, background synchronisation, personal records, GitHub commits and automated testing. A voice-over explains each feature and the relevant technical implementation.

## 8. AI Declaration

I, Darren Keanan Skippers, confirm that AI tools, including ChatGPT, were used as support during the preparation of POE Part 2. The assistance included proofreading, improving structure and wording, explaining programming concepts, generating and refining code examples, debugging assistance, and helping organise the project documentation. I reviewed the suggested content, adapted it to the SPRINTA application and remain responsible for the final implementation, testing, documentation and submission. I did not use AI as a substitute for understanding the code or the assessment requirements. All final work was reviewed by me before submission.
