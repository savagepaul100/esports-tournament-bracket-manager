package com.esports.manager.repository;

import com.esports.manager.model.*;
import java.util.List;
import java.util.Optional;

public interface DataStore {
    // Users
    Optional<User> findUserById(String id);
    Optional<User> findUserByUsername(String username);
    Optional<User> findUserByEmail(String email);
    User saveUser(User user);
    List<User> findAllUsers();

    // Tournaments
    Optional<Tournament> findTournamentById(String id);
    Tournament saveTournament(Tournament tournament);
    void deleteTournament(String id);
    List<Tournament> findAllTournaments();

    // Teams
    Optional<Team> findTeamById(String id);
    List<Team> findTeamsByTournamentId(String tournamentId);
    Team saveTeam(Team team);
    void deleteTeam(String id);
    List<Team> findAllTeams();

    // Matches
    Optional<Match> findMatchById(String id);
    List<Match> findMatchesByTournamentId(String tournamentId);
    Match saveMatch(Match match);
    List<Match> saveAllMatches(List<Match> matches);
    void deleteMatchesByTournamentId(String tournamentId);
    List<Match> findAllMatches();

    // Disputes
    Optional<Dispute> findDisputeById(String id);
    List<Dispute> findDisputesByTournamentId(String tournamentId);
    Dispute saveDispute(Dispute dispute);
    List<Dispute> findAllDisputes();

    // Conduct Reports
    Optional<ConductReport> findConductReportById(String id);
    List<ConductReport> findConductReportsByTournamentId(String tournamentId);
    ConductReport saveConductReport(ConductReport report);
    List<ConductReport> findAllConductReports();

    // Notifications
    Optional<Notification> findNotificationById(String id);
    List<Notification> findNotificationsByRecipient(String userId);
    Notification saveNotification(Notification notification);
    List<Notification> findAllNotifications();

    // Audit Logs
    AuditLog saveAuditLog(AuditLog log);
    List<AuditLog> findAllAuditLogs();
    List<AuditLog> findAuditLogsByEntity(String entityType, String entityId);
}
