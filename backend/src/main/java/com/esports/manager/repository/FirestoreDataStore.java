package com.esports.manager.repository;

import com.esports.manager.model.*;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Cloud Firestore Implementation of DataStore.
 * Synchronizes real-time tournament operations directly with Firebase Cloud Firestore.
 */
public class FirestoreDataStore implements DataStore {
    private static final Logger log = LoggerFactory.getLogger(FirestoreDataStore.class);
    private final Firestore firestore;

    public FirestoreDataStore(Firestore firestore) {
        this.firestore = firestore;
    }

    // --- USERS ---
    @Override
    public Optional<User> findUserById(String id) {
        try {
            DocumentSnapshot snap = firestore.collection("users").document(id).get().get();
            if (snap.exists()) {
                return Optional.ofNullable(snap.toObject(User.class));
            }
        } catch (Exception e) {
            log.error("Firestore error finding user by id: {}", id, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findUserByUsername(String username) {
        try {
            QuerySnapshot snap = firestore.collection("users").whereEqualTo("username", username).limit(1).get().get();
            if (!snap.isEmpty()) {
                return Optional.ofNullable(snap.getDocuments().get(0).toObject(User.class));
            }
        } catch (Exception e) {
            log.error("Firestore error finding user by username: {}", username, e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        try {
            QuerySnapshot snap = firestore.collection("users").whereEqualTo("email", email).limit(1).get().get();
            if (!snap.isEmpty()) {
                return Optional.ofNullable(snap.getDocuments().get(0).toObject(User.class));
            }
        } catch (Exception e) {
            log.error("Firestore error finding user by email: {}", email, e);
        }
        return Optional.empty();
    }

    @Override
    public User saveUser(User user) {
        if (user.getId() == null) {
            user.setId("usr-" + UUID.randomUUID().toString().substring(0, 8));
        }
        firestore.collection("users").document(user.getId()).set(user);
        return user;
    }

    @Override
    public List<User> findAllUsers() {
        List<User> list = new ArrayList<>();
        try {
            for (DocumentSnapshot doc : firestore.collection("users").get().get().getDocuments()) {
                User u = doc.toObject(User.class);
                if (u != null) list.add(u);
            }
        } catch (Exception e) {
            log.error("Firestore error findAllUsers", e);
        }
        return list;
    }

    // --- TOURNAMENTS ---
    @Override
    public Optional<Tournament> findTournamentById(String id) {
        try {
            DocumentSnapshot snap = firestore.collection("tournaments").document(id).get().get();
            if (snap.exists()) {
                return Optional.ofNullable(snap.toObject(Tournament.class));
            }
        } catch (Exception e) {
            log.error("Firestore error findTournamentById: {}", id, e);
        }
        return Optional.empty();
    }

    @Override
    public Tournament saveTournament(Tournament tournament) {
        if (tournament.getId() == null) {
            tournament.setId("trn-" + UUID.randomUUID().toString().substring(0, 8));
        }
        firestore.collection("tournaments").document(tournament.getId()).set(tournament);
        return tournament;
    }

    @Override
    public void deleteTournament(String id) {
        firestore.collection("tournaments").document(id).delete();
    }

    @Override
    public List<Tournament> findAllTournaments() {
        List<Tournament> list = new ArrayList<>();
        try {
            for (DocumentSnapshot doc : firestore.collection("tournaments").get().get().getDocuments()) {
                Tournament t = doc.toObject(Tournament.class);
                if (t != null) list.add(t);
            }
        } catch (Exception e) {
            log.error("Firestore error findAllTournaments", e);
        }
        return list;
    }

    // --- TEAMS ---
    @Override
    public Optional<Team> findTeamById(String id) {
        try {
            DocumentSnapshot snap = firestore.collection("teams").document(id).get().get();
            if (snap.exists()) {
                return Optional.ofNullable(snap.toObject(Team.class));
            }
        } catch (Exception e) {
            log.error("Firestore error findTeamById: {}", id, e);
        }
        return Optional.empty();
    }

    @Override
    public List<Team> findTeamsByTournamentId(String tournamentId) {
        List<Team> list = new ArrayList<>();
        try {
            QuerySnapshot snap = firestore.collection("teams").whereEqualTo("tournamentId", tournamentId).get().get();
            for (DocumentSnapshot doc : snap.getDocuments()) {
                Team t = doc.toObject(Team.class);
                if (t != null) list.add(t);
            }
        } catch (Exception e) {
            log.error("Firestore error findTeamsByTournamentId", e);
        }
        return list;
    }

    @Override
    public Team saveTeam(Team team) {
        if (team.getId() == null) {
            team.setId("tm-" + UUID.randomUUID().toString().substring(0, 8));
        }
        firestore.collection("teams").document(team.getId()).set(team);
        return team;
    }

    @Override
    public void deleteTeam(String id) {
        firestore.collection("teams").document(id).delete();
    }

    @Override
    public List<Team> findAllTeams() {
        List<Team> list = new ArrayList<>();
        try {
            for (DocumentSnapshot doc : firestore.collection("teams").get().get().getDocuments()) {
                Team t = doc.toObject(Team.class);
                if (t != null) list.add(t);
            }
        } catch (Exception e) {
            log.error("Firestore error findAllTeams", e);
        }
        return list;
    }

    // --- MATCHES ---
    @Override
    public Optional<Match> findMatchById(String id) {
        try {
            DocumentSnapshot snap = firestore.collection("matches").document(id).get().get();
            if (snap.exists()) {
                return Optional.ofNullable(snap.toObject(Match.class));
            }
        } catch (Exception e) {
            log.error("Firestore error findMatchById", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Match> findMatchesByTournamentId(String tournamentId) {
        List<Match> list = new ArrayList<>();
        try {
            QuerySnapshot snap = firestore.collection("matches").whereEqualTo("tournamentId", tournamentId).get().get();
            for (DocumentSnapshot doc : snap.getDocuments()) {
                Match m = doc.toObject(Match.class);
                if (m != null) list.add(m);
            }
            list.sort(Comparator.comparingInt(Match::getRound).thenComparingInt(Match::getMatchNumber));
        } catch (Exception e) {
            log.error("Firestore error findMatchesByTournamentId", e);
        }
        return list;
    }

    @Override
    public Match saveMatch(Match match) {
        if (match.getId() == null) {
            match.setId("match-" + UUID.randomUUID().toString().substring(0, 8));
        }
        firestore.collection("matches").document(match.getId()).set(match);
        return match;
    }

    @Override
    public List<Match> saveAllMatches(List<Match> matches) {
        WriteBatch batch = firestore.batch();
        for (Match m : matches) {
            if (m.getId() == null) {
                m.setId("match-" + UUID.randomUUID().toString().substring(0, 8));
            }
            batch.set(firestore.collection("matches").document(m.getId()), m);
        }
        try {
            batch.commit().get();
        } catch (Exception e) {
            log.error("Firestore error batch saving matches", e);
        }
        return matches;
    }

    @Override
    public void deleteMatchesByTournamentId(String tournamentId) {
        try {
            QuerySnapshot snap = firestore.collection("matches").whereEqualTo("tournamentId", tournamentId).get().get();
            WriteBatch batch = firestore.batch();
            for (DocumentSnapshot doc : snap.getDocuments()) {
                batch.delete(doc.getReference());
            }
            batch.commit().get();
        } catch (Exception e) {
            log.error("Firestore error deleteMatchesByTournamentId", e);
        }
    }

    @Override
    public List<Match> findAllMatches() {
        List<Match> list = new ArrayList<>();
        try {
            for (DocumentSnapshot doc : firestore.collection("matches").get().get().getDocuments()) {
                Match m = doc.toObject(Match.class);
                if (m != null) list.add(m);
            }
        } catch (Exception e) {
            log.error("Firestore error findAllMatches", e);
        }
        return list;
    }

    // --- DISPUTES ---
    @Override
    public Optional<Dispute> findDisputeById(String id) {
        try {
            DocumentSnapshot snap = firestore.collection("disputes").document(id).get().get();
            if (snap.exists()) {
                return Optional.ofNullable(snap.toObject(Dispute.class));
            }
        } catch (Exception e) {
            log.error("Firestore error findDisputeById", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Dispute> findDisputesByTournamentId(String tournamentId) {
        List<Dispute> list = new ArrayList<>();
        try {
            QuerySnapshot snap = firestore.collection("disputes").whereEqualTo("tournamentId", tournamentId).get().get();
            for (DocumentSnapshot doc : snap.getDocuments()) {
                Dispute d = doc.toObject(Dispute.class);
                if (d != null) list.add(d);
            }
        } catch (Exception e) {
            log.error("Firestore error findDisputesByTournamentId", e);
        }
        return list;
    }

    @Override
    public Dispute saveDispute(Dispute dispute) {
        if (dispute.getId() == null) {
            dispute.setId("disp-" + UUID.randomUUID().toString().substring(0, 8));
        }
        firestore.collection("disputes").document(dispute.getId()).set(dispute);
        return dispute;
    }

    @Override
    public List<Dispute> findAllDisputes() {
        List<Dispute> list = new ArrayList<>();
        try {
            for (DocumentSnapshot doc : firestore.collection("disputes").get().get().getDocuments()) {
                Dispute d = doc.toObject(Dispute.class);
                if (d != null) list.add(d);
            }
        } catch (Exception e) {
            log.error("Firestore error findAllDisputes", e);
        }
        return list;
    }

    // --- CONDUCT REPORTS ---
    @Override
    public Optional<ConductReport> findConductReportById(String id) {
        try {
            DocumentSnapshot snap = firestore.collection("conduct_reports").document(id).get().get();
            if (snap.exists()) {
                return Optional.ofNullable(snap.toObject(ConductReport.class));
            }
        } catch (Exception e) {
            log.error("Firestore error findConductReportById", e);
        }
        return Optional.empty();
    }

    @Override
    public List<ConductReport> findConductReportsByTournamentId(String tournamentId) {
        List<ConductReport> list = new ArrayList<>();
        try {
            QuerySnapshot snap = firestore.collection("conduct_reports").whereEqualTo("tournamentId", tournamentId).get().get();
            for (DocumentSnapshot doc : snap.getDocuments()) {
                ConductReport r = doc.toObject(ConductReport.class);
                if (r != null) list.add(r);
            }
        } catch (Exception e) {
            log.error("Firestore error findConductReportsByTournamentId", e);
        }
        return list;
    }

    @Override
    public ConductReport saveConductReport(ConductReport report) {
        if (report.getId() == null) {
            report.setId("rep-" + UUID.randomUUID().toString().substring(0, 8));
        }
        firestore.collection("conduct_reports").document(report.getId()).set(report);
        return report;
    }

    @Override
    public List<ConductReport> findAllConductReports() {
        List<ConductReport> list = new ArrayList<>();
        try {
            for (DocumentSnapshot doc : firestore.collection("conduct_reports").get().get().getDocuments()) {
                ConductReport r = doc.toObject(ConductReport.class);
                if (r != null) list.add(r);
            }
        } catch (Exception e) {
            log.error("Firestore error findAllConductReports", e);
        }
        return list;
    }

    // --- NOTIFICATIONS ---
    @Override
    public Optional<Notification> findNotificationById(String id) {
        try {
            DocumentSnapshot snap = firestore.collection("notifications").document(id).get().get();
            if (snap.exists()) {
                return Optional.ofNullable(snap.toObject(Notification.class));
            }
        } catch (Exception e) {
            log.error("Firestore error findNotificationById", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Notification> findNotificationsByRecipient(String userId) {
        List<Notification> list = new ArrayList<>();
        try {
            QuerySnapshot snap = firestore.collection("notifications").whereEqualTo("recipientUserId", userId).get().get();
            for (DocumentSnapshot doc : snap.getDocuments()) {
                Notification n = doc.toObject(Notification.class);
                if (n != null) list.add(n);
            }
            list.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        } catch (Exception e) {
            log.error("Firestore error findNotificationsByRecipient", e);
        }
        return list;
    }

    @Override
    public Notification saveNotification(Notification notification) {
        if (notification.getId() == null) {
            notification.setId("notif-" + UUID.randomUUID().toString().substring(0, 8));
        }
        firestore.collection("notifications").document(notification.getId()).set(notification);
        return notification;
    }

    @Override
    public List<Notification> findAllNotifications() {
        List<Notification> list = new ArrayList<>();
        try {
            for (DocumentSnapshot doc : firestore.collection("notifications").get().get().getDocuments()) {
                Notification n = doc.toObject(Notification.class);
                if (n != null) list.add(n);
            }
        } catch (Exception e) {
            log.error("Firestore error findAllNotifications", e);
        }
        return list;
    }

    // --- AUDIT LOGS ---
    @Override
    public AuditLog saveAuditLog(AuditLog logItem) {
        if (logItem.getId() == null) {
            logItem.setId("aud-" + UUID.randomUUID().toString().substring(0, 8));
        }
        firestore.collection("audit_logs").document(logItem.getId()).set(logItem);
        return logItem;
    }

    @Override
    public List<AuditLog> findAllAuditLogs() {
        List<AuditLog> list = new ArrayList<>();
        try {
            QuerySnapshot snap = firestore.collection("audit_logs").orderBy("timestamp", Query.Direction.DESCENDING).get().get();
            for (DocumentSnapshot doc : snap.getDocuments()) {
                AuditLog a = doc.toObject(AuditLog.class);
                if (a != null) list.add(a);
            }
        } catch (Exception e) {
            log.error("Firestore error findAllAuditLogs", e);
        }
        return list;
    }

    @Override
    public List<AuditLog> findAuditLogsByEntity(String entityType, String entityId) {
        List<AuditLog> list = new ArrayList<>();
        try {
            QuerySnapshot snap = firestore.collection("audit_logs")
                    .whereEqualTo("entityType", entityType)
                    .whereEqualTo("entityId", entityId)
                    .get().get();
            for (DocumentSnapshot doc : snap.getDocuments()) {
                AuditLog a = doc.toObject(AuditLog.class);
                if (a != null) list.add(a);
            }
        } catch (Exception e) {
            log.error("Firestore error findAuditLogsByEntity", e);
        }
        return list;
    }

    public void init() {
        try {
            if (findAllTournaments().isEmpty()) {
                log.info("🔥 Cloud Firestore tournaments collection is empty. Seeding rich initial tournament data...");
                seedDemoData();
                log.info("🔥 Successfully seeded Cloud Firestore with esports tournaments, matches, teams, and users.");
            }
        } catch (Exception e) {
            log.error("Failed to seed demo data into Cloud Firestore: {}", e.getMessage(), e);
        }
    }

    private void seedDemoData() {
        // Users
        User admin = new User("usr-admin", "admin@apexarena.gg", "admin", "admin123", Role.ORGANIZER, "ApexAdmin#0001");
        User tenz = new User("usr-tenz", "tenz@sentinels.gg", "tenz", "tenz123", Role.PARTICIPANT, "TenZ#SEN");
        User boaster = new User("usr-boaster", "boaster@fnatic.com", "boaster", "boaster123", Role.PARTICIPANT, "Boaster#FNC");
        User spectator = new User("usr-fan", "fan@gmail.com", "spectator", "fan123", Role.VIEWER, "HypeViewer#9999");
        saveUser(admin);
        saveUser(tenz);
        saveUser(boaster);
        saveUser(spectator);

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
        saveTournament(t1);

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
            saveTeam(tm);
        }

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
        saveMatch(mFinal);

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
        saveMatch(mSF1);

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
        saveMatch(mSF2);

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
        saveTournament(t2);

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
        placements.add(new BrTeamPlacement("tm-1", "Sentinels", 1, 12, 10, 12));
        placements.add(new BrTeamPlacement("tm-3", "Fnatic", 2, 8, 6, 8));
        placements.add(new BrTeamPlacement("tm-2", "Paper Rex", 3, 9, 5, 9));
        placements.add(new BrTeamPlacement("tm-6", "Gen.G Esports", 4, 6, 4, 6));
        placements.add(new BrTeamPlacement("tm-5", "DRX", 5, 4, 3, 4));
        placements.add(new BrTeamPlacement("tm-4", "Team Liquid", 6, 3, 2, 3));
        placements.add(new BrTeamPlacement("tm-7", "EDward Gaming", 7, 2, 1, 2));
        placements.add(new BrTeamPlacement("tm-8", "LOUD Esports", 8, 1, 1, 1));
        brMatch1.setBrPlacements(placements);
        saveMatch(brMatch1);

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
        saveTournament(t3);

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
        saveDispute(disp);

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
        saveConductReport(cr);

        // Notifications
        saveNotification(new Notification("notif-1", admin.getId(), "Pending Dispute", "New result dispute submitted for Match QF2 (Team Liquid).", "DISPUTE_UPDATE"));
        saveNotification(new Notification("notif-2", tenz.getId(), "Match Ready", "Your next match Grand Final is scheduled for Lotus at 20:00 IST.", "MATCH_READY"));

        // Audit Logs
        saveAuditLog(new AuditLog("aud-1", "ApexAdmin", "TOURNAMENT_CREATED", "TOURNAMENT", t1.getId(), "Initial tournament setup"));
        saveAuditLog(new AuditLog("aud-2", "ApexAdmin", "BRACKET_GENERATED", "TOURNAMENT", t1.getId(), "Single-elimination 8-team tree seeded"));
        saveAuditLog(new AuditLog("aud-3", "ApexAdmin", "SCORE_RECORDED", "MATCH", "match-val-sf1", "Confirmed score 13-9 for Sentinels"));
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
        saveMatch(m);
    }
}
