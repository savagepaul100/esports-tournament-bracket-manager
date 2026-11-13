# NEXUS ARENA — E-SPORTS TOURNAMENT & BRACKET MANAGER
## Comprehensive Project Report & Academic Documentation
**Course:** CSE 213 – Advanced Java Programming (III Semester B.Tech CSE)  
**Institution:** School of Engineering and Sciences, SRM University – AP  
**Academic Year:** 2026 – 2027  

---

## 1. Project Title and Background
### 1.1 Project Title
**NEXUS ARENA: E-Sports Tournament & Single-Elimination Bracket Management Engine**

### 1.2 Background
E-Sports competitions (e.g., Valorant, BGMI, Free Fire) require high-concurrency tournament logistics:
1. Dynamic team registration, captain management, and roster validation.
2. Single-elimination tree generation with mathematical BYE assignments for team sizes that are not powers of two.
3. Live match room scheduling, credential dissemination, and verified score recording.
4. Participant dispute handling with clip evidence verification and fair-play moderation penalties.
5. Real-time synchronization between an Organizer Desktop Command Center (JavaFX) and a Public Spectator Web Portal (HTML/CSS/JS) backed by Java Spring Boot and Google Cloud Firestore.

---

## 2. Problem Statement
Traditional university and collegiate tournament management relies on spreadsheets and chat groups, leading to:
* Erroneous bracket placement and unfair pairings.
* Inaccurate score recordings and delayed winner advancement.
* Lack of immutable audit trails for dispute resolution.
* Friction in distributing game room passwords and server regions.

**Nexus Arena** solves this by establishing a unified Java-centered architecture enforcing strict domain rules, mathematical bracket progressions, and multi-client real-time synchronization.

---

## 3. Objectives of the Project
### 3.1 Academic Objectives
1. Apply **Object-Oriented Programming (OOP)** principles: Encapsulation, Inheritance, Polymorphism, and Abstraction.
2. Implement **Advanced Java Concepts**:
   * Multithreading & concurrent data structures (`ConcurrentHashMap`, `synchronized`).
   * Java Streams & Lambda expressions for roster filtering, match grouping, and leaderboard ranking.
   * Custom Java Exception Handling and Generic DTO wrappers (`ApiResponse<T>`).
3. Build a desktop Graphical User Interface using **JavaFX** with a custom Cyberpunk Gamer Theme.
4. Connect Java Spring Boot backend with **Google Cloud Firestore** and portable fallback storage.

### 3.2 Functional Objectives
* Support tournament lifecycle: `DRAFT` → `REGISTRATION_OPEN` → `CHECK_IN` → `IN_PROGRESS` → `COMPLETED`.
* Allow organizers to generate brackets, manage byes, record scores, and download results in CSV.
* Allow participants and viewers to follow live brackets, view match schedules, submit disputes, and report misconduct.
* Provide an in-browser direct download link for the Windows desktop client application.

---

## 4. System Architecture & UML Design

```
+-------------------------------------------------------------+
|                  NEXUS ARENA ECOSYSTEM                      |
+-------------------------------------------------------------+
               │                                   │
               ▼                                   ▼
   [JavaFX Desktop App]               [Public Web Portal]
   (Organizer Command GUI)           (Spectators / Players)
               │                                   │
               └───────────────┬───────────────────┘
                               │ REST / HTTP
                               ▼
            +------------------------------------+
            |      Spring Boot Java Backend      |
            |     (Business Rules & Bracket)     |
            +------------------------------------+
                               │
            ┌──────────────────┴──────────────────┐
            ▼                                     ▼
 [Cloud Firestore (Firebase)]          [Local JSON DataStore]
 (Real-Time Cloud Sync)                (Offline Portable Storage)
```

### UML Class Hierarchy
* `DataStore` (Interface): Declares CRUD contracts for Users, Tournaments, Teams, Matches, Disputes, Conduct Reports, and Audit Logs.
  * `LocalJsonDataStore` (Class): Implements `DataStore` using Jackson JSON serialization with thread-safe maps.
  * `FirestoreDataStore` (Class): Implements `DataStore` using Google Cloud Firestore API with batching.
* `TournamentService`: Core business logic engine managing bracket generation, power-of-two bye allocation, score verification, and audit logging.

---

## 5. Implementation of Advanced Java Concepts

### 5.1 Java Streams and Lambdas
Used throughout the tournament engine for functional filtering, sorting, and transformations:
```java
// Filtering approved teams for bracket seeding
List<Team> teams = dataStore.findTeamsByTournamentId(tournamentId).stream()
        .filter(t -> t.getRegistrationStatus() == RegistrationStatus.APPROVED)
        .sorted(Comparator.comparingInt(t -> t.getSeed() != null ? t.getSeed() : 999))
        .collect(Collectors.toList());
```

### 5.2 Thread-Safe Concurrency & Synchronization
```java
// Preventing race conditions during simultaneous match result submissions
public synchronized Match recordMatchResult(String matchId, int scoreA, int scoreB, String reason, String actor) {
    // Validates scores and updates bracket tree safely
}
```

### 5.3 Generics & REST Responses
```java
public class ApiResponse<T> {
    public boolean success;
    public String message;
    public T data;
    // Generic static factory methods
    public static <T> ApiResponse<T> ok(T data) { return new ApiResponse<>(true, "Success", data); }
}
```

---

## 6. Testing and Verification
Unit tests in `TournamentServiceTest.java` verify:
1. **Bracket Generation with Byes:** 5 teams correctly generate an 8-slot single-elimination bracket with exactly 3 BYEs.
2. **Winner Advancement:** Confirming a match score auto-advances the winner into the Grand Final slot.

---

## 7. Conclusion and Future Enhancements
Nexus Arena demonstrates how modern Java 21, JavaFX, and Spring Boot can power competitive esports management with high reliability and zero setup friction.
* **Future Work:** Direct OAuth integration with game publisher APIs (Riot Games, Krafton) for automated lobby creation and live telemetry ingestion.
