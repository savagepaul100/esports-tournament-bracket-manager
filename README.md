# NEXUS ARENA — Cloud-Native E-Sports Tournament & Bracket Manager

A full-stack, Java 21 tournament and bracket management platform built for academic evaluation, featuring an ultra-sleek **Gamer UI** aesthetic and **100% Cloud Architecture** powered by Google Cloud Firestore.

---

## ⚡ Quick Start (Zero Setup Needed)

To run the complete system on **any Windows device**:

1. Double-click **`Nexus Arena.lnk`** or **`NexusArena.bat`** in the project root.
2. The launcher automatically checks for Java 21 (installs Eclipse Temurin JDK 21 via `winget` if missing).
3. The **Organizer Desktop Suite** immediately opens on screen, connected live to **Cloud Firestore**!
4. **No local backend or localhost server needed!** Everything synchronizes through the cloud.

---

## 🌐 Public Live Web Portal

- **Official Web Portal URL**: [https://nexusarena-esports.web.app](https://nexusarena-esports.web.app)
- Participants, fans, and organizers worldwide can view live tournament brackets, in-progress match scores, standings, and register teams in real time from any browser (desktop or mobile).
- The web portal also features a direct download link for the Windows Organizer Suite.

---

## 🎮 Technology Stack

- **Organizer Desktop App**: Java 21 LTS, JavaFX 21, Google Cloud Firestore SDK (Native Cloud Client, shaded fat JAR with custom `gamer-theme.css`).
- **Cloud Database**: Google Cloud Firestore (Real-time live multi-device synchronization).
- **Public Portal**: HTML5, Responsive CSS3 (Glassmorphism & Neon Cyberpunk Gamer UI), Vanilla JavaScript, Firebase SDK.
- **Distribution**: Standalone Windows package with launcher shortcut.

---

## 👥 User Roles & Demo Accounts

| Role | Username | Password | Capabilities |
| :--- | :--- | :--- | :--- |
| **Organizer** | `admin` | `admin123` | Create tournaments, approve/reject registrations, generate single-elimination brackets, schedule rooms, set matches LIVE, record verified scores, advance winners, resolve disputes, issue fair-play penalties. |
| **Participant** | `tenz` | `tenz123` | Register teams, check in, view room codes, submit match result disputes, report conduct incidents. |
| **Participant** | `boaster` | `boaster123` | Captain check-in, inspect opponent roster, view real-time brackets. |
| **Spectator** | Any | None | Browse live brackets, inspect standings, view real-time match scores on the web. |

---

## ⚡ Prototype Data & Live Matches

Click the **"⚡ Seed Prototype Data"** button in the desktop app (available on Dashboard, Bracket, and Matches views) to populate Cloud Firestore with:
* **VALORANT Champions Tour: Masters Tokyo**:
  - 8 Tier-1 teams (Sentinels, Fnatic, Paper Rex, Team Liquid, DRX, LOUD, Evil Geniuses, EDward Gaming).
  - 4 Completed Quarter-Final matches with scores (13-9, 13-7, 13-11, 13-10).
  - Completed Semi-Final 1 (Sentinels 13 - 11 Fnatic).
  - **🔥 LIVE Ongoing Semi-Final 2**: Paper Rex 11 - 12 Evil Geniuses (tension match in progress on Lotus!).
  - Grand Final: Sentinels vs TBD (scheduled for tonight).
* **Apex Predators: Battle Royale Showdown** (Lobby placements & kill points).
* **BGMI All-Stars Community Cup** (Open registration stage).
* Real-time fair-play disputes, conduct reports, and security audit trail logs.

---

## 📁 Clean Project Structure

```
├── desktop/                  # JavaFX Organizer Desktop GUI (Cloud-Native)
│   ├── src/main/java/        # Cloud Firestore Client, views (Dashboard, Brackets, Matches, Disputes)
│   ├── src/main/resources/   # gamer-theme.css & bundled firebase credentials
│   └── target/               # Compiled desktop-1.0.0.jar (Shaded fat JAR)
├── public-web/               # Standalone web portal deployed on Firebase Hosting
│   ├── index.html            # Public web interface
│   ├── style.css             # Neon Cyberpunk Gamer theme
│   ├── app.js                # Live Cloud Firestore client with real-time onSnapshot
│   └── download/             # Organizer Desktop distribution archive
├── NexusArena.bat            # 1-Click root launcher (runs desktop app directly)
├── Nexus Arena.lnk           # Windows desktop shortcut
├── firebase.json             # Firebase Hosting configuration
├── firestore.rules           # Cloud Firestore security rules
├── mvnw.cmd                  # Portable Maven wrapper
└── pom.xml                   # Root Maven project definition
```
