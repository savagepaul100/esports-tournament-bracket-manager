package com.esports.desktop.client;

import com.esports.desktop.model.*;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 100% Cloud-Native Firestore Client for Nexus Arena Desktop App.
 * Directly manages Tournaments, Teams, Rosters, Live Kills/Scores, Disputes, and Audit Trail.
 */
public class FirebaseClient {

    private static FirebaseClient instance;
    private final Firestore db;

    private FirebaseClient() throws Exception {
        InputStream serviceAccount = getClass().getClassLoader()
                .getResourceAsStream("firebase-service-account.json");
        if (serviceAccount == null) {
            serviceAccount = FirebaseClient.class.getResourceAsStream("/firebase-service-account.json");
        }
        if (serviceAccount == null) {
            File f = new File("firebase-service-account.json");
            if (f.exists()) {
                serviceAccount = new FileInputStream(f);
            }
        }
        if (serviceAccount == null) {
            File f = new File("desktop/src/main/resources/firebase-service-account.json");
            if (f.exists()) {
                serviceAccount = new FileInputStream(f);
            }
        }
        if (serviceAccount == null) {
            throw new IllegalStateException("firebase-service-account.json not found in resources or application directory. Cannot connect to Firebase.");
        }
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setProjectId("nexusarena-esports")
                    .build();
            FirebaseApp.initializeApp(options);
        }
        this.db = FirestoreClient.getFirestore();
    }

    public static synchronized FirebaseClient getInstance() throws Exception {
        if (instance == null) {
            instance = new FirebaseClient();
        }
        return instance;
    }

    public Firestore getDb() {
        return db;
    }

    // ==================== TOURNAMENTS ====================

    public List<TournamentDto> getTournaments() {
        try {
            List<QueryDocumentSnapshot> docs = db.collection("tournaments").get().get().getDocuments();
            return docs.stream().map(this::docToTournament).collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Firestore getTournaments error: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public TournamentDto createTournament(TournamentDto t, String actor) {
        try {
            String id = t.getId() != null && !t.getId().isBlank() ? t.getId() : "trn-" + UUID.randomUUID().toString().substring(0, 8);
            Map<String, Object> data = new HashMap<>();
            data.put("id", id);
            data.put("name", t.getName());
            data.put("description", t.getDescription() != null ? t.getDescription() : "");
            data.put("game", t.getGame() != null ? t.getGame() : "VALORANT");
            data.put("mode", t.getMode() != null ? t.getMode() : "SINGLE_ELIMINATION_TDM");
            data.put("status", t.getStatus() != null ? t.getStatus() : "REGISTRATION_OPEN");
            data.put("teamSize", t.getTeamSize() > 0 ? t.getTeamSize() : 5);
            data.put("maxParticipants", t.getMaxParticipants() > 0 ? t.getMaxParticipants() : 8);
            data.put("published", true);
            data.put("rules", t.getRules() != null ? t.getRules() : "Standard competitive rules apply. Anti-cheat screen recording mandatory.");
            data.put("tournamentStartDate", t.getTournamentStartDate() != null ? t.getTournamentStartDate() : Instant.now().toString());
            data.put("createdAt", Instant.now().toString());
            data.put("createdBy", actor);

            db.collection("tournaments").document(id).set(data).get();
            addAuditLog(actor, "CREATE_TOURNAMENT", "tournament", id, "Created tournament: " + t.getName() + " (" + t.getGame() + ")");

            t.setId(id);
            t.setStatus((String) data.get("status"));
            return t;
        } catch (Exception e) {
            System.err.println("Firestore createTournament error: " + e.getMessage());
            return null;
        }
    }

    public boolean updateTournamentStatus(String tournamentId, String status, String actor) {
        try {
            db.collection("tournaments").document(tournamentId)
                    .update("status", status, "updatedAt", Instant.now().toString()).get();
            addAuditLog(actor, "UPDATE_STATUS", "tournament", tournamentId, "Status → " + status);
            return true;
        } catch (Exception e) {
            System.err.println("Firestore updateTournamentStatus error: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteTournament(String tournamentId, String actor) {
        try {
            // Delete matches
            List<QueryDocumentSnapshot> matches = db.collection("matches").whereEqualTo("tournamentId", tournamentId).get().get().getDocuments();
            for (QueryDocumentSnapshot m : matches) m.getReference().delete();

            // Delete teams
            List<QueryDocumentSnapshot> teams = db.collection("teams").whereEqualTo("tournamentId", tournamentId).get().get().getDocuments();
            for (QueryDocumentSnapshot tm : teams) tm.getReference().delete();

            // Delete disputes
            List<QueryDocumentSnapshot> disps = db.collection("disputes").whereEqualTo("tournamentId", tournamentId).get().get().getDocuments();
            for (QueryDocumentSnapshot d : disps) d.getReference().delete();

            // Delete conduct reports
            List<QueryDocumentSnapshot> rpts = db.collection("conduct_reports").whereEqualTo("tournamentId", tournamentId).get().get().getDocuments();
            for (QueryDocumentSnapshot r : rpts) r.getReference().delete();

            // Delete tournament
            db.collection("tournaments").document(tournamentId).delete().get();
            addAuditLog(actor, "DELETE_TOURNAMENT", "tournament", tournamentId, "Deleted tournament and all linked records");
            return true;
        } catch (Exception e) {
            System.err.println("Firestore deleteTournament error: " + e.getMessage());
            return false;
        }
    }

    // ==================== TEAMS & ROSTERS ====================

    public List<TeamDto> getTeams(String tournamentId) {
        try {
            Query q = db.collection("teams").whereEqualTo("tournamentId", tournamentId);
            List<QueryDocumentSnapshot> docs = q.get().get().getDocuments();
            return docs.stream().map(this::docToTeam).collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Firestore getTeams error: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public boolean registerTeam(TeamDto team, String actor) {
        try {
            String id = team.getId() != null && !team.getId().isBlank() ? team.getId() : "tm-" + UUID.randomUUID().toString().substring(0, 8);
            Map<String, Object> data = new HashMap<>();
            data.put("id", id);
            data.put("tournamentId", team.getTournamentId());
            data.put("name", team.getName());
            data.put("tag", team.getTag() != null ? team.getTag() : "");
            data.put("password", team.getPassword() != null && !team.getPassword().isBlank() ? team.getPassword() : "TeamPass@2026");
            data.put("captainId", team.getCaptainId() != null ? team.getCaptainId() : actor);
            data.put("captainEmail", team.getCaptainEmail() != null ? team.getCaptainEmail() : actor + "@nexusarena.gg");
            data.put("playerNames", team.getPlayerNames() != null ? team.getPlayerNames() : Collections.emptyList());
            data.put("checkedIn", team.isCheckedIn());
            data.put("seed", team.getSeed() != null ? team.getSeed() : 1);
            data.put("registrationStatus", team.getRegistrationStatus() != null ? team.getRegistrationStatus() : "APPROVED");
            data.put("registeredAt", Instant.now().toString());

            db.collection("teams").document(id).set(data).get();
            addAuditLog(actor, "REGISTER_TEAM", "team", id, "Registered team " + team.getName() + " with password protection");
            return true;
        } catch (Exception e) {
            System.err.println("Firestore registerTeam error: " + e.getMessage());
            return false;
        }
    }

    public boolean verifyTeamPassword(String teamId, String password) {
        try {
            DocumentSnapshot doc = db.collection("teams").document(teamId).get().get();
            if (!doc.exists()) return false;
            String stored = str(doc, "password");
            if (stored.isBlank()) return true; // no password required if legacy/unset
            return stored.equals(password);
        } catch (Exception e) {
            System.err.println("Firestore verifyTeamPassword error: " + e.getMessage());
            return false;
        }
    }

    public boolean updateTeam(String teamId, Map<String, Object> updates, String actor) {
        try {
            updates.put("updatedAt", Instant.now().toString());
            db.collection("teams").document(teamId).update(updates).get();
            addAuditLog(actor, "UPDATE_TEAM", "team", teamId, "Updated team details: " + updates.keySet());
            return true;
        } catch (Exception e) {
            System.err.println("Firestore updateTeam error: " + e.getMessage());
            return false;
        }
    }

    public boolean removePlayerFromTeam(String teamId, String playerName, String actor) {
        try {
            DocumentSnapshot doc = db.collection("teams").document(teamId).get().get();
            if (!doc.exists()) return false;
            Object obj = doc.get("playerNames");
            if (obj instanceof List) {
                List<String> list = new ArrayList<>();
                for (Object item : (List<?>) obj) {
                    String p = String.valueOf(item);
                    if (!p.equalsIgnoreCase(playerName.trim())) {
                        list.add(p);
                    }
                }
                db.collection("teams").document(teamId).update("playerNames", list, "updatedAt", Instant.now().toString()).get();
                addAuditLog(actor, "REMOVE_PLAYER", "team", teamId, "Removed player " + playerName + " from roster");
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Firestore removePlayerFromTeam error: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteTeam(String teamId, String actor) {
        try {
            db.collection("teams").document(teamId).delete().get();
            addAuditLog(actor, "DELETE_TEAM", "team", teamId, "Deleted team from Cloud Firestore");
            return true;
        } catch (Exception e) {
            System.err.println("Firestore deleteTeam error: " + e.getMessage());
            return false;
        }
    }

    public boolean updateTeamStatus(String teamId, String status, String reason, String actor) {
        try {
            db.collection("teams").document(teamId)
                    .update("registrationStatus", status, "statusReason", reason != null ? reason : "", "updatedAt", Instant.now().toString()).get();
            addAuditLog(actor, "UPDATE_TEAM_STATUS", "team", teamId, "Status → " + status + (reason != null ? " | " + reason : ""));
            return true;
        } catch (Exception e) {
            System.err.println("Firestore updateTeamStatus error: " + e.getMessage());
            return false;
        }
    }

    public boolean setTeamCheckIn(String teamId, boolean checkedIn, String actor) {
        try {
            db.collection("teams").document(teamId)
                    .update("checkedIn", checkedIn, "checkInTime", Instant.now().toString()).get();
            addAuditLog(actor, checkedIn ? "CHECK_IN" : "CHECK_OUT", "team", teamId, "Check-in: " + checkedIn);
            return true;
        } catch (Exception e) {
            System.err.println("Firestore setTeamCheckIn error: " + e.getMessage());
            return false;
        }
    }

    // ==================== MATCHES & BRACKET ====================

    public List<MatchDto> getMatches(String tournamentId) {
        try {
            Query q = db.collection("matches").whereEqualTo("tournamentId", tournamentId);
            List<QueryDocumentSnapshot> docs = q.get().get().getDocuments();
            List<MatchDto> matches = docs.stream().map(this::docToMatch).collect(Collectors.toList());
            matches.sort(Comparator.comparingInt(MatchDto::getRound).thenComparingInt(MatchDto::getMatchNumber));
            return matches;
        } catch (Exception e) {
            System.err.println("Firestore getMatches error: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public boolean deleteMatch(String matchId, String actor) {
        try {
            db.collection("matches").document(matchId).delete().get();
            addAuditLog(actor, "DELETE_MATCH", "match", matchId, "Deleted match record");
            return true;
        } catch (Exception e) {
            System.err.println("Firestore deleteMatch error: " + e.getMessage());
            return false;
        }
    }

    public List<MatchDto> generateBracket(String tournamentId, String actor) {
        try {
            List<TeamDto> teams = getTeams(tournamentId);
            List<TeamDto> approved = teams.stream()
                    .filter(t -> "APPROVED".equals(t.getRegistrationStatus()) || t.getRegistrationStatus() == null)
                    .collect(Collectors.toList());

            if (approved.size() < 2) {
                System.err.println("Not enough approved teams to generate bracket.");
                return getMatches(tournamentId);
            }

            Collections.shuffle(approved);

            int numTeams = approved.size();
            int bracketSize = 1;
            int totalRounds = 0;
            while (bracketSize < numTeams) {
                bracketSize *= 2;
                totalRounds++;
            }
            if (bracketSize < 2) {
                bracketSize = 2;
                totalRounds = 1;
            }

            // Delete existing matches for this tournament
            List<QueryDocumentSnapshot> existing = db.collection("matches")
                    .whereEqualTo("tournamentId", tournamentId).get().get().getDocuments();
            for (QueryDocumentSnapshot d : existing) {
                d.getReference().delete();
            }

            List<MatchDto> created = new ArrayList<>();

            // Round 1 matches
            int r1MatchCount = bracketSize / 2;
            for (int i = 0; i < r1MatchCount; i++) {
                TeamDto teamA = (i * 2 < approved.size()) ? approved.get(i * 2) : null;
                TeamDto teamB = (i * 2 + 1 < approved.size()) ? approved.get(i * 2 + 1) : null;

                String matchId = "match-" + tournamentId.replace("trn-", "") + "-r1-m" + (i + 1);
                String roundName = getRoundName(bracketSize);

                Map<String, Object> data = new HashMap<>();
                data.put("id", matchId);
                data.put("tournamentId", tournamentId);
                data.put("round", 1);
                data.put("matchNumber", i + 1);
                data.put("roundName", roundName);
                data.put("mode", "5V5");
                data.put("teamAId", teamA != null ? teamA.getId() : "tbd");
                data.put("teamBId", teamB != null ? teamB.getId() : "tbd");
                data.put("teamAName", teamA != null ? teamA.getName() : "TBD");
                data.put("teamBName", teamB != null ? teamB.getName() : "TBD");
                data.put("scoreA", 0);
                data.put("scoreB", 0);
                data.put("killsA", 0);
                data.put("killsB", 0);
                data.put("status", "UPCOMING");
                data.put("scheduledTime", "Match Day 1 • 18:00 IST");
                data.put("roomId", "NX-LOBBY-" + (100 + i + 1));
                data.put("roomPassword", "arena" + (10 + i + 1));
                data.put("mapName", "Haven");
                data.put("createdAt", Instant.now().toString());

                db.collection("matches").document(matchId).set(data).get();
                created.add(docToMatch(data));
            }

            // Subsequent rounds (TBD placeholders)
            int currentRoundTeams = bracketSize / 2;
            int r = 2;
            while (currentRoundTeams >= 2) {
                int matchesInRound = currentRoundTeams / 2;
                String roundName = getRoundName(currentRoundTeams);

                for (int m = 1; m <= matchesInRound; m++) {
                    String matchId = "match-" + tournamentId.replace("trn-", "") + "-r" + r + "-m" + m;

                    Map<String, Object> data = new HashMap<>();
                    data.put("id", matchId);
                    data.put("tournamentId", tournamentId);
                    data.put("round", r);
                    data.put("matchNumber", m);
                    data.put("roundName", roundName);
                    data.put("mode", "5V5");
                    data.put("teamAId", "tbd");
                    data.put("teamBId", "tbd");
                    data.put("teamAName", "TBD");
                    data.put("teamBName", "TBD");
                    data.put("scoreA", 0);
                    data.put("scoreB", 0);
                    data.put("killsA", 0);
                    data.put("killsB", 0);
                    data.put("status", "UPCOMING");
                    data.put("scheduledTime", "Match Day " + r + " • 20:00 IST");
                    data.put("roomId", "NX-LOBBY-" + (200 + m));
                    data.put("roomPassword", "arena" + (20 + m));
                    data.put("mapName", r == totalRounds ? "Ascent" : "Bind");
                    data.put("createdAt", Instant.now().toString());

                    db.collection("matches").document(matchId).set(data).get();
                    created.add(docToMatch(data));
                }
                currentRoundTeams /= 2;
                r++;
            }

            updateTournamentStatus(tournamentId, "IN_PROGRESS", actor);
            addAuditLog(actor, "GENERATE_BRACKET", "tournament", tournamentId,
                    "Generated single-elimination bracket with " + created.size() + " matches across " + (r - 1) + " rounds");

            return created;
        } catch (Exception e) {
            System.err.println("Firestore generateBracket error: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private String getRoundName(int teamsInStage) {
        switch (teamsInStage) {
            case 2: return "Grand Final";
            case 4: return "Semi-Finals";
            case 8: return "Quarter-Finals";
            case 16: return "Round of 16";
            case 32: return "Round of 32";
            default: return "Round of " + teamsInStage;
        }
    }

    public boolean updateMatchSchedule(String matchId, String time, String roomId, String password, String map, String actor) {
        try {
            Map<String, Object> updates = new HashMap<>();
            if (time != null && !time.isBlank()) updates.put("scheduledTime", time);
            if (roomId != null && !roomId.isBlank()) updates.put("roomId", roomId);
            if (password != null && !password.isBlank()) updates.put("roomPassword", password);
            if (map != null && !map.isBlank()) updates.put("mapName", map);
            updates.put("updatedAt", Instant.now().toString());

            db.collection("matches").document(matchId).update(updates).get();
            addAuditLog(actor, "SCHEDULE_MATCH", "match", matchId, "Scheduled: room=" + roomId + " map=" + map);
            return true;
        } catch (Exception e) {
            System.err.println("Firestore updateMatchSchedule error: " + e.getMessage());
            return false;
        }
    }

    public boolean setMatchLive(String matchId, int scoreA, int scoreB, String actor) {
        return updateLiveMatch(matchId, scoreA, scoreB, scoreA * 4 + 2, scoreB * 4 + 1, "LIVE", actor);
    }

    public boolean updateLiveMatch(String matchId, int scoreA, int scoreB, int killsA, int killsB, String status, String actor) {
        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("scoreA", scoreA);
            updates.put("scoreB", scoreB);
            updates.put("killsA", killsA);
            updates.put("killsB", killsB);
            updates.put("status", status != null ? status : "LIVE");
            updates.put("updatedAt", Instant.now().toString());

            db.collection("matches").document(matchId).update(updates).get();
            addAuditLog(actor, "LIVE_SCORE_UPDATE", "match", matchId,
                    "Live Score: " + scoreA + "-" + scoreB + " | Kills: " + killsA + "-" + killsB + " (" + status + ")");
            return true;
        } catch (Exception e) {
            System.err.println("Firestore updateLiveMatch error: " + e.getMessage());
            return false;
        }
    }

    public boolean recordMatchScore(String matchId, int scoreA, int scoreB, String reason, String actor) {
        return recordMatchScore(matchId, scoreA, scoreB, scoreA * 4 + 5, scoreB * 4 + 3, reason, actor);
    }

    public boolean recordMatchScore(String matchId, int scoreA, int scoreB, int killsA, int killsB, String reason, String actor) {
        try {
            DocumentSnapshot doc = db.collection("matches").document(matchId).get().get();
            if (!doc.exists()) {
                System.err.println("Match not found: " + matchId);
                return false;
            }

            String teamAId = str(doc, "teamAId");
            String teamBId = str(doc, "teamBId");
            String teamAName = str(doc, "teamAName");
            String teamBName = str(doc, "teamBName");
            String tournamentId = str(doc, "tournamentId");
            Long roundLong = doc.getLong("round");
            Long matchNumLong = doc.getLong("matchNumber");
            int round = roundLong != null ? roundLong.intValue() : 1;
            int matchNum = matchNumLong != null ? matchNumLong.intValue() : 1;

            if (scoreA == scoreB) {
                System.err.println("Match cannot end in a draw in single-elimination tournament.");
                return false;
            }

            String winnerId = scoreA > scoreB ? teamAId : teamBId;
            String winnerName = scoreA > scoreB ? teamAName : teamBName;

            Map<String, Object> updates = new HashMap<>();
            updates.put("scoreA", scoreA);
            updates.put("scoreB", scoreB);
            updates.put("killsA", killsA);
            updates.put("killsB", killsB);
            updates.put("status", "COMPLETE");
            updates.put("winnerId", winnerId);
            updates.put("winnerName", winnerName);
            updates.put("completedAt", Instant.now().toString());
            updates.put("reason", reason != null ? reason : "Match completed");
            updates.put("updatedAt", Instant.now().toString());

            db.collection("matches").document(matchId).update(updates).get();
            addAuditLog(actor, "RECORD_SCORE", "match", matchId,
                    "Result: " + teamAName + " (" + scoreA + ") vs (" + scoreB + ") " + teamBName +
                    " [Kills: " + killsA + "-" + killsB + "] → Winner: " + winnerName);

            // ==================== ADVANCE WINNER TO NEXT ROUND ====================
            int nextRound = round + 1;
            int nextMatchNum = (matchNum + 1) / 2;
            boolean isTeamAInNext = (matchNum % 2 != 0);

            Query nextMatchQuery = db.collection("matches")
                    .whereEqualTo("tournamentId", tournamentId)
                    .whereEqualTo("round", nextRound)
                    .whereEqualTo("matchNumber", nextMatchNum);

            List<QueryDocumentSnapshot> nextDocs = nextMatchQuery.get().get().getDocuments();
            if (!nextDocs.isEmpty()) {
                DocumentReference nextRef = nextDocs.get(0).getReference();
                Map<String, Object> nextUpdates = new HashMap<>();
                if (isTeamAInNext) {
                    nextUpdates.put("teamAId", winnerId);
                    nextUpdates.put("teamAName", winnerName);
                } else {
                    nextUpdates.put("teamBId", winnerId);
                    nextUpdates.put("teamBName", winnerName);
                }
                nextUpdates.put("updatedAt", Instant.now().toString());
                nextRef.update(nextUpdates).get();
                addAuditLog(actor, "BRACKET_ADVANCEMENT", "bracket", nextDocs.get(0).getId(),
                        "Advanced " + winnerName + " into Round " + nextRound + " Match #" + nextMatchNum);
            }

            return true;
        } catch (Exception e) {
            System.err.println("Firestore recordMatchScore error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateMatchDetails(String matchId, Map<String, Object> updates, String actor) {
        try {
            updates.put("updatedAt", Instant.now().toString());
            db.collection("matches").document(matchId).update(updates).get();
            addAuditLog(actor, "EDIT_MATCH", "match", matchId, "Modified match parameters: " + updates.keySet());
            return true;
        } catch (Exception e) {
            System.err.println("Firestore updateMatchDetails error: " + e.getMessage());
            return false;
        }
    }

    // ==================== DISPUTES ====================

    public List<DisputeDto> getDisputes(String tournamentId) {
        try {
            Query q = db.collection("disputes").whereEqualTo("tournamentId", tournamentId);
            List<QueryDocumentSnapshot> docs = q.get().get().getDocuments();
            return docs.stream().map(this::docToDispute).collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Firestore getDisputes error: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public boolean createDispute(DisputeDto d, String actor) {
        try {
            String id = d.getId() != null && !d.getId().isBlank() ? d.getId() : "disp-" + UUID.randomUUID().toString().substring(0, 8);
            Map<String, Object> data = new HashMap<>();
            data.put("id", id);
            data.put("tournamentId", d.getTournamentId());
            data.put("matchId", d.getMatchId());
            data.put("submittedByTeamName", d.getSubmittedByTeamName());
            data.put("reason", d.getReason());
            data.put("evidenceUrl", d.getEvidenceUrl() != null ? d.getEvidenceUrl() : "");
            data.put("status", "SUBMITTED");
            data.put("createdAt", Instant.now().toString());

            db.collection("disputes").document(id).set(data).get();
            addAuditLog(actor, "FILE_DISPUTE", "dispute", id, "Dispute filed for match " + d.getMatchId());
            return true;
        } catch (Exception e) {
            System.err.println("Firestore createDispute error: " + e.getMessage());
            return false;
        }
    }

    public boolean resolveDispute(String disputeId, String decision, String notes, String actor) {
        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", "RESOLVED");
            updates.put("decision", decision);
            updates.put("reviewerNotes", notes != null ? notes : "");
            updates.put("reviewedBy", actor);
            updates.put("resolvedAt", Instant.now().toString());

            db.collection("disputes").document(disputeId).update(updates).get();
            addAuditLog(actor, "RESOLVE_DISPUTE", "dispute", disputeId, "Decision: " + decision + " | Notes: " + notes);
            return true;
        } catch (Exception e) {
            System.err.println("Firestore resolveDispute error: " + e.getMessage());
            return false;
        }
    }

    // ==================== CONDUCT REPORTS ====================

    public List<ConductReportDto> getConductReports(String tournamentId) {
        try {
            Query q = db.collection("conduct_reports").whereEqualTo("tournamentId", tournamentId);
            List<QueryDocumentSnapshot> docs = q.get().get().getDocuments();
            return docs.stream().map(this::docToConduct).collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Firestore getConductReports error: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public boolean resolveConductReport(String reportId, String action, String reason, String actor) {
        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("status", "RESOLVED");
            updates.put("moderationAction", action);
            updates.put("moderationReason", reason != null ? reason : "");
            updates.put("reviewedBy", actor);
            updates.put("resolvedAt", Instant.now().toString());

            db.collection("conduct_reports").document(reportId).update(updates).get();
            addAuditLog(actor, "RESOLVE_CONDUCT_REPORT", "conduct_report", reportId, "Action: " + action + " | " + reason);
            return true;
        } catch (Exception e) {
            System.err.println("Firestore resolveConductReport error: " + e.getMessage());
            return false;
        }
    }

    // ==================== AUDIT LOGS ====================

    public List<AuditLogDto> getAuditLogs() {
        try {
            List<QueryDocumentSnapshot> docs = db.collection("audit_logs")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(100).get().get().getDocuments();
            return docs.stream().map(this::docToAudit).collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Firestore getAuditLogs error: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public void addAuditLog(String actor, String action, String entityType, String entityId, String reason) {
        try {
            String id = "log-" + UUID.randomUUID().toString().substring(0, 8);
            Map<String, Object> data = new HashMap<>();
            data.put("id", id);
            data.put("actor", actor != null ? actor : "system");
            data.put("action", action);
            data.put("entityType", entityType);
            data.put("entityId", entityId);
            data.put("reason", reason);
            data.put("timestamp", Instant.now().toString());

            db.collection("audit_logs").document(id).set(data);
        } catch (Exception e) {
            System.err.println("Firestore addAuditLog error: " + e.getMessage());
        }
    }

    // ==================== AUTH ====================

    public UserDto login(String username, String password) {
        try {
            Query q = db.collection("users").whereEqualTo("username", username);
            List<QueryDocumentSnapshot> docs = q.get().get().getDocuments();
            if (docs.isEmpty()) return null;
            Map<String, Object> d = docs.get(0).getData();
            String storedPw = str(d, "password");
            if (!password.equals(storedPw)) return null;

            UserDto user = new UserDto();
            user.setId(str(d, "id"));
            user.setUsername(str(d, "username"));
            user.setRole(str(d, "role"));
            user.setGamerTag(str(d, "gamerTag"));
            return user;
        } catch (Exception e) {
            System.err.println("Firestore login error: " + e.getMessage());
            return null;
        }
    }

    // ==================== PROTOTYPE DATA SEEDER ====================

    public boolean seedPrototypeData(String actor) {
        try {
            System.out.println("⚡ Seeding multi-game esports championship prototype data into Cloud Firestore...");

            // 1. TOURNAMENT 1: VALORANT MASTERS (IN_PROGRESS)
            Map<String, Object> t1 = new HashMap<>();
            t1.put("id", "trn-val-masters");
            t1.put("name", "VALORANT CHAMPIONS TOUR: MASTERS TOKYO");
            t1.put("description", "Tier-1 International VCT Masters Tournament. Single-elimination championship bracket.");
            t1.put("game", "VALORANT");
            t1.put("mode", "SINGLE_ELIMINATION_TDM");
            t1.put("status", "IN_PROGRESS");
            t1.put("teamSize", 5);
            t1.put("maxParticipants", 8);
            t1.put("published", true);
            t1.put("rules", "Standard VCT competitive ruleset. Overtime win by 2.");
            t1.put("tournamentStartDate", "2026-11-14T12:00:00Z");
            t1.put("createdAt", Instant.now().toString());
            t1.put("createdBy", actor);
            db.collection("tournaments").document("trn-val-masters").set(t1).get();

            // 2. TOURNAMENT 2: COUNTER-STRIKE 2 (CS2) IEM COLOGNE (IN_PROGRESS)
            Map<String, Object> t2 = new HashMap<>();
            t2.put("id", "trn-cs2-cologne");
            t2.put("name", "CS2 IEM COLOGNE: CATHEDRAL OF COUNTER-STRIKE");
            t2.put("description", "Premier Counter-Strike 2 Major Championship. MR12 Competitive Ruleset.");
            t2.put("game", "COUNTER-STRIKE 2");
            t2.put("mode", "SINGLE_ELIMINATION_TDM");
            t2.put("status", "IN_PROGRESS");
            t2.put("teamSize", 5);
            t2.put("maxParticipants", 8);
            t2.put("published", true);
            t2.put("rules", "Valve competitive settings. 128-tick equivalent subtick servers. Overtime MR3.");
            t2.put("tournamentStartDate", "2026-11-14T15:00:00Z");
            t2.put("createdAt", Instant.now().toString());
            t2.put("createdBy", actor);
            db.collection("tournaments").document("trn-cs2-cologne").set(t2).get();

            // 3. TOURNAMENT 3: DOTA 2 THE INTERNATIONAL (REGISTRATION_OPEN)
            Map<String, Object> t3 = new HashMap<>();
            t3.put("id", "trn-dota2-ti");
            t3.put("name", "THE INTERNATIONAL: CHALICE OF AEGIS");
            t3.put("description", "Annual world championship tournament. Captain's Mode 5v5.");
            t3.put("game", "DOTA 2");
            t3.put("mode", "SINGLE_ELIMINATION_TDM");
            t3.put("status", "REGISTRATION_OPEN");
            t3.put("teamSize", 5);
            t3.put("maxParticipants", 8);
            t3.put("published", true);
            t3.put("rules", "Captain's Mode. Standard pick/ban phase.");
            t3.put("tournamentStartDate", "2026-11-16T18:00:00Z");
            t3.put("createdAt", Instant.now().toString());
            t3.put("createdBy", actor);
            db.collection("tournaments").document("trn-dota2-ti").set(t3).get();

            // 4. TOURNAMENT 4: APEX LEGENDS BATTLE ROYALE (IN_PROGRESS)
            Map<String, Object> t4 = new HashMap<>();
            t4.put("id", "trn-br-pro");
            t4.put("name", "APEX PREDATORS: PRO SERIES SHOWDOWN");
            t4.put("description", "ALGS Pro Series Battle Royale. Point-based placement + kill multipliers.");
            t4.put("game", "APEX LEGENDS");
            t4.put("mode", "BATTLE_ROYALE");
            t4.put("status", "IN_PROGRESS");
            t4.put("teamSize", 3);
            t4.put("maxParticipants", 8);
            t4.put("published", true);
            t4.put("rules", "Placement points (1st=12, 2nd=9, 3rd=7...) + 1 point per kill.");
            t4.put("tournamentStartDate", "2026-11-14T14:00:00Z");
            t4.put("createdAt", Instant.now().toString());
            t4.put("createdBy", actor);
            db.collection("tournaments").document("trn-br-pro").set(t4).get();

            // 5. TEAMS FOR VALORANT MASTERS (With Passwords!)
            String[][] teamData = {
                    {"tm-1", "Sentinels", "SEN", "tenz@sentinels.gg", "TenZ, zekken, Sacy, johnqt, Zellsis", "1", "Sentinels@2026"},
                    {"tm-2", "Team Liquid", "TL", "jamppi@liquid.com", "Jamppi, nAts, keiko, Enzo, Mistic", "8", "Liquid@2026"},
                    {"tm-3", "Fnatic", "FNC", "boaster@fnatic.com", "Boaster, Derke, Alfajer, Chronicle, hiro", "4", "Fnatic@2026"},
                    {"tm-4", "DRX Esports", "DRX", "stax@drx.gg", "stax, BuZz, MaKo, Foxy9, Flashback", "5", "DRXEsports@2026"},
                    {"tm-5", "Paper Rex", "PRX", "f0rsaken@pprx.team", "f0rsakeN, Jinggg, d4v41, mindfreak, something", "2", "PaperRex@2026"},
                    {"tm-6", "LOUD Esports", "LOUD", "saadhak@loud.gg", "Saadhak, Less, tuyz, cauanzin, qck", "7", "LOUDEsports@2026"},
                    {"tm-7", "Evil Geniuses", "EG", "jawgemo@eg.gg", "Jawgemo, Potter, Derrek, NaturE, supamen", "3", "EvilGeniuses@2026"},
                    {"tm-8", "EDward Gaming", "EDG", "zmjjkk@edg.cn", "ZmjjKK, nobody, CHICHOO, Smoggy, Haodong", "6", "EDwardGaming@2026"}
            };

            for (String[] t : teamData) {
                Map<String, Object> tm = new HashMap<>();
                tm.put("id", t[0]);
                tm.put("tournamentId", "trn-val-masters");
                tm.put("name", t[1]);
                tm.put("tag", t[2]);
                tm.put("captainId", t[1].toLowerCase().replace(" ", ""));
                tm.put("captainEmail", t[3]);
                tm.put("playerNames", Arrays.asList(t[4].split(", ")));
                tm.put("checkedIn", true);
                tm.put("seed", Integer.parseInt(t[5]));
                tm.put("password", t[6]);
                tm.put("registrationStatus", "APPROVED");
                tm.put("registeredAt", Instant.now().toString());
                db.collection("teams").document(t[0]).set(tm).get();
            }

            // 6. TEAMS FOR CS2 IEM COLOGNE
            String[][] cs2Teams = {
                    {"tm-cs1", "Natus Vincere", "NAVI", "aleksib@navi.gg", "s1mple, b1t, jL, iM, Aleksib", "1", "NaviPass@2026"},
                    {"tm-cs2", "FaZe Clan", "FAZE", "karrigan@faze.com", "karrigan, rain, broky, frozen, ropz", "2", "FaZePass@2026"},
                    {"tm-cs3", "Team Vitality", "VIT", "apex@vitality.gg", "ZywOo, apEX, Spinx, flameZ, mezii", "3", "VitalityPass@2026"},
                    {"tm-cs4", "G2 Esports", "G2", "snax@g2.gg", "NiKo, m0NESY, huNter-, malbsMd, Snax", "4", "G2Esports@2026"}
            };
            for (String[] t : cs2Teams) {
                Map<String, Object> tm = new HashMap<>();
                tm.put("id", t[0]);
                tm.put("tournamentId", "trn-cs2-cologne");
                tm.put("name", t[1]);
                tm.put("tag", t[2]);
                tm.put("captainId", t[1].toLowerCase().replace(" ", ""));
                tm.put("captainEmail", t[3]);
                tm.put("playerNames", Arrays.asList(t[4].split(", ")));
                tm.put("checkedIn", true);
                tm.put("seed", Integer.parseInt(t[5]));
                tm.put("password", t[6]);
                tm.put("registrationStatus", "APPROVED");
                tm.put("registeredAt", Instant.now().toString());
                db.collection("teams").document(t[0]).set(tm).get();
            }

            // 7. MATCHES FOR VALORANT MASTERS (With Authentic Kills & Scores)
            // Quarter-Finals (ALL COMPLETE)
            seedMatch("match-val-qf1", "trn-val-masters", 1, 1, "Quarter-Finals",
                    "tm-1", "Sentinels", 13, 56, "tm-2", "Team Liquid", 9, 41,
                    "COMPLETE", "tm-1", "Sentinels", "Sunset", "VCT-QF-101", "vct2026", "2026-11-14T10:00:00Z");

            seedMatch("match-val-qf2", "trn-val-masters", 1, 2, "Quarter-Finals",
                    "tm-3", "Fnatic", 13, 58, "tm-4", "DRX Esports", 7, 34,
                    "COMPLETE", "tm-3", "Fnatic", "Lotus", "VCT-QF-102", "vct2026", "2026-11-14T11:00:00Z");

            seedMatch("match-val-qf3", "trn-val-masters", 1, 3, "Quarter-Finals",
                    "tm-5", "Paper Rex", 13, 62, "tm-6", "LOUD Esports", 11, 54,
                    "COMPLETE", "tm-5", "Paper Rex", "Bind", "VCT-QF-103", "vct2026", "2026-11-14T12:00:00Z");

            seedMatch("match-val-qf4", "trn-val-masters", 1, 4, "Quarter-Finals",
                    "tm-7", "Evil Geniuses", 13, 57, "tm-8", "EDward Gaming", 10, 46,
                    "COMPLETE", "tm-7", "Evil Geniuses", "Haven", "VCT-QF-104", "vct2026", "2026-11-14T13:00:00Z");

            // Semi-Final 1 (COMPLETE)
            seedMatch("match-val-sf1", "trn-val-masters", 2, 1, "Semi-Finals",
                    "tm-1", "Sentinels", 13, 64, "tm-3", "Fnatic", 11, 59,
                    "COMPLETE", "tm-1", "Sentinels", "Ascent", "VCT-SF-201", "vct2026", "2026-11-14T15:00:00Z");

            // Semi-Final 2 (LIVE RIGHT NOW! High tension 11-12 score with 48 vs 51 kills!)
            seedMatch("match-val-sf2", "trn-val-masters", 2, 2, "Semi-Finals",
                    "tm-5", "Paper Rex", 11, 48, "tm-7", "Evil Geniuses", 12, 51,
                    "LIVE", null, null, "Lotus", "VCT-LIVE-202", "livepass99", "2026-11-14T17:00:00Z");

            // Grand Final (UPCOMING)
            seedMatch("match-val-final", "trn-val-masters", 3, 1, "Grand Final",
                    "tm-1", "Sentinels", 0, 0, "tbd", "TBD (Winner SF2)", 0, 0,
                    "UPCOMING", null, null, "Sunset", "VCT-FINALS-301", "apex2026", "Tonight 21:00 IST");

            // 8. MATCHES FOR CS2 (NaVi vs FaZe LIVE!)
            seedMatch("match-cs2-sf1", "trn-cs2-cologne", 1, 1, "Semi-Finals",
                    "tm-cs1", "Natus Vincere", 11, 44, "tm-cs2", "FaZe Clan", 10, 42,
                    "LIVE", null, null, "Mirage", "CS2-IEM-01", "cs2major", "2026-11-14T18:30:00Z");

            seedMatch("match-cs2-sf2", "trn-cs2-cologne", 1, 2, "Semi-Finals",
                    "tm-cs3", "Team Vitality", 13, 52, "tm-cs4", "G2 Esports", 9, 39,
                    "COMPLETE", "tm-cs3", "Team Vitality", "Inferno", "CS2-IEM-02", "cs2major", "2026-11-14T16:00:00Z");

            seedMatch("match-cs2-final", "trn-cs2-cologne", 2, 1, "Grand Final",
                    "tbd", "TBD (Winner SF1)", 0, 0, "tm-cs3", "Team Vitality", 0, 0,
                    "UPCOMING", null, null, "Nuke", "CS2-IEM-FINAL", "cs2major", "Tomorrow 20:00 IST");

            // 9. DISPUTES
            Map<String, Object> disp1 = new HashMap<>();
            disp1.put("id", "disp-001");
            disp1.put("tournamentId", "trn-val-masters");
            disp1.put("matchId", "match-val-qf1");
            disp1.put("submittedByTeamName", "Team Liquid");
            disp1.put("reason", "Disputed ping spike on round 19 server host.");
            disp1.put("evidenceUrl", "https://clips.twitch.tv/example-qf1-spike");
            disp1.put("status", "RESOLVED");
            disp1.put("decision", "CONFIRMED_ORIGINAL_RESULT");
            disp1.put("reviewerNotes", "Riot official spectator log showed round tickrate was steady at 128Hz.");
            disp1.put("reviewedBy", "admin");
            disp1.put("resolvedAt", Instant.now().toString());
            disp1.put("createdAt", Instant.now().toString());
            db.collection("disputes").document("disp-001").set(disp1).get();

            Map<String, Object> disp2 = new HashMap<>();
            disp2.put("id", "disp-002");
            disp2.put("tournamentId", "trn-val-masters");
            disp2.put("matchId", "match-val-sf2");
            disp2.put("submittedByTeamName", "Paper Rex");
            disp2.put("reason", "Requested review of tech pause timer on round 16.");
            disp2.put("evidenceUrl", "https://nexusarena.gg/clips/sf2-tech-pause");
            disp2.put("status", "UNDER_REVIEW");
            disp2.put("decision", "-");
            disp2.put("reviewerNotes", "Reviewing official referee headset transcript.");
            disp2.put("createdAt", Instant.now().toString());
            db.collection("disputes").document("disp-002").set(disp2).get();

            // 10. CONDUCT REPORTS
            Map<String, Object> cr1 = new HashMap<>();
            cr1.put("id", "rpt-001");
            cr1.put("tournamentId", "trn-val-masters");
            cr1.put("matchId", "match-val-qf2");
            cr1.put("reportedPlayerName", "Demon1");
            cr1.put("reportedTeamName", "Evil Geniuses");
            cr1.put("submittedByName", "Jamppi (TL)");
            cr1.put("category", "ALL_CHAT_BM");
            cr1.put("description", "Excessive taunting in all-chat post defuse.");
            cr1.put("evidenceUrl", "https://nexusarena.gg/evidence/bm-screenshot.png");
            cr1.put("status", "RESOLVED");
            cr1.put("moderationAction", "WARNED");
            cr1.put("moderationReason", "Official fair-play verbal warning issued.");
            cr1.put("reviewedBy", "admin");
            cr1.put("createdAt", Instant.now().toString());
            db.collection("conduct_reports").document("rpt-001").set(cr1).get();

            // 11. DEMO USERS
            seedUser("usr-admin", "admin", "admin123", "ORGANIZER", "ApexAdmin#0001");
            seedUser("usr-tenz", "tenz", "tenz123", "PARTICIPANT", "SEN_TenZ#1337");
            seedUser("usr-boaster", "boaster", "boaster123", "PARTICIPANT", "FNC_Boaster#0001");

            // 12. AUDIT LOGS
            addAuditLog(actor, "SEED_PROTOTYPE_DATA", "system", "all",
                    "Populated Cloud Firestore with complete VCT Masters and CS2 Cologne Major championships, live kills, and ongoing matches.");

            System.out.println("✓ Complete multi-game esports championship data populated into Cloud Firestore!");
            return true;
        } catch (Exception e) {
            System.err.println("Firestore seedPrototypeData error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void seedMatch(String id, String tournamentId, int round, int matchNum, String roundName,
                           String teamAId, String teamAName, int scoreA, int killsA,
                           String teamBId, String teamBName, int scoreB, int killsB,
                           String status, String winnerId, String winnerName,
                           String map, String roomId, String password, String time) throws Exception {
        Map<String, Object> m = new HashMap<>();
        m.put("id", id);
        m.put("tournamentId", tournamentId);
        m.put("round", round);
        m.put("matchNumber", matchNum);
        m.put("roundName", roundName);
        m.put("mode", "5V5");
        m.put("teamAId", teamAId);
        m.put("teamBId", teamBId);
        m.put("teamAName", teamAName);
        m.put("teamBName", teamBName);
        m.put("scoreA", scoreA);
        m.put("scoreB", scoreB);
        m.put("killsA", killsA);
        m.put("killsB", killsB);
        m.put("status", status);
        m.put("winnerId", winnerId);
        m.put("winnerName", winnerName);
        m.put("mapName", map);
        m.put("roomId", roomId);
        m.put("roomPassword", password);
        m.put("scheduledTime", time);
        m.put("createdAt", Instant.now().toString());
        m.put("updatedAt", Instant.now().toString());
        db.collection("matches").document(id).set(m).get();
    }

    private void seedUser(String id, String username, String password, String role, String gamerTag) throws Exception {
        Map<String, Object> u = new HashMap<>();
        u.put("id", id);
        u.put("username", username);
        u.put("password", password);
        u.put("role", role);
        u.put("gamerTag", gamerTag);
        db.collection("users").document(id).set(u).get();
    }

    // ==================== CONVERTERS ====================

    private TournamentDto docToTournament(DocumentSnapshot d) {
        TournamentDto t = new TournamentDto();
        t.setId(d.getId());
        t.setName(str(d, "name"));
        t.setDescription(str(d, "description"));
        t.setGame(str(d, "game"));
        t.setMode(str(d, "mode"));
        t.setStatus(str(d, "status"));
        t.setRules(str(d, "rules"));
        t.setTournamentStartDate(str(d, "tournamentStartDate"));
        Long ts = d.getLong("teamSize");
        t.setTeamSize(ts != null ? ts.intValue() : 5);
        Long mp = d.getLong("maxParticipants");
        t.setMaxParticipants(mp != null ? mp.intValue() : 8);
        Boolean pub = d.getBoolean("published");
        t.setPublished(pub != null && pub);
        return t;
    }

    private TeamDto docToTeam(DocumentSnapshot d) {
        TeamDto t = new TeamDto();
        t.setId(d.getId());
        t.setTournamentId(str(d, "tournamentId"));
        t.setName(str(d, "name"));
        t.setTag(str(d, "tag"));
        t.setPassword(str(d, "password"));
        t.setCaptainId(str(d, "captainId"));
        t.setCaptainEmail(str(d, "captainEmail"));
        t.setRegistrationStatus(str(d, "registrationStatus"));
        Boolean ci = d.getBoolean("checkedIn");
        t.setCheckedIn(ci != null && ci);
        Long seed = d.getLong("seed");
        t.setSeed(seed != null ? seed.intValue() : null);
        Object players = d.get("playerNames");
        if (players instanceof List) {
            List<String> pNames = new ArrayList<>();
            for (Object o : (List<?>) players) pNames.add(String.valueOf(o));
            t.setPlayerNames(pNames);
        }
        return t;
    }

    private MatchDto docToMatch(DocumentSnapshot d) {
        MatchDto m = new MatchDto();
        m.setId(d.getId());
        m.setTournamentId(str(d, "tournamentId"));
        Long r = d.getLong("round");
        m.setRound(r != null ? r.intValue() : 1);
        Long mn = d.getLong("matchNumber");
        m.setMatchNumber(mn != null ? mn.intValue() : 1);
        m.setRoundName(str(d, "roundName"));
        m.setMode(str(d, "mode"));
        m.setTeamAId(str(d, "teamAId"));
        m.setTeamBId(str(d, "teamBId"));
        m.setTeamAName(str(d, "teamAName"));
        m.setTeamBName(str(d, "teamBName"));
        Long sa = d.getLong("scoreA");
        m.setScoreA(sa != null ? sa.intValue() : 0);
        Long sb = d.getLong("scoreB");
        m.setScoreB(sb != null ? sb.intValue() : 0);
        Long ka = d.getLong("killsA");
        m.setKillsA(ka != null ? ka.intValue() : 0);
        Long kb = d.getLong("killsB");
        m.setKillsB(kb != null ? kb.intValue() : 0);
        m.setWinnerId(str(d, "winnerId"));
        m.setWinnerName(str(d, "winnerName"));
        m.setStatus(str(d, "status"));
        m.setScheduledTime(str(d, "scheduledTime"));
        m.setRoomId(str(d, "roomId"));
        m.setRoomPassword(str(d, "roomPassword"));
        m.setMapName(str(d, "mapName"));
        Boolean bye = d.getBoolean("bye");
        m.setBye(bye != null && bye);
        return m;
    }

    private MatchDto docToMatch(Map<String, Object> d) {
        MatchDto m = new MatchDto();
        m.setId((String) d.get("id"));
        m.setTournamentId((String) d.get("tournamentId"));
        Object r = d.get("round");
        m.setRound(r instanceof Number ? ((Number) r).intValue() : 1);
        Object mn = d.get("matchNumber");
        m.setMatchNumber(mn instanceof Number ? ((Number) mn).intValue() : 1);
        m.setRoundName((String) d.get("roundName"));
        m.setMode((String) d.get("mode"));
        m.setTeamAId((String) d.get("teamAId"));
        m.setTeamBId((String) d.get("teamBId"));
        m.setTeamAName((String) d.get("teamAName"));
        m.setTeamBName((String) d.get("teamBName"));
        Object sa = d.get("scoreA");
        m.setScoreA(sa instanceof Number ? ((Number) sa).intValue() : 0);
        Object sb = d.get("scoreB");
        m.setScoreB(sb instanceof Number ? ((Number) sb).intValue() : 0);
        Object ka = d.get("killsA");
        m.setKillsA(ka instanceof Number ? ((Number) ka).intValue() : 0);
        Object kb = d.get("killsB");
        m.setKillsB(kb instanceof Number ? ((Number) kb).intValue() : 0);
        m.setWinnerId((String) d.get("winnerId"));
        m.setWinnerName((String) d.get("winnerName"));
        m.setStatus((String) d.get("status"));
        m.setScheduledTime((String) d.get("scheduledTime"));
        m.setRoomId((String) d.get("roomId"));
        m.setRoomPassword((String) d.get("roomPassword"));
        m.setMapName((String) d.get("mapName"));
        return m;
    }

    private DisputeDto docToDispute(DocumentSnapshot d) {
        DisputeDto dp = new DisputeDto();
        dp.setId(d.getId());
        dp.setTournamentId(str(d, "tournamentId"));
        dp.setMatchId(str(d, "matchId"));
        dp.setSubmittedByTeamName(str(d, "submittedByTeamName"));
        dp.setReason(str(d, "reason"));
        dp.setEvidenceUrl(str(d, "evidenceUrl"));
        dp.setStatus(str(d, "status"));
        dp.setDecision(str(d, "decision"));
        dp.setReviewerNotes(str(d, "reviewerNotes"));
        dp.setReviewedBy(str(d, "reviewedBy"));
        dp.setResolvedAt(str(d, "resolvedAt"));
        dp.setCreatedAt(str(d, "createdAt"));
        return dp;
    }

    private ConductReportDto docToConduct(DocumentSnapshot d) {
        ConductReportDto c = new ConductReportDto();
        c.setId(d.getId());
        c.setTournamentId(str(d, "tournamentId"));
        c.setMatchId(str(d, "matchId"));
        c.setReportedPlayerName(str(d, "reportedPlayerName"));
        c.setReportedTeamName(str(d, "reportedTeamName"));
        c.setSubmittedByName(str(d, "submittedByName"));
        c.setCategory(str(d, "category"));
        c.setDescription(str(d, "description"));
        c.setEvidenceUrl(str(d, "evidenceUrl"));
        c.setStatus(str(d, "status"));
        c.setModerationAction(str(d, "moderationAction"));
        c.setModerationReason(str(d, "moderationReason"));
        c.setReviewedBy(str(d, "reviewedBy"));
        c.setCreatedAt(str(d, "createdAt"));
        return c;
    }

    private AuditLogDto docToAudit(DocumentSnapshot d) {
        AuditLogDto a = new AuditLogDto();
        a.setId(d.getId());
        a.setActor(str(d, "actor"));
        a.setAction(str(d, "action"));
        a.setEntityType(str(d, "entityType"));
        a.setEntityId(str(d, "entityId"));
        a.setReason(str(d, "reason"));
        a.setTimestamp(str(d, "timestamp"));
        return a;
    }

    private String str(DocumentSnapshot d, String key) {
        Object v = d.get(key);
        return v != null ? v.toString() : "";
    }

    private String str(Map<String, Object> d, String key) {
        Object v = d.get(key);
        return v != null ? v.toString() : "";
    }
}
