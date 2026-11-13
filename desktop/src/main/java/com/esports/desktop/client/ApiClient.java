package com.esports.desktop.client;

import com.esports.desktop.model.*;
import javafx.application.Platform;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * High-Performance Asynchronous Facade over FirebaseClient.
 * Features in-memory caching for 0ms instantaneous UI responses and
 * non-blocking background synchronization with Cloud Firestore.
 */
public class ApiClient {

    public static final String CURRENT_VERSION = "2.4.0";
    public static final String UPDATE_CHECK_URL = "https://nexusarena-esports.web.app/version.json";

    private FirebaseClient firebase;
    private UserDto currentUser;
    private String connectionError;

    // Fast In-Memory Caches
    private List<TournamentDto> cachedTournaments = new CopyOnWriteArrayList<>();
    private final Map<String, List<TeamDto>> cachedTeams = new ConcurrentHashMap<>();
    private final Map<String, List<MatchDto>> cachedMatches = new ConcurrentHashMap<>();
    private final Map<String, List<DisputeDto>> cachedDisputes = new ConcurrentHashMap<>();
    private final Map<String, List<ConductReportDto>> cachedReports = new ConcurrentHashMap<>();
    private List<AuditLogDto> cachedAudit = new CopyOnWriteArrayList<>();

    private final Set<String> sessionUnlockedTeams = ConcurrentHashMap.newKeySet();
    private final ExecutorService backgroundExecutor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "nexus-cloud-worker");
        t.setDaemon(true);
        return t;
    });

    public ApiClient() {
        this.currentUser = new UserDto();
        this.currentUser.setId("usr-admin");
        this.currentUser.setUsername("admin");
        this.currentUser.setRole("ORGANIZER");
        this.currentUser.setGamerTag("ApexAdmin#0001");

        try {
            this.firebase = FirebaseClient.getInstance();
            // Perform initial background warm-up
            refreshAllAsync(null);
        } catch (Exception e) {
            this.connectionError = e.getMessage();
            System.err.println("Firebase init failed: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return firebase != null;
    }

    public String getConnectionError() {
        return connectionError;
    }

    public boolean isBackendAlive() {
        return firebase != null;
    }

    public UserDto getCurrentUser() {
        return currentUser;
    }

    public boolean login(String username, String password) {
        if (firebase == null) return false;
        UserDto user = firebase.login(username, password);
        if (user != null) {
            this.currentUser = user;
            return true;
        }
        return false;
    }

    // ==================== ASYNC CACHE REFRESH ====================

    public void refreshAllAsync(Runnable onComplete) {
        if (firebase == null) {
            if (onComplete != null) Platform.runLater(onComplete);
            return;
        }

        backgroundExecutor.submit(() -> {
            try {
                List<TournamentDto> trns = firebase.getTournaments();
                if (trns != null && !trns.isEmpty()) {
                    cachedTournaments = new CopyOnWriteArrayList<>(trns);
                    for (TournamentDto t : trns) {
                        List<TeamDto> tms = firebase.getTeams(t.getId());
                        if (tms != null) cachedTeams.put(t.getId(), new CopyOnWriteArrayList<>(tms));

                        List<MatchDto> mts = firebase.getMatches(t.getId());
                        if (mts != null) cachedMatches.put(t.getId(), new CopyOnWriteArrayList<>(mts));

                        List<DisputeDto> dsp = firebase.getDisputes(t.getId());
                        if (dsp != null) cachedDisputes.put(t.getId(), new CopyOnWriteArrayList<>(dsp));

                        List<ConductReportDto> crs = firebase.getConductReports(t.getId());
                        if (crs != null) cachedReports.put(t.getId(), new CopyOnWriteArrayList<>(crs));
                    }
                }
                List<AuditLogDto> logs = firebase.getAuditLogs();
                if (logs != null) cachedAudit = new CopyOnWriteArrayList<>(logs);
            } catch (Exception ex) {
                System.err.println("Background sync warning: " + ex.getMessage());
            } finally {
                if (onComplete != null) {
                    Platform.runLater(onComplete);
                }
            }
        });
    }

    // ==================== TOURNAMENTS ====================

    public List<TournamentDto> getTournaments() {
        if (!cachedTournaments.isEmpty()) {
            return new ArrayList<>(cachedTournaments);
        }
        if (firebase == null) return Collections.emptyList();
        try {
            List<TournamentDto> trns = firebase.getTournaments();
            if (trns != null && !trns.isEmpty()) {
                cachedTournaments = new CopyOnWriteArrayList<>(trns);
                return new ArrayList<>(cachedTournaments);
            }
        } catch (Exception e) {
            System.err.println("getTournaments error: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    public TournamentDto createTournament(TournamentDto tournament) {
        if (firebase == null) return null;
        TournamentDto created = firebase.createTournament(tournament, currentUser.getUsername());
        if (created != null) {
            cachedTournaments.add(0, created);
        }
        return created;
    }

    public boolean updateTournamentStatus(String tournamentId, String status) {
        if (firebase == null) return false;
        boolean ok = firebase.updateTournamentStatus(tournamentId, status, currentUser.getUsername());
        if (ok) {
            for (TournamentDto t : cachedTournaments) {
                if (t.getId().equals(tournamentId)) {
                    t.setStatus(status);
                    break;
                }
            }
        }
        return ok;
    }

    public boolean deleteTournament(String tournamentId) {
        if (firebase == null) return false;
        boolean ok = firebase.deleteTournament(tournamentId, currentUser.getUsername());
        if (ok) {
            cachedTournaments.removeIf(t -> t.getId().equals(tournamentId));
            cachedTeams.remove(tournamentId);
            cachedMatches.remove(tournamentId);
        }
        return ok;
    }

    // ==================== TEAMS & AUTH ====================

    public List<TeamDto> getTeams(String tournamentId) {
        if (tournamentId == null) return Collections.emptyList();
        List<TeamDto> list = cachedTeams.get(tournamentId);
        if (list != null && !list.isEmpty()) {
            return new ArrayList<>(list);
        }
        if (firebase == null) return Collections.emptyList();
        try {
            List<TeamDto> fetched = firebase.getTeams(tournamentId);
            if (fetched != null) {
                cachedTeams.put(tournamentId, new CopyOnWriteArrayList<>(fetched));
                return new ArrayList<>(fetched);
            }
        } catch (Exception e) {
            System.err.println("getTeams error: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    public boolean registerTeam(TeamDto team) {
        if (firebase == null) return false;
        boolean ok = firebase.registerTeam(team, currentUser.getUsername());
        if (ok) {
            if (team.getId() != null) sessionUnlockedTeams.add(team.getId());
            if (team.getTournamentId() != null) {
                cachedTeams.computeIfAbsent(team.getTournamentId(), k -> new CopyOnWriteArrayList<>()).add(team);
            }
        }
        return ok;
    }

    public boolean isTeamUnlockedThisSession(String teamId) {
        return sessionUnlockedTeams.contains(teamId);
    }

    public boolean unlockTeamWithPassword(String teamId, String password) {
        if (firebase == null) return false;
        boolean valid = firebase.verifyTeamPassword(teamId, password);
        if (valid) {
            sessionUnlockedTeams.add(teamId);
        }
        return valid;
    }

    public boolean updateTeam(String teamId, Map<String, Object> updates) {
        if (firebase == null) return false;
        boolean ok = firebase.updateTeam(teamId, updates, currentUser.getUsername());
        if (ok) {
            // Update cache
            for (List<TeamDto> list : cachedTeams.values()) {
                for (TeamDto t : list) {
                    if (t.getId().equals(teamId)) {
                        if (updates.containsKey("name")) t.setName((String) updates.get("name"));
                        if (updates.containsKey("tag")) t.setTag((String) updates.get("tag"));
                        if (updates.containsKey("captainEmail")) t.setCaptainEmail((String) updates.get("captainEmail"));
                        break;
                    }
                }
            }
        }
        return ok;
    }

    public boolean removePlayerFromTeam(String teamId, String playerName) {
        if (firebase == null) return false;
        boolean ok = firebase.removePlayerFromTeam(teamId, playerName, currentUser.getUsername());
        if (ok) {
            for (List<TeamDto> list : cachedTeams.values()) {
                for (TeamDto t : list) {
                    if (t.getId().equals(teamId) && t.getPlayerNames() != null) {
                        t.getPlayerNames().remove(playerName);
                        break;
                    }
                }
            }
        }
        return ok;
    }

    public boolean deleteTeam(String teamId) {
        if (firebase == null) return false;
        boolean ok = firebase.deleteTeam(teamId, currentUser.getUsername());
        if (ok) {
            sessionUnlockedTeams.remove(teamId);
            for (List<TeamDto> list : cachedTeams.values()) {
                list.removeIf(t -> t.getId().equals(teamId));
            }
        }
        return ok;
    }

    public boolean updateTeamStatus(String teamId, String status, String reason) {
        if (firebase == null) return false;
        return firebase.updateTeamStatus(teamId, status, reason, currentUser.getUsername());
    }

    public boolean setTeamCheckIn(String teamId, boolean checkedIn) {
        if (firebase == null) return false;
        boolean ok = firebase.setTeamCheckIn(teamId, checkedIn, currentUser.getUsername());
        if (ok) {
            for (List<TeamDto> list : cachedTeams.values()) {
                for (TeamDto t : list) {
                    if (t.getId().equals(teamId)) {
                        t.setCheckedIn(checkedIn);
                        break;
                    }
                }
            }
        }
        return ok;
    }

    // ==================== MATCHES ====================

    public List<MatchDto> getMatches(String tournamentId) {
        if (tournamentId == null) return Collections.emptyList();
        List<MatchDto> list = cachedMatches.get(tournamentId);
        if (list != null && !list.isEmpty()) {
            return new ArrayList<>(list);
        }
        if (firebase == null) return Collections.emptyList();
        try {
            List<MatchDto> fetched = firebase.getMatches(tournamentId);
            if (fetched != null) {
                cachedMatches.put(tournamentId, new CopyOnWriteArrayList<>(fetched));
                return new ArrayList<>(fetched);
            }
        } catch (Exception e) {
            System.err.println("getMatches error: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    public boolean deleteMatch(String matchId) {
        if (firebase == null) return false;
        boolean ok = firebase.deleteMatch(matchId, currentUser.getUsername());
        if (ok) {
            for (List<MatchDto> list : cachedMatches.values()) {
                list.removeIf(m -> m.getId().equals(matchId));
            }
        }
        return ok;
    }

    public List<MatchDto> generateBracket(String tournamentId) {
        if (firebase == null) return Collections.emptyList();
        List<MatchDto> matches = firebase.generateBracket(tournamentId, currentUser.getUsername());
        if (matches != null) {
            cachedMatches.put(tournamentId, new CopyOnWriteArrayList<>(matches));
        }
        return matches;
    }

    public boolean updateMatchSchedule(String matchId, String time, String roomId, String password, String map) {
        if (firebase == null) return false;
        return firebase.updateMatchSchedule(matchId, time, roomId, password, map, currentUser.getUsername());
    }

    public boolean setMatchLive(String matchId, int scoreA, int scoreB) {
        if (firebase == null) return false;
        boolean ok = firebase.setMatchLive(matchId, scoreA, scoreB, currentUser.getUsername());
        if (ok) {
            updateMatchInCache(matchId, m -> {
                m.setScoreA(scoreA);
                m.setScoreB(scoreB);
                m.setStatus("LIVE");
            });
        }
        return ok;
    }

    public boolean updateLiveMatch(String matchId, int scoreA, int scoreB, int killsA, int killsB, String status) {
        if (firebase == null) return false;
        boolean ok = firebase.updateLiveMatch(matchId, scoreA, scoreB, killsA, killsB, status, currentUser.getUsername());
        if (ok) {
            updateMatchInCache(matchId, m -> {
                m.setScoreA(scoreA);
                m.setScoreB(scoreB);
                m.setKillsA(killsA);
                m.setKillsB(killsB);
                m.setStatus(status);
            });
        }
        return ok;
    }

    public boolean recordMatchScore(String matchId, int scoreA, int scoreB, String reason) {
        return recordMatchScore(matchId, scoreA, scoreB, 0, 0, reason);
    }

    public boolean recordMatchScore(String matchId, int scoreA, int scoreB, int killsA, int killsB, String reason) {
        if (firebase == null) return false;
        boolean ok = firebase.recordMatchScore(matchId, scoreA, scoreB, killsA, killsB, reason, currentUser.getUsername());
        if (ok) {
            updateMatchInCache(matchId, m -> {
                m.setScoreA(scoreA);
                m.setScoreB(scoreB);
                m.setKillsA(killsA);
                m.setKillsB(killsB);
                m.setStatus("COMPLETED");
            });
        }
        return ok;
    }

    public boolean updateMatchDetails(String matchId, Map<String, Object> updates) {
        if (firebase == null) return false;
        return firebase.updateMatchDetails(matchId, updates, currentUser.getUsername());
    }

    private void updateMatchInCache(String matchId, Consumer<MatchDto> modifier) {
        for (List<MatchDto> list : cachedMatches.values()) {
            for (MatchDto m : list) {
                if (m.getId().equals(matchId)) {
                    modifier.accept(m);
                    break;
                }
            }
        }
    }

    // ==================== DISPUTES & CONDUCT ====================

    public List<DisputeDto> getDisputes(String tournamentId) {
        if (tournamentId == null) return Collections.emptyList();
        List<DisputeDto> list = cachedDisputes.get(tournamentId);
        if (list != null && !list.isEmpty()) return new ArrayList<>(list);
        if (firebase == null) return Collections.emptyList();
        List<DisputeDto> fetched = firebase.getDisputes(tournamentId);
        if (fetched != null) {
            cachedDisputes.put(tournamentId, new CopyOnWriteArrayList<>(fetched));
            return new ArrayList<>(fetched);
        }
        return Collections.emptyList();
    }

    public boolean createDispute(DisputeDto dispute) {
        if (firebase == null) return false;
        return firebase.createDispute(dispute, currentUser.getUsername());
    }

    public boolean resolveDispute(String disputeId, String decision, String notes) {
        if (firebase == null) return false;
        return firebase.resolveDispute(disputeId, decision, notes, currentUser.getUsername());
    }

    public List<ConductReportDto> getConductReports(String tournamentId) {
        if (tournamentId == null) return Collections.emptyList();
        List<ConductReportDto> list = cachedReports.get(tournamentId);
        if (list != null && !list.isEmpty()) return new ArrayList<>(list);
        if (firebase == null) return Collections.emptyList();
        List<ConductReportDto> fetched = firebase.getConductReports(tournamentId);
        if (fetched != null) {
            cachedReports.put(tournamentId, new CopyOnWriteArrayList<>(fetched));
            return new ArrayList<>(fetched);
        }
        return Collections.emptyList();
    }

    public boolean resolveConductReport(String reportId, String action, String reason) {
        if (firebase == null) return false;
        return firebase.resolveConductReport(reportId, action, reason, currentUser.getUsername());
    }

    public List<AuditLogDto> getAuditLogs() {
        if (!cachedAudit.isEmpty()) return new ArrayList<>(cachedAudit);
        if (firebase == null) return Collections.emptyList();
        List<AuditLogDto> logs = firebase.getAuditLogs();
        if (logs != null) {
            cachedAudit = new CopyOnWriteArrayList<>(logs);
            return new ArrayList<>(cachedAudit);
        }
        return Collections.emptyList();
    }

    public boolean seedPrototypeData() {
        if (firebase == null) return false;
        boolean ok = firebase.seedPrototypeData(currentUser.getUsername());
        if (ok) {
            refreshAllAsync(null);
        }
        return ok;
    }

    // ==================== IN-APP UPDATER ====================

    public static class UpdateCheckResult {
        public boolean updateAvailable;
        public String latestVersion;
        public String releaseNotes;
        public String downloadUrl;
        public String jarUrl;
        public String message;
    }

    public void checkForUpdatesAsync(Consumer<UpdateCheckResult> onResult) {
        backgroundExecutor.submit(() -> {
            UpdateCheckResult res = new UpdateCheckResult();
            try {
                URL url = new URI(UPDATE_CHECK_URL + "?t=" + System.currentTimeMillis()).toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "NexusArenaDesktop/" + CURRENT_VERSION);

                if (conn.getResponseCode() == 200) {
                    try (Scanner sc = new Scanner(conn.getInputStream())) {
                        String json = sc.useDelimiter("\\A").hasNext() ? sc.next() : "{}";
                        // Simple parser for version.json
                        String version = extractJsonField(json, "version");
                        String jarUrl = extractJsonField(json, "jarUrl");
                        String notes = extractJsonField(json, "releaseNotes");
                        String dlUrl = extractJsonField(json, "downloadUrl");

                        res.latestVersion = version != null ? version : CURRENT_VERSION;
                        res.jarUrl = jarUrl;
                        res.releaseNotes = notes != null ? notes : "Performance updates and fixes.";
                        res.downloadUrl = dlUrl;

                        if (res.latestVersion != null && !res.latestVersion.equalsIgnoreCase(CURRENT_VERSION)) {
                            res.updateAvailable = true;
                            res.message = "New version " + res.latestVersion + " is available! (Current: " + CURRENT_VERSION + ")";
                        } else {
                            res.updateAvailable = false;
                            res.message = "Nexus Arena is up to date (Version " + CURRENT_VERSION + ").";
                        }
                    }
                } else {
                    res.message = "Update check returned status code: " + conn.getResponseCode();
                }
            } catch (Exception ex) {
                res.message = "Could not check for updates: " + ex.getMessage();
            } finally {
                if (onResult != null) {
                    Platform.runLater(() -> onResult.accept(res));
                }
            }
        });
    }

    private String extractJsonField(String json, String field) {
        int idx = json.indexOf("\"" + field + "\"");
        if (idx == -1) return null;
        int colon = json.indexOf(":", idx);
        if (colon == -1) return null;
        int firstQuote = json.indexOf("\"", colon);
        if (firstQuote == -1) return null;
        int secondQuote = json.indexOf("\"", firstQuote + 1);
        if (secondQuote == -1) return null;
        return json.substring(firstQuote + 1, secondQuote);
    }
}
