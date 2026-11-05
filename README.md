SPRINTA - Athletic Tracking Platform
Author: Darren Keanan Skippers
Student Number: ST10455119
Module: OPSC 6312 (Open Source Coding) - Final POE

Demonstration Video
https://youtu.be/vWj6FzPf1Fw?si=EH9CcQ6gXJaFaiJO

Purpose of the App
Sprinta is a high-performance athletic activity tracking platform designed for multi-sport GPS logging, offline telemetry synchronization, and personal record benchmarking. The purpose of this application is to provide runners and fitness enthusiasts with a robust, reliable tool to track their workouts accurately, even in areas without cellular reception. It bridges the gap between simple step-counters and complex, premium fitness applications.

Design Considerations:

Offline-First Architecture: Because runners often lose signal, Sprinta is designed to save all telemetry directly to a local Room (SQLite) database first.

MVVM Architecture: The codebase strictly separates concerns. The UI (Jetpack Compose) only observes state, while the MainViewModel handles logic and the SprintaRepository manages data routing.

UI/UX: The app utilizes a strict Material Design 3 dark theme palette (SprintaBlack, SprintaDarkSurface, SprintaWhite, and SprintaGold) to ensure high contrast, readability in sunlight, and a premium aesthetic. Custom vector graphics were implemented to replace generic system emojis.

GitHub and GitHub Actions Utilization:

Version Control: Git and GitHub were utilized continuously throughout development to maintain a history of changes, allowing for safe refactoring of the GPS engine and UI.

GitHub Actions: Automated testing and continuous integration (CI) workflows were set up using GitHub Actions to automatically build the Android application and run basic tests on every push to the main branch, ensuring that no breaking changes were introduced to the compilation process.

Final Tag: The final, stable version of this repository has been explicitly tagged as final PoE.

Release Notes: Updates Since Prototype
This final version includes massive architectural and UI overhauls compared to the initial prototype:

Innovative Feature - Dynamic Gear Tracking: Removed hardcoded shoes. Users can now add custom running shoes to a Room database, and the app automatically increments the shoe's mileage every time a run is saved.

Innovative Feature - Advanced GPS Kalman Filtering: Completely rewrote the location engine using the Fused Location Provider. Added mathematical algorithms to drop inaccurate coordinates (worse than 10m accuracy) and ignore impossible speeds to prevent route jitter.

Authentication Overhaul: Fixed a critical bug causing invalid credential crashes by sanitizing inputs. Fully implemented modern Google Single Sign-On (SSO) using the Android Credential Manager API.

Background Syncing: Implemented Android WorkManager to automatically detect when the user regains internet connection and sync pending local Room database workouts to the Firebase backend.

Dynamic Weekly Goal: Added an interactive weekly goal setter to the dashboard that saves to AppPreferences and dynamically updates a progress bar.

UI Polish: Fixed bottom navigation bar text wrapping, overhauled the tracking HUD to feature a massive timer (Strava-style), and corrected contrast issues on the Settings screen.

Preparation for Publishing
As required for publication readiness, this repository contains:

A signed release .apk / .aab file (Located in the /release folder).

High-resolution screenshots formatted for the Google Play Store (Located in the /assets/play_store folder).

AI Tools Usage & Citation:
During the development of Sprinta, Artificial Intelligence tools (specifically Google Gemini) were utilized as an advanced consultation and pair-programming resource. AI was not used to blindly generate the application, but rather to accelerate boilerplate creation, debug complex SDK errors, and refine mathematical algorithms.

Debugging and Error Resolution: When implementing Firebase Authentication, I encountered persistent FirebaseAuthInvalidCredentials exceptions. I provided the error logs to the AI, which correctly identified that the Android software keyboard was appending trailing spaces to email inputs. The AI suggested the .trim() solution which immediately resolved the crash.

Architectural Consultation: I conceptualized the need for an offline-first architecture to handle dropped signals. I consulted the AI on the best modern Android practices for this, and it guided me toward pairing a local Room (SQLite) database with the Android WorkManager API for background syncing. I used the AI to help scaffold the Room DAO (Data Access Object) queries and the WorkManager worker class constraints.

Mathematical Algorithms: To fix the GPS jitter (where the map polyline would zig-zag wildly), I requested the AI to help me write a spatial filtering algorithm. The AI provided the Haversine formula implementation in Kotlin to calculate speed between two coordinates and filter out impossible human velocities (e.g., > 54 km/h).

UI Generation: I utilized AI image generation tools to brainstorm the minimalist, line-art vector graphics used for the Run, Walk, and Workout icons. Furthermore, I used Gemini to help refactor my Jetpack Compose layouts, specifically asking it for the best modifiers to prevent text truncation in the NavigationBar.

All generated code snippets were manually reviewed, adapted to fit the MVVM architecture of Sprinta, and tested on physical hardware.
