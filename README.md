# NEXUS ARENA — E-Sports Tournament & Bracket Manager

A full-stack, Java-centered tournament and bracket management platform built for academic evaluation, featuring an ultra-sleek **Gamer UI** aesthetic.

---

## ⚡ Quick Start (Zero Setup Needed)

To run the complete system on **any Windows device**:

1. Extract the `.zip` archive.
2. Double-click **`start-all.bat`**.

That's it!
* **Automated Package Setup**: If Java 21 is not installed on the computer, `start-all.bat` will automatically trigger Windows Package Manager (`winget`) to install **Eclipse Temurin JDK 21** without needing to search online.
* **Backend**: Starts automatically at `http://localhost:8080`.
* **Public Gamer Website**: Automatically opens in your default browser.
* **Organizer Desktop App**: Launches directly on screen in dark Cyberpunk Gamer UI.

---

## 🎮 Technology Stack

- **Backend**: Java 21 LTS, Spring Boot 3.3.4, Spring Web, Validation, Jackson.
- **Organizer Desktop App**: JavaFX 21 (Shaded Fat JAR with custom `gamer-theme.css`).
- **Public Portal**: HTML5, Responsive CSS3 (Glassmorphism & Neon Cyberpunk Gamer UI), Vanilla JavaScript.
- **Database & Persistence**:
  - **Local Portable Engine**: Thread-safe atomic JSON file storage in `data/`, pre-seeded with rich demo tournaments (Valorant, Battlegrounds Mobile, Free Fire).
  - **Cloud Firestore & Firebase Auth**: Plug-and-play connector. Simply drop your `firebase-service-account.json` into the root folder to connect live to Google Cloud.

---

## 👥 User Roles & Demo Accounts

| Role | Username | Password | Capabilities |
| :--- | :--- | :--- | :--- |
| **Organizer** | `admin` | `admin123` | Create tournaments, approve/reject registrations, generate brackets, set match room IDs/passwords, enter/correct scores, resolve disputes, issue conduct penalties, export CSV. |
| **Participant** | `tenz` | `tenz123` | Register teams, check in, view room codes, submit match result disputes, report toxic conduct or suspected cheating. |
| **Viewer** | `spectator` | `fan123` | Browse live brackets, inspect standings, view match schedules and confirmed results in real time. |

---

## 🌐 How to Deploy to the Web (Step-by-Step)

If you wish to deploy the project online so anyone worldwide can access it via a public URL:

### Step 1: Deploy the Public Website to Firebase Hosting
1. Install Firebase CLI (if not installed): `npm install -g firebase-tools`
2. Go to the [Firebase Console](https://console.firebase.google.com/) and create a project (e.g. `nexus-arena-esports`).
3. Inside this project folder, run:
   ```bash
   firebase login
   firebase init hosting
   ```
   - When asked for public directory, enter: `public-web`
   - Configure as single-page app: `Yes`
4. Run:
   ```bash
   firebase deploy --only hosting
   ```
   Firebase will give you a live URL: `https://nexus-arena-esports.web.app`.

### Step 2: Deploy Spring Boot Backend to Google Cloud Run (Free Tier)
Because Firebase runs on Google Cloud Platform, your Firebase project is already a GCP project!
1. Containerize the backend using the Dockerfile or deploy directly with Google Cloud SDK:
   ```bash
   gcloud run deploy nexus-backend --source backend --region asia-south1 --allow-unauthenticated
   ```
2. In `public-web/app.js`, change `const API_BASE = window.location.origin;` to your Cloud Run URL (e.g. `https://nexus-backend-xyz.run.app`).

### Step 3: Distribute Organizer Desktop App
The organizer application is a native desktop application. Organizers download `desktop-1.0.0.jar` and run `start-desktop.bat`.

---

## 📁 Project Structure

```
├── backend/                  # Spring Boot REST API & Embedded Web Server
│   ├── src/main/java/        # Domain models, single-elimination bracket engine, controllers
│   ├── src/main/resources/   # Static Gamer Web Portal (HTML/CSS/JS) & config
│   └── target/               # Compiled backend-1.0.0.jar
├── desktop/                  # JavaFX Organizer Desktop GUI
│   ├── src/main/java/        # Desktop views (Dashboard, Brackets, Matches, Disputes)
│   ├── src/main/resources/   # gamer-theme.css stylesheet
│   └── target/               # Compiled desktop-1.0.0.jar
├── public-web/               # Standalone web portal for Firebase Hosting deploy
├── data/                     # Local persistence JSON database (auto-seeded)
├── start-all.bat             # 1-Click Windows launcher with auto-installer
├── start-backend.bat         # Standalone backend launcher
├── start-desktop.bat         # Standalone desktop launcher
├── mvnw.cmd                  # Portable Maven wrapper
└── pom.xml                   # Root Maven multi-module definition
```
