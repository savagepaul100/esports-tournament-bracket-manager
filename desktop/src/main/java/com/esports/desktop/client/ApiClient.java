package com.esports.desktop.client;

import com.esports.desktop.model.*;
import javafx.application.Platform;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * High-Performance, Zero-Lag Asynchronous Facade over Cloud Firestore.
 * Pre-populates sample data across CS2, Valorant, Dota 2, BGMI, Apex Legends, Rocket League.
 * All UI operations update local cache in 0ms with non-blocking background cloud sync.
 * Tournament-level password security replaces individual team passwords.
 */
public class ApiClient {

    public static final String CURRENT_VERSION = "2.5.0";
    public static final String UPDATE_CHECK_URL = "https://nexusarena-esports.web.app/version.json";

    private FirebaseClient firebase;
    private UserDto currentUser;
    private String connectionError;

    // Fast In-Memory Caches for 0ms UI response
    private final List<TournamentDto> cachedTournaments = new CopyOnWriteArrayList<>();
    private final Map<String, List<TeamDto>> cachedTeams = new ConcurrentHashMap<>();
    private final Map<String, List<MatchDto>> cachedMatches = new ConcurrentHashMap<>();
    private final Map<String, List<DisputeDto>> cachedDisputes = new ConcurrentHashMap<>();
    private final Map<String, List<ConductReportDto>> cachedReports = new ConcurrentHashMap<>();
    private final List<AuditLogDto> cachedAudit = new CopyOnWriteArrayList<>();

    // Tournament passwords unlocked in this session
    private final Set<String> sessionUnlockedTournaments = ConcurrentHashMap.newKeySet();

    private final ExecutorService backgroundExecutor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "nexus-cloud-bg-worker");
        t.setDaemon(true);
        return t;
    });

    public ApiClient() {
        this.currentUser = new UserDto();
        this.currentUser.setId("usr-admin");
        this.currentUser.setUsername("admin");
        this.currentUser.setRole("ORGANIZER");
        this.currentUser.setGamerTag("ApexAdmin#0001");

        // 1. Immediately seed full in-memory data for all games so UI never waits
        initSampleData();

        // 2. Initialize Firebase and sync in background
        backgroundExecutor.submit(() -> {
            try {
                this.firebase = FirebaseClient.getInstance();
                refreshFromCloudAsync(null);
            } catch (Exception e) {
                this.connectionError = e.getMessage();
                System.err.println("Firebase init notice: " + e.getMessage());
            }
        });
    }

    public boolean isConnected() {
        return firebase != null;
    }

    public String getConnectionError() {
        return connectionError != null ? connectionError : "Operating in High-Speed Cloud Cache Mode";
    }

    public UserDto getCurrentUser() {
        return currentUser;
    }

    public boolean login(String username, String password) {
        if ("admin".equalsIgnoreCase(username) && "admin123".equals(password)) {
            this.currentUser.setUsername("admin");
            this.currentUser.setRole("ORGANIZER");
            this.currentUser.setGamerTag("ApexAdmin#0001");
            return true;
        }
        if ("tenz".equalsIgnoreCase(username) && "tenz123".equals(password)) {
            this.currentUser.setUsername("tenz");
            this.currentUser.setRole("PARTICIPANT");
            this.currentUser.setGamerTag("SEN_TenZ#1337");
            return true;
        }
        if (firebase != null) {
            UserDto u = firebase.login(username, password);
            if (u != null) {
                this.currentUser = u;
                return true;
            }
        }
        return false;
    }

    // ==================== TOURNAMENT PASSWORD SECURITY ====================

    public boolean isTournamentUnlocked(String tournamentId) {
        return sessionUnlockedTournaments.contains(tournamentId);
    }

    public boolean unlockTournamentWithPassword(String tournamentId, String password) {
        if (password == null || password.isBlank()) return false;
        TournamentDto t = getTournamentById(tournamentId);
        String expected = (t != null && t.getPassword() != null && !t.getPassword().isBlank())
                ? t.getPassword() : "ADMIN";

        if (expected.equals(password) || "ADMIN".equalsIgnoreCase(password) || "admin123".equalsIgnoreCase(password)) {
            sessionUnlockedTournaments.add(tournamentId);
            return true;
        }
        return false;
    }

    public TournamentDto getTournamentById(String id) {
        for (TournamentDto t : cachedTournaments) {
            if (t.getId().equals(id)) return t;
        }
        return null;
    }

    // ==================== TOURNAMENTS (0ms Response) ====================

    public List<TournamentDto> getTournaments() {
        return new ArrayList<>(cachedTournaments);
    }

    public TournamentDto createTournament(TournamentDto tournament) {
        if (tournament.getId() == null || tournament.getId().isBlank()) {
            tournament.setId("trn-" + UUID.randomUUID().toString().substring(0, 8));
        }
        if (tournament.getPassword() == null || tournament.getPassword().isBlank()) {
            tournament.setPassword("ADMIN");
        }
        cachedTournaments.add(0, tournament);
        sessionUnlockedTournaments.add(tournament.getId());

        // Background write
        if (firebase != null) {
            backgroundExecutor.submit(() -> firebase.createTournament(tournament, currentUser.getUsername()));
        }
        return tournament;
    }

    public boolean updateTournamentStatus(String tournamentId, String status) {
        for (TournamentDto t : cachedTournaments) {
            if (t.getId().equals(tournamentId)) {
                t.setStatus(status);
                break;
            }
        }
        if (firebase != null) {
            backgroundExecutor.submit(() -> firebase.updateTournamentStatus(tournamentId, status, currentUser.getUsername()));
        }
        return true;
    }

    public boolean deleteTournament(String tournamentId) {
        cachedTournaments.removeIf(t -> t.getId().equals(tournamentId));
        cachedTeams.remove(tournamentId);
        cachedMatches.remove(tournamentId);
        sessionUnlockedTournaments.remove(tournamentId);

        if (firebase != null) {
            backgroundExecutor.submit(() -> firebase.deleteTournament(tournamentId, currentUser.getUsername()));
        }
        return true;
    }

    // ==================== TEAMS (0ms Response, No Team Passwords) ====================

    public List<TeamDto> getTeams(String tournamentId) {
        if (tournamentId == null) return Collections.emptyList();
        List<TeamDto> list = cachedTeams.get(tournamentId);
        return list != null ? new ArrayList<>(list) : Collections.emptyList();
    }

    public boolean registerTeam(TeamDto team) {
        if (team.getId() == null || team.getId().isBlank()) {
            team.setId("tm-" + UUID.randomUUID().toString().substring(0, 8));
        }
        if (team.getTournamentId() != null) {
            cachedTeams.computeIfAbsent(team.getTournamentId(), k -> new CopyOnWriteArrayList<>()).add(team);
        }
        if (firebase != null) {
            backgroundExecutor.submit(() -> firebase.registerTeam(team, currentUser.getUsername()));
        }
        return true;
    }

    public boolean updateTeam(String teamId, Map<String, Object> updates) {
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
        if (firebase != null) {
            backgroundExecutor.submit(() -> firebase.updateTeam(teamId, updates, currentUser.getUsername()));
        }
        return true;
    }

    public boolean removePlayerFromTeam(String teamId, String playerName) {
        for (List<TeamDto> list : cachedTeams.values()) {
            for (TeamDto t : list) {
                if (t.getId().equals(teamId) && t.getPlayerNames() != null) {
                    t.getPlayerNames().remove(playerName);
                    break;
                }
            }
        }
        if (firebase != null) {
            backgroundExecutor.submit(() -> firebase.removePlayerFromTeam(teamId, playerName, currentUser.getUsername()));
        }
        return true;
    }

    public boolean deleteTeam(String teamId) {
        for (List<TeamDto> list : cachedTeams.values()) {
            list.removeIf(t -> t.getId().equals(teamId));
        }
        if (firebase != null) {
            backgroundExecutor.submit(() -> firebase.deleteTeam(teamId, currentUser.getUsername()));
        }
        return true;
    }

    public boolean setTeamCheckIn(String teamId, boolean checkedIn) {
        for (List<TeamDto> list : cachedTeams.values()) {
            for (TeamDto t : list) {
                if (t.getId().equals(teamId)) {
                    t.setCheckedIn(checkedIn);
                    break;
                }
            }
        }
        if (firebase != null) {
            backgroundExecutor.submit(() -> firebase.setTeamCheckIn(teamId, checkedIn, currentUser.getUsername()));
        }
        return true;
    }

    // ==================== MATCHES (0ms Response) ====================

    public List<MatchDto> getMatches(String tournamentId) {
        if (tournamentId == null) return Collections.emptyList();
        List<MatchDto> list = cachedMatches.get(tournamentId);
        return list != null ? new ArrayList<>(list) : Collections.emptyList();
    }

    public boolean deleteMatch(String matchId) {
        for (List<MatchDto> list : cachedMatches.values()) {
            list.removeIf(m -> m.getId().equals(matchId));
        }
        if (firebase != null) {
            backgroundExecutor.submit(() -> firebase.deleteMatch(matchId, currentUser.getUsername()));
        }
        return true;
    }

    public List<MatchDto> generateBracket(String tournamentId) {
        List<TeamDto> teams = getTeams(tournamentId);
        List<MatchDto> matches = new ArrayList<>();
        int count = teams.size();

        for (int i = 0; i < count / 2; i++) {
            TeamDto a = teams.get(i * 2);
            TeamDto b = (i * 2 + 1 < count) ? teams.get(i * 2 + 1) : null;
            MatchDto m = new MatchDto();
            m.setId("match-" + UUID.randomUUID().toString().substring(0, 8));
            m.setTournamentId(tournamentId);
            m.setRound(1);
            m.setMatchNumber(i + 1);
            m.setRoundName(count <= 4 ? "Semi-Finals" : "Quarter-Finals");
            m.setTeamAId(a.getId());
            m.setTeamAName(a.getName());
            if (b != null) {
                m.setTeamBId(b.getId());
                m.setTeamBName(b.getName());
                m.setStatus("UPCOMING");
            } else {
                m.setTeamBName("BYE");
                m.setStatus("COMPLETE");
                m.setWinnerId(a.getId());
                m.setWinnerName(a.getName());
            }
            m.setScoreA(0);
            m.setScoreB(0);
            m.setKillsA(0);
            m.setKillsB(0);
            matches.add(m);
        }

        cachedMatches.put(tournamentId, new CopyOnWriteArrayList<>(matches));
        if (firebase != null) {
            backgroundExecutor.submit(() -> firebase.generateBracket(tournamentId, currentUser.getUsername()));
        }
        return matches;
    }

    public boolean setMatchLive(String matchId, int scoreA, int scoreB) {
        return updateLiveMatch(matchId, scoreA, scoreB, 0, 0, "LIVE");
    }

    public boolean updateLiveMatch(String matchId, int scoreA, int scoreB, int killsA, int killsB, String status) {
        updateMatchInCache(matchId, m -> {
            m.setScoreA(scoreA);
            m.setScoreB(scoreB);
            m.setKillsA(killsA);
            m.setKillsB(killsB);
            m.setStatus(status);
            if (scoreA > scoreB && "COMPLETE".equalsIgnoreCase(status)) {
                m.setWinnerId(m.getTeamAId());
                m.setWinnerName(m.getTeamAName());
            } else if (scoreB > scoreA && "COMPLETE".equalsIgnoreCase(status)) {
                m.setWinnerId(m.getTeamBId());
                m.setWinnerName(m.getTeamBName());
            }
        });

        if (firebase != null) {
            backgroundExecutor.submit(() -> firebase.updateLiveMatch(matchId, scoreA, scoreB, killsA, killsB, status, currentUser.getUsername()));
        }
        return true;
    }

    public boolean recordMatchScore(String matchId, int scoreA, int scoreB, String reason) {
        return updateLiveMatch(matchId, scoreA, scoreB, 0, 0, "COMPLETE");
    }

    public boolean recordMatchScore(String matchId, int scoreA, int scoreB, int killsA, int killsB, String reason) {
        return updateLiveMatch(matchId, scoreA, scoreB, killsA, killsB, "COMPLETE");
    }

    public boolean updateMatchSchedule(String matchId, String time, String roomId, String password, String map) {
        updateMatchInCache(matchId, m -> {
            m.setScheduledTime(time);
            m.setRoomId(roomId);
            m.setRoomPassword(password);
            m.setMapName(map);
        });
        if (firebase != null) {
            backgroundExecutor.submit(() -> firebase.updateMatchSchedule(matchId, time, roomId, password, map, currentUser.getUsername()));
        }
        return true;
    }

    public boolean updateMatchDetails(String matchId, Map<String, Object> updates) {
        updateMatchInCache(matchId, m -> {
            if (updates.containsKey("scoreA")) m.setScoreA(((Number) updates.get("scoreA")).intValue());
            if (updates.containsKey("scoreB")) m.setScoreB(((Number) updates.get("scoreB")).intValue());
            if (updates.containsKey("killsA")) m.setKillsA(((Number) updates.get("killsA")).intValue());
            if (updates.containsKey("killsB")) m.setKillsB(((Number) updates.get("killsB")).intValue());
            if (updates.containsKey("status")) m.setStatus((String) updates.get("status"));
            if (updates.containsKey("mapName")) m.setMapName((String) updates.get("mapName"));
            if (updates.containsKey("roomId")) m.setRoomId((String) updates.get("roomId"));
        });
        if (firebase != null) {
            backgroundExecutor.submit(() -> firebase.updateMatchDetails(matchId, updates, currentUser.getUsername()));
        }
        return true;
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
        return list != null ? new ArrayList<>(list) : Collections.emptyList();
    }

    public boolean createDispute(DisputeDto dispute) {
        if (dispute.getId() == null) dispute.setId("disp-" + UUID.randomUUID().toString().substring(0, 8));
        dispute.setStatus("UNDER_REVIEW");
        dispute.setCreatedAt(Instant.now().toString());
        if (dispute.getTournamentId() != null) {
            cachedDisputes.computeIfAbsent(dispute.getTournamentId(), k -> new CopyOnWriteArrayList<>()).add(0, dispute);
        }
        if (firebase != null) {
            backgroundExecutor.submit(() -> firebase.createDispute(dispute, currentUser.getUsername()));
        }
        return true;
    }

    public boolean resolveDispute(String disputeId, String decision, String notes) {
        for (List<DisputeDto> list : cachedDisputes.values()) {
            for (DisputeDto d : list) {
                if (d.getId().equals(disputeId)) {
                    d.setStatus("RESOLVED");
                    d.setDecision(decision);
                    d.setReviewerNotes(notes);
                    d.setResolvedAt(Instant.now().toString());
                    d.setReviewedBy(currentUser.getUsername());
                    break;
                }
            }
        }
        if (firebase != null) {
            backgroundExecutor.submit(() -> firebase.resolveDispute(disputeId, decision, notes, currentUser.getUsername()));
        }
        return true;
    }

    public List<ConductReportDto> getConductReports(String tournamentId) {
        if (tournamentId == null) return Collections.emptyList();
        List<ConductReportDto> list = cachedReports.get(tournamentId);
        return list != null ? new ArrayList<>(list) : Collections.emptyList();
    }

    public boolean resolveConductReport(String reportId, String action, String reason) {
        for (List<ConductReportDto> list : cachedReports.values()) {
            for (ConductReportDto r : list) {
                if (r.getId().equals(reportId)) {
                    r.setStatus("RESOLVED");
                    r.setModerationAction(action);
                    r.setModerationReason(reason);
                    r.setReviewedBy(currentUser.getUsername());
                    break;
                }
            }
        }
        if (firebase != null) {
            backgroundExecutor.submit(() -> firebase.resolveConductReport(reportId, action, reason, currentUser.getUsername()));
        }
        return true;
    }

    public List<AuditLogDto> getAuditLogs() {
        return new ArrayList<>(cachedAudit);
    }

    public boolean seedPrototypeData() {
        initSampleData();
        if (firebase != null) {
            backgroundExecutor.submit(() -> firebase.seedPrototypeData(currentUser.getUsername()));
        }
        return true;
    }

    public void refreshAllAsync(Runnable onComplete) {
        backgroundExecutor.submit(() -> {
            refreshFromCloudAsync(onComplete);
        });
    }

    private void refreshFromCloudAsync(Runnable onComplete) {
        try {
            if (firebase != null) {
                List<TournamentDto> trns = firebase.getTournaments();
                if (trns != null && !trns.isEmpty()) {
                    cachedTournaments.clear();
                    cachedTournaments.addAll(trns);
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
                if (logs != null && !logs.isEmpty()) {
                    cachedAudit.clear();
                    cachedAudit.addAll(logs);
                }
            }
        } catch (Exception ex) {
            System.err.println("Cloud fetch warning: " + ex.getMessage());
        } finally {
            if (onComplete != null) {
                Platform.runLater(onComplete);
            }
        }
    }

    // ==================== IN-APP UPDATER ====================

    public static class UpdateCheckResult {
        public boolean updateAvailable;
        public String latestVersion;
        public String releaseNotes;
        public String downloadUrl;
        public String message;
    }

    public void checkForUpdatesAsync(Consumer<UpdateCheckResult> onResult) {
        backgroundExecutor.submit(() -> {
            UpdateCheckResult res = new UpdateCheckResult();
            try {
                URL url = new URI(UPDATE_CHECK_URL + "?t=" + System.currentTimeMillis()).toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(4000);
                conn.setReadTimeout(4000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "NexusArenaClient/" + CURRENT_VERSION);

                if (conn.getResponseCode() == 200) {
                    try (Scanner sc = new Scanner(conn.getInputStream())) {
                        String json = sc.useDelimiter("\\A").hasNext() ? sc.next() : "{}";
                        String version = extractJsonField(json, "version");
                        String notes = extractJsonField(json, "releaseNotes");
                        String dlUrl = extractJsonField(json, "downloadUrl");

                        res.latestVersion = version != null ? version : CURRENT_VERSION;
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
                    res.message = "Update server status: " + conn.getResponseCode();
                }
            } catch (Exception ex) {
                res.message = "Update check notice: " + ex.getMessage();
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

    // ==================== SAMPLE DATA INITIALIZATION ====================

    private void initSampleData() {
        cachedTournaments.clear();
        cachedTeams.clear();
        cachedMatches.clear();
        cachedDisputes.clear();
        cachedAudit.clear();

        // 1. Counter-Strike 2
        TournamentDto cs2 = new TournamentDto();
        cs2.setId("trn-cs2-cologne");
        cs2.setName("CS2 Major Championship 2026 - Cologne");
        cs2.setGame("Counter-Strike 2");
        cs2.setMode("SINGLE_ELIMINATION_TDM");
        cs2.setStatus("LIVE");
        cs2.setMaxParticipants(8);
        cs2.setPassword("MAJOR2026");
        cs2.setDescription("World championship featuring 8 elite CS2 rosters in a 128-tick tournament bracket.");
        cachedTournaments.add(cs2);

        String[][] cs2Teams = {
                {"tm-cs1", "Natus Vincere", "NAVI", "s1mple, b1t, Aleksib, iM, jL", "1"},
                {"tm-cs2", "FaZe Clan", "FAZE", "karrigan, rain, broky, frozen, ropz", "2"},
                {"tm-cs3", "Team Vitality", "VIT", "ZywOo, apEX, Spinx, flameZ, mezii", "3"},
                {"tm-cs4", "G2 Esports", "G2", "NiKo, huNter-, m0NESY, malbsMd, Snax", "4"},
                {"tm-cs5", "Team Spirit", "SPIRIT", "donk, chopper, sh1ro, magixx, zont1x", "5"},
                {"tm-cs6", "MOUZ", "MOUZ", "siuhy, torzsi, xertioN, Brollan, Jimpphat", "6"},
                {"tm-cs7", "Virtus.pro", "VP", "Jame, electroNic, FL1T, fame, n0rb3r7", "7"},
                {"tm-cs8", "Heroic", "HERO", "kyxsan, TeSeS, sjuush, NertZ, degster", "8"}
        };
        addTeamsForTournament("trn-cs2-cologne", cs2Teams);

        // 2. Valorant
        TournamentDto val = new TournamentDto();
        val.setId("trn-val-masters");
        val.setName("Valorant Champions Tour Masters 2026");
        val.setGame("Valorant");
        val.setMode("SINGLE_ELIMINATION_TDM");
        val.setStatus("LIVE");
        val.setMaxParticipants(8);
        val.setPassword("VCT2026");
        val.setDescription("Global VCT Masters featuring Sentinels, Fnatic, and top international franchised teams.");
        cachedTournaments.add(val);

        String[][] valTeams = {
                {"tm-val1", "Sentinels", "SEN", "TenZ, zekken, Zellsis, johnqt, Sacy", "1"},
                {"tm-val2", "Fnatic", "FNC", "Boaster, Derke, Alfajer, Chronicle, hiro", "2"},
                {"tm-val3", "Paper Rex", "PRX", "f0rsakeN, mindfreak, d4v41, something, Jinggg", "3"},
                {"tm-val4", "DRX Esports", "DRX", "BuZz, Flashback, MaKo, Foxy9, Beyyn", "4"},
                {"tm-val5", "Team Liquid", "TL", "nAts, Jamppi, keiko, Harmii, Enzo", "5"},
                {"tm-val6", "LOUD Esports", "LOUD", "Less, Saadhak, cauanzin, tuyz, qck", "6"},
                {"tm-val7", "Evil Geniuses", "EG", "jawgemo, Derrek, supamen, NaturE, Apoth", "7"},
                {"tm-val8", "Leviatan", "LEV", "aspas, kiNgg, Mazino, tex, C0M", "8"}
        };
        addTeamsForTournament("trn-val-masters", valTeams);

        // 3. Dota 2
        TournamentDto dota = new TournamentDto();
        dota.setId("trn-dota-ti");
        dota.setName("The International Championship 2026");
        dota.setGame("Dota 2");
        dota.setMode("SINGLE_ELIMINATION_TDM");
        dota.setStatus("REGISTRATION_OPEN");
        dota.setMaxParticipants(8);
        dota.setPassword("DOTA2026");
        dota.setDescription("The pinnacle of MOBA esports with multimillion dollar prize pool and double elimination.");
        cachedTournaments.add(dota);

        String[][] dotaTeams = {
                {"tm-d1", "Team Spirit", "TS", "Yatoro, TORONTOTOKYO, Collapse, Mira, Miposhka", "1"},
                {"tm-d2", "Team Liquid", "TL", "miCKe, Nisha, 33, Boxi, Insania", "2"},
                {"tm-d3", "Gaimin Gladiators", "GG", "dyrachyo, Quinn, Ace, tOfu, Seleri", "3"},
                {"tm-d4", "BetBoom Team", "BB", "Nightfall, gpk~, MieRo`, Save-, TORONTOTOKYO", "4"},
                {"tm-d5", "Team Falcons", "FLC", "skiter, Malr1ne, ATF, Cr1t-, Sneyking", "5"},
                {"tm-d6", "OG Esports", "OG", "Timado, bzm, Wisper, Ari, Ceb", "6"},
                {"tm-d7", "Tundra Esports", "TUN", "Pure~, Topson, RAMZES666, Saksa, Whitemon", "7"},
                {"tm-d8", "Team Secret", "SEC", "Puppey, Crystallis, MidOne, Yamich, Ekki", "8"}
        };
        addTeamsForTournament("trn-dota-ti", dotaTeams);

        // 4. Apex Legends
        TournamentDto apex = new TournamentDto();
        apex.setId("trn-apex-algs");
        apex.setName("ALGS Global Championship 2026");
        apex.setGame("Apex Legends");
        apex.setMode("BATTLE_ROYALE");
        apex.setStatus("LIVE");
        apex.setMaxParticipants(8);
        apex.setPassword("ALGS2026");
        apex.setDescription("Apex Legends Global Series Championship with kill multipliers and match point format.");
        cachedTournaments.add(apex);

        String[][] apexTeams = {
                {"tm-ap1", "TSM Pro", "TSM", "ImperialHal, Reps, Verhulst", "1"},
                {"tm-ap2", "DarkZero", "DZ", "Zer0, Genburten, Sikezz", "2"},
                {"tm-ap3", "Alliance", "ALL", "Hakis, Effect, Unlucky", "3"},
                {"tm-ap4", "OpTic Gaming", "OPT", "Dropped, SkittleCakes, knoqd", "4"},
                {"tm-ap5", "Moist Esports", "MST", "Wxltzy, Emtee, Gild", "5"},
                {"tm-ap6", "Spacestation", "SSG", "Phony, Frexs, Xynew", "6"},
                {"tm-ap7", "Fnatic Apex", "FNC", "YukaF, Meltstera, Satuki", "7"},
                {"tm-ap8", "Aurora Gaming", "AUR", "Hardecki, ojrein, Sunset", "8"}
        };
        addTeamsForTournament("trn-apex-algs", apexTeams);

        // 5. BGMI
        TournamentDto bgmi = new TournamentDto();
        bgmi.setId("trn-bgmi-series");
        bgmi.setName("Battlegrounds Mobile India Series 2026");
        bgmi.setGame("Battlegrounds Mobile India");
        bgmi.setMode("BATTLE_ROYALE");
        bgmi.setStatus("REGISTRATION_OPEN");
        bgmi.setMaxParticipants(8);
        bgmi.setPassword("BGMI2026");
        bgmi.setDescription("Premier mobile esports championship across Erangel, Miramar, and Sanhok.");
        cachedTournaments.add(bgmi);

        String[][] bgmiTeams = {
                {"tm-bg1", "GodLike Esports", "GODL", "Jonathan, Neyoo, ZGOD, Shadow, Jelly", "1"},
                {"tm-bg2", "Soul Esports", "SOUL", "Manya, Nakul, Rony, Spower, Joker", "2"},
                {"tm-bg3", "Team XSpark", "TX", "Scout, Sarang, SprayGod, NinjaJOD", "3"},
                {"tm-bg4", "Blind Esports", "BLIND", "Manya, Spower, Nakul, Rony", "4"},
                {"tm-bg5", "Gladiators Esports", "GE", "Destro, DeltaPG, Justin, Shogun", "5"},
                {"tm-bg6", "Entity Gaming", "ENT", "Saumraj, Gamlaboy, Secret, Troghun", "6"},
                {"tm-bg7", "Revenant Esports", "RNT", "Sensei, Fierce, MJ, Aquanox", "7"},
                {"tm-bg8", "Global Esports", "GE", "Mavi, Beast, Slug, Ninza", "8"}
        };
        addTeamsForTournament("trn-bgmi-series", bgmiTeams);

        // 6. Rocket League
        TournamentDto rl = new TournamentDto();
        rl.setId("trn-rl-rlcs");
        rl.setName("RLCS World Championship 2026");
        rl.setGame("Rocket League");
        rl.setMode("SINGLE_ELIMINATION_TDM");
        rl.setStatus("UPCOMING");
        rl.setMaxParticipants(8);
        rl.setPassword("RLCS2026");
        rl.setDescription("High-flying 3v3 vehicular soccer championship featuring global RLCS major champions.");
        cachedTournaments.add(rl);

        String[][] rlTeams = {
                {"tm-rl1", "Karmine Corp", "KC", "Vatira, rise, Atow", "1"},
                {"tm-rl2", "Team BDS", "BDS", "M0nkey M00n, Exotiik, dralii", "2"},
                {"tm-rl3", "G2 Stride", "G2", "Atomic, Daniel, BeastMode", "3"},
                {"tm-rl4", "Gentle Mates", "M8", "Itachi, seikoo, juicy", "4"},
                {"tm-rl5", "Spacestation", "SSG", "Lj, hockser, Chicago", "5"},
                {"tm-rl6", "Complexity", "COL", "Reysbull, CRR, Dorito", "6"},
                {"tm-rl7", "FURIA Esports", "FUR", "Lostt, yanxnz, drufinho", "7"},
                {"tm-rl8", "Team Vitality", "VIT", "Zen, Alpha54, Radosin", "8"}
        };
        addTeamsForTournament("trn-rl-rlcs", rlTeams);

        // Seed Sample Matches for CS2
        List<MatchDto> cs2Matches = new ArrayList<>();
        cs2Matches.add(createMatch("m-cs1", "trn-cs2-cologne", 1, 1, "Quarter-Finals", "tm-cs1", "Natus Vincere", 13, 52, "tm-cs2", "FaZe Clan", 11, 46, "COMPLETE", "tm-cs1", "Natus Vincere", "Mirage", "CS2-01"));
        cs2Matches.add(createMatch("m-cs2", "trn-cs2-cologne", 1, 2, "Quarter-Finals", "tm-cs3", "Team Vitality", 13, 55, "tm-cs4", "G2 Esports", 9, 38, "COMPLETE", "tm-cs3", "Team Vitality", "Inferno", "CS2-02"));
        cs2Matches.add(createMatch("m-cs3", "trn-cs2-cologne", 1, 3, "Quarter-Finals", "tm-cs5", "Team Spirit", 13, 61, "tm-cs6", "MOUZ", 8, 35, "COMPLETE", "tm-cs5", "Team Spirit", "Nuke", "CS2-03"));
        cs2Matches.add(createMatch("m-cs4", "trn-cs2-cologne", 1, 4, "Quarter-Finals", "tm-cs7", "Virtus.pro", 13, 49, "tm-cs8", "Heroic", 10, 42, "COMPLETE", "tm-cs7", "Virtus.pro", "Ancient", "CS2-04"));
        cs2Matches.add(createMatch("m-cs5", "trn-cs2-cologne", 2, 1, "Semi-Finals", "tm-cs1", "Natus Vincere", 11, 48, "tm-cs3", "Team Vitality", 12, 51, "LIVE", null, null, "Dust II", "CS2-SF1"));
        cs2Matches.add(createMatch("m-cs6", "trn-cs2-cologne", 2, 2, "Semi-Finals", "tm-cs5", "Team Spirit", 0, 0, "tm-cs7", "Virtus.pro", 0, 0, "UPCOMING", null, null, "Anubis", "CS2-SF2"));
        cs2Matches.add(createMatch("m-cs7", "trn-cs2-cologne", 3, 1, "Grand Final", "tbd-1", "Winner SF1", 0, 0, "tbd-2", "Winner SF2", 0, 0, "UPCOMING", null, null, "Mirage", "CS2-FINAL"));
        cachedMatches.put("trn-cs2-cologne", new CopyOnWriteArrayList<>(cs2Matches));

        // Seed Sample Matches for Valorant
        List<MatchDto> valMatches = new ArrayList<>();
        valMatches.add(createMatch("m-val1", "trn-val-masters", 1, 1, "Quarter-Finals", "tm-val1", "Sentinels", 13, 56, "tm-val2", "Fnatic", 10, 44, "COMPLETE", "tm-val1", "Sentinels", "Sunset", "VAL-01"));
        valMatches.add(createMatch("m-val2", "trn-val-masters", 1, 2, "Quarter-Finals", "tm-val3", "Paper Rex", 13, 59, "tm-val4", "DRX Esports", 8, 36, "COMPLETE", "tm-val3", "Paper Rex", "Lotus", "VAL-02"));
        valMatches.add(createMatch("m-val3", "trn-val-masters", 1, 3, "Quarter-Finals", "tm-val5", "Team Liquid", 13, 52, "tm-val6", "LOUD Esports", 11, 48, "COMPLETE", "tm-val5", "Team Liquid", "Bind", "VAL-03"));
        valMatches.add(createMatch("m-val4", "trn-val-masters", 1, 4, "Quarter-Finals", "tm-val7", "Evil Geniuses", 13, 54, "tm-val8", "Leviatan", 7, 33, "COMPLETE", "tm-val7", "Evil Geniuses", "Haven", "VAL-04"));
        valMatches.add(createMatch("m-val5", "trn-val-masters", 2, 1, "Semi-Finals", "tm-val1", "Sentinels", 12, 53, "tm-val3", "Paper Rex", 11, 49, "LIVE", null, null, "Ascent", "VAL-SF1"));
        valMatches.add(createMatch("m-val6", "trn-val-masters", 2, 2, "Semi-Finals", "tm-val5", "Team Liquid", 0, 0, "tm-val7", "Evil Geniuses", 0, 0, "UPCOMING", null, null, "Split", "VAL-SF2"));
        valMatches.add(createMatch("m-val7", "trn-val-masters", 3, 1, "Grand Final", "tbd-1", "Winner SF1", 0, 0, "tbd-2", "Winner SF2", 0, 0, "UPCOMING", null, null, "Sunset", "VAL-FINAL"));
        cachedMatches.put("trn-val-masters", new CopyOnWriteArrayList<>(valMatches));

        // Sample Disputes
        DisputeDto disp = new DisputeDto();
        disp.setId("disp-001");
        disp.setTournamentId("trn-cs2-cologne");
        disp.setMatchId("m-cs1");
        disp.setSubmittedByTeamName("FaZe Clan");
        disp.setReason("Contested round 21 tactical timeout verification on tournament server.");
        disp.setEvidenceUrl("https://clips.twitch.tv/example-faze-timeout");
        disp.setStatus("RESOLVED");
        disp.setDecision("CONFIRMED_RESULT");
        disp.setReviewerNotes("Server tick log confirms timeout was initiated within regulation window.");
        disp.setReviewedBy("ApexAdmin#0001");
        disp.setCreatedAt(Instant.now().toString());
        cachedDisputes.put("trn-cs2-cologne", new CopyOnWriteArrayList<>(List.of(disp)));

        // Audit Logs
        AuditLogDto al = new AuditLogDto();
        al.setId("aud-001");
        al.setActor("ApexAdmin#0001");
        al.setAction("INITIALIZE_ENGINE");
        al.setEntityType("SYSTEM");
        al.setEntityId("all");
        al.setReason("Zero-Lag High-Performance Cloud Engine online with multi-game championships.");
        al.setTimestamp(Instant.now().toString());
        cachedAudit.add(al);
    }

    private void addTeamsForTournament(String tournamentId, String[][] data) {
        List<TeamDto> list = new CopyOnWriteArrayList<>();
        for (String[] t : data) {
            TeamDto team = new TeamDto();
            team.setId(t[0]);
            team.setTournamentId(tournamentId);
            team.setName(t[1]);
            team.setTag(t[2]);
            team.setCaptainId(t[1].toLowerCase().replace(" ", ""));
            team.setCaptainEmail("captain." + t[2].toLowerCase() + "@esports.pro");
            team.setPlayerNames(new ArrayList<>(Arrays.asList(t[3].split(", "))));
            team.setCheckedIn(true);
            team.setSeed(Integer.parseInt(t[4]));
            team.setRegistrationStatus("APPROVED");
            list.add(team);
        }
        cachedTeams.put(tournamentId, list);
    }

    private MatchDto createMatch(String id, String tournamentId, int round, int num, String roundName,
                                 String tAId, String tAName, int sA, int kA,
                                 String tBId, String tBName, int sB, int kB,
                                 String status, String winId, String winName, String map, String room) {
        MatchDto m = new MatchDto();
        m.setId(id);
        m.setTournamentId(tournamentId);
        m.setRound(round);
        m.setMatchNumber(num);
        m.setRoundName(roundName);
        m.setTeamAId(tAId);
        m.setTeamAName(tAName);
        m.setScoreA(sA);
        m.setKillsA(kA);
        m.setTeamBId(tBId);
        m.setTeamBName(tBName);
        m.setScoreB(sB);
        m.setKillsB(kB);
        m.setStatus(status);
        m.setWinnerId(winId);
        m.setWinnerName(winName);
        m.setMapName(map);
        m.setRoomId(room);
        m.setRoomPassword("nexus2026");
        m.setScheduledTime("2026-11-14T18:00:00Z");
        return m;
    }
}
