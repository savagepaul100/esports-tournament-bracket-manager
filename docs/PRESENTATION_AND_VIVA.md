# NEXUS ARENA — Presentation & Viva Voce Guide
## Course: CSE 213 (Advanced Java Programming)

---

### Slide 1: Title Slide
* **Title:** NEXUS ARENA: E-Sports Tournament & Single-Elimination Bracket Manager
* **Course:** CSE 213 – Advanced Java Programming
* **Team:** [Your Name / Roll Numbers]
* **Instructor:** [Faculty Name], Assistant Professor, Dept. of CSE, SRM University – AP

### Slide 2: Problem Statement & Motivation
* **Problem:** Manual esports tournament coordination via Excel/Discord leads to incorrect bracket seeding, disputed scores, lost match credentials, and delayed results.
* **Solution:** A Java 21 full-stack tournament platform with an Organizer Desktop GUI (JavaFX), a public spectator portal (HTML/JS), and a Spring Boot backend with Firebase Firestore.

### Slide 3: System Architecture
* **Frontend Clients:**
  1. Organizer Desktop Application (JavaFX with Gamer UI)
  2. Public Spectator Portal (HTML5, Responsive CSS, Live Polling)
* **Backend:** Spring Boot 3.3.4 (REST API & Business Logic Engine)
* **Storage Layer:** Dual-Mode DataStore (Google Cloud Firestore + Thread-Safe Local JSON Store)

### Slide 4: Single-Elimination Bracket Engine (Key Algorithm)
* Next power-of-two calculation: \(P = 2^{\lceil \log_2 N \rceil}\)
* Mathematical BYE allocation: \(\text{BYEs} = P - N\)
* Top-seeded teams receive BYE passage to Round 2.
* Winner progression links: `curr.nextMatchId` and `curr.nextMatchSlot` ("A" or "B").

### Slide 5: Advanced Java Concepts Implemented
1. **OOP Principles:** Polymorphic `DataStore`, encapsulated entities (`Tournament`, `Team`, `Match`).
2. **Java Streams & Lambdas:** Used for seed sorting, bracket filtering, and leaderboard calculation.
3. **Multithreading & Concurrency:** Synchronized methods and `ConcurrentHashMap` for race-free score submissions.
4. **Generics & Exception Handling:** Generic API envelopes (`ApiResponse<T>`) and domain validation.

### Slide 6: Organizer Operations & Security
* Room ID & Password distribution.
* Score recording with required audit notes.
* Dispute resolution workflow (Review clip evidence, confirm/reverse ruling).
* Misconduct reporting (Warn, suspend, or ban players).
* CSV Result Export for university records.

### Slide 7: Live Demonstration Walkthrough
1. Launch app with `start-all.bat`.
2. Inspect Valorant Cyber Masters 2026 live tree.
3. Enter score for Semifinal 2 (Paper Rex vs Fnatic) -> Watch winner advance to Grand Final!
4. Check spectator web portal at `http://localhost:8080`.
5. Download Windows organizer app directly from the website.

### Slide 8: Viva Q&A Quick Reference
* **Q: Why use an Interface for DataStore?**  
  *A: Abstraction and loose coupling. It allows seamless switching between Cloud Firestore and local persistence without changing any business logic in `TournamentService`.*
* **Q: How are Byes calculated?**  
  *A: By finding the smallest power of 2 greater than or equal to the team count \(N\). The difference \(2^k - N\) represents the number of top-seeded teams that bypass Round 1.*
* **Q: How does the web portal download the desktop app?**  
  *A: The Spring Boot backend exposes a static download endpoint (`/download/NexusArena-Organizer-Windows.zip`) containing the pre-packaged JavaFX fat JAR and launch scripts.*
