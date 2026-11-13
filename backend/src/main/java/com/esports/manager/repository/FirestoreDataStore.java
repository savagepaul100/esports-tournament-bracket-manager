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
}
