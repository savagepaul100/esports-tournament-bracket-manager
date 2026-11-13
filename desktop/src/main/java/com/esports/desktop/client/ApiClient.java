package com.esports.desktop.client;

import com.esports.desktop.model.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Facade over FirebaseClient.
 * All calls go directly to Cloud Firestore — no local backend required.
 */
public class ApiClient {

    private FirebaseClient firebase;
    private UserDto currentUser;
    private String connectionError;

    public ApiClient() {
        // Set up a default organizer user; replaced after login
        this.currentUser = new UserDto();
        this.currentUser.setId("usr-admin");
        this.currentUser.setUsername("admin");
        this.currentUser.setRole("ORGANIZER");
        this.currentUser.setGamerTag("ApexAdmin#0001");

        try {
            this.firebase = FirebaseClient.getInstance();
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

    /** Kept for compatibility — always returns true if Firebase init succeeded */
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

    // ==================== TOURNAMENTS ====================

    public List<TournamentDto> getTournaments() {
        if (firebase == null) return Collections.emptyList();
        return firebase.getTournaments();
    }

    public TournamentDto createTournament(TournamentDto tournament) {
        if (firebase == null) return null;
        return firebase.createTournament(tournament, currentUser.getUsername());
    }

    public boolean updateTournamentStatus(String tournamentId, String status) {
        if (firebase == null) return false;
        return firebase.updateTournamentStatus(tournamentId, status, currentUser.getUsername());
    }

    public boolean deleteTournament(String tournamentId) {
        if (firebase == null) return false;
        return firebase.deleteTournament(tournamentId, currentUser.getUsername());
    }

    // ==================== TEAMS ====================

    public List<TeamDto> getTeams(String tournamentId) {
        if (firebase == null) return Collections.emptyList();
        return firebase.getTeams(tournamentId);
    }

    public boolean registerTeam(TeamDto team) {
        if (firebase == null) return false;
        return firebase.registerTeam(team, currentUser.getUsername());
    }

    public boolean updateTeamStatus(String teamId, String status, String reason) {
        if (firebase == null) return false;
        return firebase.updateTeamStatus(teamId, status, reason, currentUser.getUsername());
    }

    public boolean setTeamCheckIn(String teamId, boolean checkedIn) {
        if (firebase == null) return false;
        return firebase.setTeamCheckIn(teamId, checkedIn, currentUser.getUsername());
    }

    // ==================== MATCHES ====================

    public List<MatchDto> getMatches(String tournamentId) {
        if (firebase == null) return Collections.emptyList();
        return firebase.getMatches(tournamentId);
    }

    public List<MatchDto> generateBracket(String tournamentId) {
        if (firebase == null) return Collections.emptyList();
        return firebase.generateBracket(tournamentId, currentUser.getUsername());
    }

    public boolean updateMatchSchedule(String matchId, String time, String roomId, String password, String map) {
        if (firebase == null) return false;
        return firebase.updateMatchSchedule(matchId, time, roomId, password, map, currentUser.getUsername());
    }

    public boolean setMatchLive(String matchId, int scoreA, int scoreB) {
        if (firebase == null) return false;
        return firebase.setMatchLive(matchId, scoreA, scoreB, currentUser.getUsername());
    }

    public boolean updateLiveScore(String matchId, int scoreA, int scoreB) {
        if (firebase == null) return false;
        return firebase.updateLiveScore(matchId, scoreA, scoreB, currentUser.getUsername());
    }

    public boolean recordMatchScore(String matchId, int scoreA, int scoreB, String reason) {
        if (firebase == null) return false;
        return firebase.recordMatchScore(matchId, scoreA, scoreB, reason, currentUser.getUsername());
    }

    public boolean updateMatchDetails(String matchId, Map<String, Object> updates) {
        if (firebase == null) return false;
        return firebase.updateMatchDetails(matchId, updates, currentUser.getUsername());
    }

    // ==================== DISPUTES ====================

    public List<DisputeDto> getDisputes(String tournamentId) {
        if (firebase == null) return Collections.emptyList();
        return firebase.getDisputes(tournamentId);
    }

    public boolean createDispute(DisputeDto dispute) {
        if (firebase == null) return false;
        return firebase.createDispute(dispute, currentUser.getUsername());
    }

    public boolean resolveDispute(String disputeId, String decision, String notes) {
        if (firebase == null) return false;
        return firebase.resolveDispute(disputeId, decision, notes, currentUser.getUsername());
    }

    // ==================== CONDUCT REPORTS ====================

    public List<ConductReportDto> getConductReports(String tournamentId) {
        if (firebase == null) return Collections.emptyList();
        return firebase.getConductReports(tournamentId);
    }

    public boolean resolveConductReport(String reportId, String action, String reason) {
        if (firebase == null) return false;
        return firebase.resolveConductReport(reportId, action, reason, currentUser.getUsername());
    }

    // ==================== AUDIT LOGS ====================

    public List<AuditLogDto> getAuditLogs() {
        if (firebase == null) return Collections.emptyList();
        return firebase.getAuditLogs();
    }

    // ==================== SEEDER ====================

    public boolean seedPrototypeData() {
        if (firebase == null) return false;
        return firebase.seedPrototypeData(currentUser.getUsername());
    }
}
