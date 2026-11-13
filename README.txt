================================================================================
                    ⚡ NEXUS ARENA - ESPORTS TOURNAMENT SUITE ⚡
                          100% Cloud-Native Java 21 & Firebase
================================================================================

HOW TO RUN:
1. Double-click "Nexus Arena.lnk" or "NexusArena.bat"
   - Automatically detects Java 21 (installs Eclipse Temurin JDK 21 via winget if missing).
   - Connects directly to Google Cloud Firestore (nexusarena-esports).
   - Launches the Organizer Desktop Application (JavaFX Cyberpunk UI).
   - No local backend or localhost server required! Everything runs on cloud.

PUBLIC LIVE WEB PORTAL:
- https://nexusarena-esports.web.app
  (Anyone can open this URL in any mobile or desktop browser to view live brackets,
  standings, in-progress matches, and register teams in real time!)

PROTOTYPE DATA:
- Click "⚡ Seed Prototype Data" inside the desktop app (on Dashboard, Bracket, or Matches view)
  to populate Cloud Firestore with a live VCT Masters championship, in-progress live match (PRX 11-12 EG),
  completed quarter-finals, and active disputes!

DEMO CREDENTIALS:
- Organizer (Admin): username: "admin", password: "admin123"
- Participant (Captain): username: "tenz", password: "tenz123"
- Participant (Captain): username: "boaster", password: "boaster123"

PROJECT ARCHITECTURE:
- Desktop: Java 21, JavaFX 21, Google Cloud Firestore SDK (Native Cloud Client)
- Cloud Database: Google Cloud Firestore (Live Document Sync)
- Web Portal: HTML5, CSS3, JavaScript, Firebase Hosting & Firestore SDK
- Build Tool: Apache Maven (Wrapper included)
================================================================================
