package com.esports.manager.repository;

import com.esports.manager.model.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Repository("localJsonDataStore")
public class LocalJsonDataStore implements DataStore {
    private static final Logger log = LoggerFactory.getLogger(LocalJsonDataStore.class);
    private final Path dataDir;
    private final ObjectMapper mapper;

    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<String, Tournament> tournaments = new ConcurrentHashMap<>();
    private final Map<String, Team> teams = new ConcurrentHashMap<>();
    private final Map<String, Match> matches = new ConcurrentHashMap<>();
    private final Map<String, Dispute> disputes = new ConcurrentHashMap<>();
    private final Map<String, ConductReport> conductReports = new ConcurrentHashMap<>();
    private final Map<String, Notification> notifications = new ConcurrentHashMap<>();
    private final List<AuditLog> auditLogs = Collections.synchronizedList(new ArrayList<>());

    public LocalJsonDataStore() {
        this(Paths.get("data"));
    }

    public LocalJsonDataStore(Path dataDir) {
        this.dataDir = dataDir;
        this.mapper = new ObjectMapper();
        this.mapper.registerModule(new JavaTimeModule());
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);

        initStorage();
    }

    private void initStorage() {
        try {
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
            }
            loadFromDisk();
            if (tournaments.isEmpty() && users.isEmpty()) {
                seedDemoData();
                saveAllToDisk();
            }
        } catch (Exception e) {
            log.error("Failed to initialize local data storage", e);
        }
    }

    private synchronized void loadFromDisk() {
        loadCollection("users.json", new TypeReference<Map<String, User>>() {}, users);
        loadCollection("tournaments.json", new TypeReference<Map<String, Tournament>>() {}, tournaments);
        loadCollection("teams.json", new TypeReference<Map<String, Team>>() {}, teams);
        loadCollection("matches.json", new TypeReference<Map<String, Match>>() {}, matches);
        loadCollection("disputes.json", new TypeReference<Map<String, Dispute>>() {}, disputes);
        loadCollection("conduct_reports.json", new TypeReference<Map<String, ConductReport>>() {}, conductReports);
        loadCollection("notifications.json", new TypeReference<Map<String, Notification>>() {}, notifications);

        File auditFile = dataDir.resolve("audit_logs.json").toFile();
        if (auditFile.exists()) {
            try {
                List<AuditLog> list = mapper.readValue(auditFile, new TypeReference<List<AuditLog>>() {});
                auditLogs.clear();
                auditLogs.addAll(list);
            } catch (IOException e) {
                log.warn("Could not load audit logs: {}", e.getMessage());
            }
        }
    }

    private <T> void loadCollection(String filename, TypeReference<Map<String, T>> typeRef, Map<String, T> targetMap) {
        File file = dataDir.resolve(filename).toFile();
        if (file.exists()) {
            try {
                Map<String, T> loaded = mapper.readValue(file, typeRef);
                targetMap.clear();
                targetMap.putAll(loaded);
            } catch (IOException e) {
                log.warn("Could not load {}: {}", filename, e.getMessage());
            }
        }
    }

    public synchronized void persistCollection(String filename, Object data) {
        try {
            File file = dataDir.resolve(filename).toFile();
            mapper.writeValue(file, data);
        } catch (IOException e) {
            log.error("Error writing collection {}", filename, e);
        }
    }

    public synchronized void saveAllToDisk() {
        persistCollection("users.json", users);
        persistCollection("tournaments.json", tournaments);
        persistCollection("teams.json", teams);
        persistCollection("matches.json", matches);
        persistCollection("disputes.json", disputes);
        persistCollection("conduct_reports.json", conductReports);
        persistCollection("notifications.json", notifications);
        persistCollection("audit_logs.json", auditLogs);
    }

    private void seedDemoData() {
        log.info("Seeding rich Esports demo data into local store...");

        // Users
        User admin = new User("usr-admin", "admin@apexarena.gg", "admin", "admin123", Role.ORGANIZER, "ApexAdmin#0001");
        User tenz = new User("usr-tenz", "tenz@sentinels.gg", "tenz", "tenz123", Role.PARTICIPANT, "TenZ#SEN");
        User boaster = new User("usr-boaster", "boaster@fnatic.com", "boaster", "boaster123", Role.PARTICIPANT, "Boaster#FNC");
        User spectator = new User("usr-fan", "fan@gmail.com", "spectator", "fan123", Role.VIEWER, "HypeViewer#9999");
        users.put(admin.getId(), admin);
        users.put(tenz.getId(), tenz);
        users.put(boaster.getId(), boaster);
        users.put(spectator.getId(), spectator);

        // Tournament 1: VALORANT MASTERS (Single Elimination TDM)
        Tournament t1 = new Tournament();
        t1.setId("trn-val-masters");
        t1.setName("VALORANT CYBER MASTERS 2026");
        t1.setDescription("Tier 1 international championship featuring elite 5v5 rosters battling in single elimination.");
        t1.setGame("Valorant");
        t1.setMode(GameMode.SINGLE_ELIMINATION_TDM);
        t1.setStatus(TournamentStatus.IN_PROGRESS);
        t1.setTeamSize(5);
        t1.setMaxParticipants(8);
        t1.setOrganizerId(admin.getId());
        t1.setPublished(true);
        t1.setRegistrationStartDate("2026-10-01");
        t1.setRegistrationEndDate("2026-10-15");
        t1.setTournamentStartDate("2026-10-20");
        t1.getScoringConfig().setScoreToWin(13);
        tournaments.put(t1.getId(), t1);

        // Teams for Tournament 1
        String[][] teamData = {
                {"tm-1", "Sentinels", "SEN", tenz.getId(), "TenZ, zekken, Sacy, pancada, Marved"},
                {"tm-2", "Paper Rex", "PRX", "usr-prx", "f0rsakeN, Jinggg, d4v41, mindfreak, something"},
                {"tm-3", "Fnatic", "FNC", boaster.getId(), "Boaster, Derke, Alfajer, Chronicle, Leo"},
                {"tm-4", "Team Liquid", "TL", "usr-tl", "nAts, Jamppi, Keiko, Enzo, Mistic"},
                {"tm-5", "DRX", "DRX", "usr-drx", "stax, BuZz, MaKo, Foxy9, Flashback"},
                {"tm-6", "Gen.G Esports", "GEN", "usr-geng", "Meteor, t3xture, Lakia, Munchkin, Karon"},
                {"tm-7", "EDward Gaming", "EDG", "usr-edg", "ZmjjKK, CHICHOO, nobody, Haodong, Smoggy"},
                {"tm-8", "LOUD Esports", "LOUD", "usr-loud", "Saadhak, Less, tuyz, cauanzin, qck"}
        };

        for (int i = 0; i < teamData.length; i++) {
            String[] td = teamData[i];
            Team tm = new Team();
            tm.setId(td[0]);
            tm.setTournamentId(t1.getId());
            tm.setName(td[1]);
            tm.setTag(td[2]);
            tm.setCaptainId(td[3]);
            tm.setCaptainEmail(td[1].toLowerCase().replace(" ", "") + "@esports.gg");
            tm.setPlayerNames(Arrays.asList(td[4].split(", ")));
            tm.setSeed(i + 1);
            tm.setCheckedIn(true);
            tm.setRegistrationStatus(RegistrationStatus.APPROVED);
            teams.put(tm.getId(), tm);
        }

        // Bracket Matches for Tournament 1 (Quarterfinals -> Semifinals -> Grand Final)
        // Finals
        Match mFinal = new Match();
        mFinal.setId("match-val-final");
        mFinal.setTournamentId(t1.getId());
        mFinal.setRound(3);
        mFinal.setMatchNumber(7);
        mFinal.setRoundName("Grand Final");
        mFinal.setMode(GameMode.SINGLE_ELIMINATION_TDM);
        mFinal.setTeamAId("tm-1");
        mFinal.setTeamAName("Sentinels");
        mFinal.setTeamBId(null);
        mFinal.setTeamBName("TBD (Winner SF2)");
        mFinal.setStatus(MatchStatus.UPCOMING);
        mFinal.setScheduledTime("2026-10-27 20:00 IST");
        mFinal.setRoomId("NX-GF-990");
        mFinal.setRoomPassword("CHAMPION26");
        mFinal.setMapName("Lotus");
        matches.put(mFinal.getId(), mFinal);

        // Semifinal 1 (Sentinels won)
        Match mSF1 = new Match();
        mSF1.setId("match-val-sf1");
        mSF1.setTournamentId(t1.getId());
        mSF1.setRound(2);
        mSF1.setMatchNumber(5);
        mSF1.setRoundName("Semifinal 1");
        mSF1.setMode(GameMode.SINGLE_ELIMINATION_TDM);
        mSF1.setTeamAId("tm-1");
        mSF1.setTeamAName("Sentinels");
        mSF1.setTeamBId("tm-4");
        mSF1.setTeamBName("Team Liquid");
        mSF1.setScoreA(13);
        mSF1.setScoreB(9);
        mSF1.setWinnerId("tm-1");
        mSF1.setWinnerName("Sentinels");
        mSF1.setStatus(MatchStatus.COMPLETE);
        mSF1.setNextMatchId(mFinal.getId());
        mSF1.setNextMatchSlot("A");
        mSF1.setMapName("Ascent");
        matches.put(mSF1.getId(), mSF1);

        // Semifinal 2 (Paper Rex vs Fnatic - LIVE!)
        Match mSF2 = new Match();
        mSF2.setId("match-val-sf2");
        mSF2.setTournamentId(t1.getId());
        mSF2.setRound(2);
        mSF2.setMatchNumber(6);
        mSF2.setRoundName("Semifinal 2");
        mSF2.setMode(GameMode.SINGLE_ELIMINATION_TDM);
        mSF2.setTeamAId("tm-2");
        mSF2.setTeamAName("Paper Rex");
        mSF2.setTeamBId("tm-3");
        mSF2.setTeamBName("Fnatic");
        mSF2.setScoreA(11);
        mSF2.setScoreB(12);
        mSF2.setStatus(MatchStatus.LIVE);
        mSF2.setScheduledTime("2026-10-26 18:30 IST");
        mSF2.setRoomId("NX-SF-202");
        mSF2.setRoomPassword("PRXvFNC");
        mSF2.setNextMatchId(mFinal.getId());
        mSF2.setNextMatchSlot("B");
        mSF2.setMapName("Bind");
        matches.put(mSF2.getId(), mSF2);

        // Quarterfinals
        createQF(t1.getId(), "match-val-qf1", 1, "tm-1", "Sentinels", "tm-8", "LOUD Esports", 13, 7, "tm-1", "Sentinels", mSF1.getId(), "A", "Ascent");
        createQF(t1.getId(), "match-val-qf2", 2, "tm-4", "Team Liquid", "tm-5", "DRX", 13, 11, "tm-4", "Team Liquid", mSF1.getId(), "B", "Haven");
        createQF(t1.getId(), "match-val-qf3", 3, "tm-2", "Paper Rex", "tm-7", "EDward Gaming", 13, 8, "tm-2", "Paper Rex", mSF2.getId(), "A", "Split");
        createQF(t1.getId(), "match-val-qf4", 4, "tm-3", "Fnatic", "tm-6", "Gen.G Esports", 13, 10, "tm-3", "Fnatic", mSF2.getId(), "B", "Breeze");

        // Tournament 2: BATTLE ROYALE (Standings & Placement points)
        Tournament t2 = new Tournament();
        t2.setId("trn-br-pro");
        t2.setName("BGMI / PUBG PRO CLASH: INVITATIONAL");
        t2.setDescription("16-team Battle Royale lobby scoring with placement multipliers and kill bonuses.");
        t2.setGame("Battlegrounds Mobile");
        t2.setMode(GameMode.BATTLE_ROYALE);
        t2.setStatus(TournamentStatus.IN_PROGRESS);
        t2.setTeamSize(4);
        t2.setMaxParticipants(16);
        t2.setOrganizerId(admin.getId());
        t2.setPublished(true);
        t2.setTournamentStartDate("2026-10-25");
        tournaments.put(t2.getId(), t2);

        // Battle Royale Match 1 with full placement standings
        Match brMatch1 = new Match();
        brMatch1.setId("match-br-round1");
        brMatch1.setTournamentId(t2.getId());
        brMatch1.setRound(1);
        brMatch1.setMatchNumber(1);
        brMatch1.setRoundName("Round 1: Erangel");
        brMatch1.setMode(GameMode.BATTLE_ROYALE);
        brMatch1.setStatus(MatchStatus.COMPLETE);
        brMatch1.setMapName("Erangel");
        brMatch1.setRoomId("BR-LOBBY-01");
        brMatch1.setRoomPassword("CHICKENDINNER");

        List<BrTeamPlacement> placements = new ArrayList<>();
        placements.add(new BrTeamPlacement("tm-1", "Sentinels", 1, 12, 10, 12)); // 22 pts
        placements.add(new BrTeamPlacement("tm-3", "Fnatic", 2, 8, 6, 8));     // 14 pts
        placements.add(new BrTeamPlacement("tm-2", "Paper Rex", 3, 9, 5, 9));  // 14 pts
        placements.add(new BrTeamPlacement("tm-6", "Gen.G Esports", 4, 6, 4, 6)); // 10 pts
        placements.add(new BrTeamPlacement("tm-5", "DRX", 5, 4, 3, 4));        // 7 pts
        placements.add(new BrTeamPlacement("tm-4", "Team Liquid", 6, 3, 2, 3)); // 5 pts
        placements.add(new BrTeamPlacement("tm-7", "EDward Gaming", 7, 2, 1, 2)); // 3 pts
        placements.add(new BrTeamPlacement("tm-8", "LOUD Esports", 8, 1, 1, 1));  // 2 pts
        brMatch1.setBrPlacements(placements);
        matches.put(brMatch1.getId(), brMatch1);

        // Tournament 3: CLASH SQUAD BRAWL (Registration open)
        Tournament t3 = new Tournament();
        t3.setId("trn-clash-open");
        t3.setName("FREE FIRE: CLASH SQUAD APEX");
        t3.setDescription("Fast-paced 4v4 round-based tactical showdown. Best of 7 rounds.");
        t3.setGame("Free Fire");
        t3.setMode(GameMode.SINGLE_ELIMINATION_CLASH);
        t3.setStatus(TournamentStatus.REGISTRATION_OPEN);
        t3.setTeamSize(4);
        t3.setMaxParticipants(8);
        t3.setOrganizerId(admin.getId());
        t3.setPublished(true);
        t3.setTournamentStartDate("2026-11-05");
        tournaments.put(t3.getId(), t3);

        // Sample Dispute
        Dispute disp = new Dispute();
        disp.setId("disp-101");
        disp.setTournamentId(t1.getId());
        disp.setMatchId("match-val-qf2");
        disp.setSubmittedByUserId(tenz.getId());
        disp.setSubmittedByTeamName("Team Liquid");
        disp.setReason("Opponent ping spike and alleged unapproved tactical timeout in Round 21.");
        disp.setEvidenceUrl("https://youtube.com/watch?v=demo-clip-evidence");
        disp.setStatus(DisputeStatus.UNDER_REVIEW);
        disp.setReviewerNotes("Reviewing tournament spectator VOD logs for ping spikes.");
        disputes.put(disp.getId(), disp);

        // Sample Conduct Report
        ConductReport cr = new ConductReport();
        cr.setId("rep-201");
        cr.setTournamentId(t1.getId());
        cr.setMatchId("match-val-qf1");
        cr.setReportedUserId("usr-loud");
        cr.setReportedPlayerName("qck");
        cr.setReportedTeamName("LOUD Esports");
        cr.setSubmittedByUserId(tenz.getId());
        cr.setSubmittedByName("TenZ");
        cr.setCategory(ConductCategory.TOXIC_BEHAVIOR);
        cr.setDescription("In-game all-chat unsportsmanlike taunting post-match.");
        cr.setStatus(ReportStatus.SUBMITTED);
        cr.setModerationAction(ModerationAction.NONE);
        conductReports.put(cr.getId(), cr);

        // Notifications
        notifications.put("notif-1", new Notification("notif-1", admin.getId(), "Pending Dispute", "New result dispute submitted for Match QF2 (Team Liquid).", "DISPUTE_UPDATE"));
        notifications.put("notif-2", new Notification("notif-2", tenz.getId(), "Match Ready", "Your next match Grand Final is scheduled for Lotus at 20:00 IST.", "MATCH_READY"));

        // Audit Logs
        auditLogs.add(new AuditLog("aud-1", "ApexAdmin", "TOURNAMENT_CREATED", "TOURNAMENT", t1.getId(), "Initial tournament setup"));
        auditLogs.add(new AuditLog("aud-2", "ApexAdmin", "BRACKET_GENERATED", "TOURNAMENT", t1.getId(), "Single-elimination 8-team tree seeded"));
        auditLogs.add(new AuditLog("aud-3", "ApexAdmin", "SCORE_RECORDED", "MATCH", "match-val-sf1", "Confirmed score 13-9 for Sentinels"));
    }

    private void createQF(String tId, String id, int matchNum, String tAId, String tAName, String tBId, String tBName,
                          int sA, int sB, String wId, String wName, String nextId, String nextSlot, String map) {
        Match m = new Match();
        m.setId(id);
        m.setTournamentId(tId);
        m.setRound(1);
        m.setMatchNumber(matchNum);
        m.setRoundName("Quarterfinal " + matchNum);
        m.setMode(GameMode.SINGLE_ELIMINATION_TDM);
        m.setTeamAId(tAId);
        m.setTeamAName(tAName);
        m.setTeamBId(tBId);
        m.setTeamBName(tBName);
        m.setScoreA(sA);
        m.setScoreB(sB);
        m.setWinnerId(wId);
        m.setWinnerName(wName);
        m.setStatus(MatchStatus.COMPLETE);
        m.setNextMatchId(nextId);
        m.setNextMatchSlot(nextSlot);
        m.setMapName(map);
        matches.put(m.getId(), m);
    }

    @Override
    public Optional<User> findUserById(String id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Optional<User> findUserByUsername(String username) {
        if (username == null) return Optional.empty();
        return users.values().stream()
                .filter(u -> username.equalsIgnoreCase(u.getUsername()))
                .findFirst();
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        if (email == null) return Optional.empty();
        return users.values().stream()
                .filter(u -> email.equalsIgnoreCase(u.getEmail()))
                .findFirst();
    }

    @Override
    public User saveUser(User user) {
        if (user.getId() == null) {
            user.setId("usr-" + UUID.randomUUID().toString().substring(0, 8));
        }
        users.put(user.getId(), user);
        persistCollection("users.json", users);
        return user;
    }

    @Override
    public List<User> findAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<Tournament> findTournamentById(String id) {
        return Optional.ofNullable(tournaments.get(id));
    }

    @Override
    public Tournament saveTournament(Tournament tournament) {
        if (tournament.getId() == null) {
            tournament.setId("trn-" + UUID.randomUUID().toString().substring(0, 8));
        }
        tournaments.put(tournament.getId(), tournament);
        persistCollection("tournaments.json", tournaments);
        return tournament;
    }

    @Override
    public void deleteTournament(String id) {
        tournaments.remove(id);
        persistCollection("tournaments.json", tournaments);
    }

    @Override
    public List<Tournament> findAllTournaments() {
        return new ArrayList<>(tournaments.values());
    }

    @Override
    public Optional<Team> findTeamById(String id) {
        return Optional.ofNullable(teams.get(id));
    }

    @Override
    public List<Team> findTeamsByTournamentId(String tournamentId) {
        List<Team> result = new ArrayList<>();
        for (Team t : teams.values()) {
            if (tournamentId.equals(t.getTournamentId())) {
                result.add(t);
            }
        }
        return result;
    }

    @Override
    public Team saveTeam(Team team) {
        if (team.getId() == null) {
            team.setId("tm-" + UUID.randomUUID().toString().substring(0, 8));
        }
        teams.put(team.getId(), team);
        persistCollection("teams.json", teams);
        return team;
    }

    @Override
    public void deleteTeam(String id) {
        teams.remove(id);
        persistCollection("teams.json", teams);
    }

    @Override
    public List<Team> findAllTeams() {
        return new ArrayList<>(teams.values());
    }

    @Override
    public Optional<Match> findMatchById(String id) {
        return Optional.ofNullable(matches.get(id));
    }

    @Override
    public List<Match> findMatchesByTournamentId(String tournamentId) {
        List<Match> result = new ArrayList<>();
        for (Match m : matches.values()) {
            if (tournamentId.equals(m.getTournamentId())) {
                result.add(m);
            }
        }
        result.sort(Comparator.comparingInt(Match::getRound).thenComparingInt(Match::getMatchNumber));
        return result;
    }

    @Override
    public Match saveMatch(Match match) {
        if (match.getId() == null) {
            match.setId("match-" + UUID.randomUUID().toString().substring(0, 8));
        }
        match.setUpdatedAt(Instant.now().toString());
        matches.put(match.getId(), match);
        persistCollection("matches.json", matches);
        return match;
    }

    @Override
    public List<Match> saveAllMatches(List<Match> matchList) {
        for (Match m : matchList) {
            if (m.getId() == null) {
                m.setId("match-" + UUID.randomUUID().toString().substring(0, 8));
            }
            m.setUpdatedAt(Instant.now().toString());
            matches.put(m.getId(), m);
        }
        persistCollection("matches.json", matches);
        return matchList;
    }

    @Override
    public void deleteMatchesByTournamentId(String tournamentId) {
        matches.values().removeIf(m -> tournamentId.equals(m.getTournamentId()));
        persistCollection("matches.json", matches);
    }

    @Override
    public List<Match> findAllMatches() {
        return new ArrayList<>(matches.values());
    }

    @Override
    public Optional<Dispute> findDisputeById(String id) {
        return Optional.ofNullable(disputes.get(id));
    }

    @Override
    public List<Dispute> findDisputesByTournamentId(String tournamentId) {
        List<Dispute> result = new ArrayList<>();
        for (Dispute d : disputes.values()) {
            if (tournamentId.equals(d.getTournamentId())) {
                result.add(d);
            }
        }
        return result;
    }

    @Override
    public Dispute saveDispute(Dispute dispute) {
        if (dispute.getId() == null) {
            dispute.setId("disp-" + UUID.randomUUID().toString().substring(0, 8));
        }
        disputes.put(dispute.getId(), dispute);
        persistCollection("disputes.json", disputes);
        return dispute;
    }

    @Override
    public List<Dispute> findAllDisputes() {
        return new ArrayList<>(disputes.values());
    }

    @Override
    public Optional<ConductReport> findConductReportById(String id) {
        return Optional.ofNullable(conductReports.get(id));
    }

    @Override
    public List<ConductReport> findConductReportsByTournamentId(String tournamentId) {
        List<ConductReport> result = new ArrayList<>();
        for (ConductReport r : conductReports.values()) {
            if (tournamentId.equals(r.getTournamentId())) {
                result.add(r);
            }
        }
        return result;
    }

    @Override
    public ConductReport saveConductReport(ConductReport report) {
        if (report.getId() == null) {
            report.setId("rep-" + UUID.randomUUID().toString().substring(0, 8));
        }
        conductReports.put(report.getId(), report);
        persistCollection("conduct_reports.json", conductReports);
        return report;
    }

    @Override
    public List<ConductReport> findAllConductReports() {
        return new ArrayList<>(conductReports.values());
    }

    @Override
    public Optional<Notification> findNotificationById(String id) {
        return Optional.ofNullable(notifications.get(id));
    }

    @Override
    public List<Notification> findNotificationsByRecipient(String userId) {
        List<Notification> result = new ArrayList<>();
        for (Notification n : notifications.values()) {
            if (userId.equals(n.getRecipientUserId())) {
                result.add(n);
            }
        }
        result.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return result;
    }

    @Override
    public Notification saveNotification(Notification notification) {
        if (notification.getId() == null) {
            notification.setId("notif-" + UUID.randomUUID().toString().substring(0, 8));
        }
        notifications.put(notification.getId(), notification);
        persistCollection("notifications.json", notifications);
        return notification;
    }

    @Override
    public List<Notification> findAllNotifications() {
        return new ArrayList<>(notifications.values());
    }

    @Override
    public AuditLog saveAuditLog(AuditLog logItem) {
        if (logItem.getId() == null) {
            logItem.setId("aud-" + UUID.randomUUID().toString().substring(0, 8));
        }
        auditLogs.add(0, logItem);
        persistCollection("audit_logs.json", auditLogs);
        return logItem;
    }

    @Override
    public List<AuditLog> findAllAuditLogs() {
        return new ArrayList<>(auditLogs);
    }

    @Override
    public List<AuditLog> findAuditLogsByEntity(String entityType, String entityId) {
        List<AuditLog> result = new ArrayList<>();
        for (AuditLog al : auditLogs) {
            if (entityType.equalsIgnoreCase(al.getEntityType()) && entityId.equalsIgnoreCase(al.getEntityId())) {
                result.add(al);
            }
        }
        return result;
    }
}
