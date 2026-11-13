package com.esports.manager.service;

import com.esports.manager.model.*;
import com.esports.manager.repository.DataStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TournamentService {

    private final DataStore dataStore;

    @Autowired
    public TournamentService(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    // ==========================================
    // TOURNAMENT LIFECYCLE
    // ==========================================

    public List<Tournament> getAllTournaments() {
        return dataStore.findAllTournaments();
    }

    public List<Tournament> getPublishedTournaments() {
        return dataStore.findAllTournaments().stream()
                .filter(Tournament::isPublished)
                .collect(Collectors.toList());
    }

    public Optional<Tournament> getTournamentById(String id) {
        return dataStore.findTournamentById(id);
    }

    public Tournament createTournament(Tournament tournament, String organizerId) {
        if (tournament.getId() == null || tournament.getId().isBlank()) {
            tournament.setId("trn-" + UUID.randomUUID().toString().substring(0, 8));
        }
        tournament.setOrganizerId(organizerId);
        tournament.setStatus(TournamentStatus.DRAFT);
        tournament.setCreatedAt(Instant.now().toString());

        Tournament saved = dataStore.saveTournament(tournament);
        dataStore.saveAuditLog(new AuditLog(null, organizerId, "TOURNAMENT_CREATED", "TOURNAMENT", saved.getId(), "Created tournament: " + saved.getName()));
        return saved;
    }

    public Tournament updateTournament(String id, Tournament updated, String actor) {
        Tournament existing = dataStore.findTournamentById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found: " + id));

        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setGame(updated.getGame());
        existing.setMode(updated.getMode());
        existing.setRegistrationStartDate(updated.getRegistrationStartDate());
        existing.setRegistrationEndDate(updated.getRegistrationEndDate());
        existing.setTournamentStartDate(updated.getTournamentStartDate());
        existing.setTimeZone(updated.getTimeZone());
        existing.setTeamSize(updated.getTeamSize());
        existing.setMaxParticipants(updated.getMaxParticipants());
        existing.setRules(updated.getRules());
        existing.setScoringConfig(updated.getScoringConfig());
        existing.setBannerUrl(updated.getBannerUrl());

        Tournament saved = dataStore.saveTournament(existing);
        dataStore.saveAuditLog(new AuditLog(null, actor, "TOURNAMENT_UPDATED", "TOURNAMENT", id, "Updated tournament settings"));
        return saved;
    }

    public Tournament updateStatus(String id, TournamentStatus newStatus, String actor) {
        Tournament tournament = dataStore.findTournamentById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found: " + id));

        TournamentStatus oldStatus = tournament.getStatus();
        tournament.setStatus(newStatus);
        if (newStatus == TournamentStatus.REGISTRATION_OPEN || newStatus == TournamentStatus.IN_PROGRESS) {
            tournament.setPublished(true);
        }

        Tournament saved = dataStore.saveTournament(tournament);
        dataStore.saveAuditLog(new AuditLog(null, actor, "STATUS_CHANGE", "TOURNAMENT", id, "Status changed from " + oldStatus + " to " + newStatus));
        return saved;
    }

    public void setPublished(String id, boolean published, String actor) {
        Tournament tournament = dataStore.findTournamentById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found: " + id));
        tournament.setPublished(published);
        dataStore.saveTournament(tournament);
        dataStore.saveAuditLog(new AuditLog(null, actor, "PUBLICATION_CHANGED", "TOURNAMENT", id, "Published set to: " + published));
    }

    // ==========================================
    // TEAMS & PARTICIPANTS
    // ==========================================

    public List<Team> getTeamsByTournament(String tournamentId) {
        return dataStore.findTeamsByTournamentId(tournamentId);
    }

    public Team registerTeam(Team team, String actor) {
        Tournament tournament = dataStore.findTournamentById(team.getTournamentId())
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found: " + team.getTournamentId()));

        if (tournament.getStatus() != TournamentStatus.REGISTRATION_OPEN && tournament.getStatus() != TournamentStatus.DRAFT) {
            throw new IllegalStateException("Registrations are not currently open for this tournament.");
        }

        List<Team> currentTeams = dataStore.findTeamsByTournamentId(tournament.getId());
        if (currentTeams.size() >= tournament.getMaxParticipants()) {
            throw new IllegalStateException("Tournament has reached maximum capacity of " + tournament.getMaxParticipants() + " teams.");
        }

        if (team.getId() == null) {
            team.setId("tm-" + UUID.randomUUID().toString().substring(0, 8));
        }
        team.setRegistrationStatus(RegistrationStatus.PENDING);
        team.setSeed(currentTeams.size() + 1);

        Team saved = dataStore.saveTeam(team);

        // Notify organizer
        if (tournament.getOrganizerId() != null) {
            dataStore.saveNotification(new Notification(null, tournament.getOrganizerId(), "New Team Registration",
                    "Team '" + team.getName() + "' registered for " + tournament.getName(), "REGISTRATION"));
        }

        dataStore.saveAuditLog(new AuditLog(null, actor, "TEAM_REGISTERED", "TEAM", saved.getId(), "Registered team: " + saved.getName()));
        return saved;
    }

    public Team updateRegistrationStatus(String teamId, RegistrationStatus status, String reason, String actor) {
        Team team = dataStore.findTeamById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId));

        team.setRegistrationStatus(status);
        Team saved = dataStore.saveTeam(team);

        if (team.getCaptainId() != null) {
            dataStore.saveNotification(new Notification(null, team.getCaptainId(), "Registration " + status,
                    "Your team '" + team.getName() + "' registration was " + status + ". Reason: " + (reason != null ? reason : "None"), "REGISTRATION"));
        }

        dataStore.saveAuditLog(new AuditLog(null, actor, "REGISTRATION_DECISION", "TEAM", teamId, "Set status to " + status + ". Reason: " + reason));
        return saved;
    }

    public Team setCheckIn(String teamId, boolean checkedIn, String actor) {
        Team team = dataStore.findTeamById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamId));

        team.setCheckedIn(checkedIn);
        Team saved = dataStore.saveTeam(team);
        dataStore.saveAuditLog(new AuditLog(null, actor, "CHECK_IN_UPDATE", "TEAM", teamId, "Check-in set to " + checkedIn));
        return saved;
    }

    // ==========================================
    // BRACKET GENERATION & MATCH SCHEDULING
    // ==========================================

    public List<Match> getMatchesByTournament(String tournamentId) {
        return dataStore.findMatchesByTournamentId(tournamentId);
    }

    /**
     * Generates a single-elimination bracket for approved/checked-in teams.
     * Supports arbitrary team counts by calculating power of 2 and assigning byes.
     */
    public synchronized List<Match> generateSingleEliminationBracket(String tournamentId, String actor) {
        Tournament tournament = dataStore.findTournamentById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found: " + tournamentId));

        List<Team> teams = dataStore.findTeamsByTournamentId(tournamentId).stream()
                .filter(t -> t.getRegistrationStatus() == RegistrationStatus.APPROVED)
                .collect(Collectors.toList());

        if (teams.size() < 2) {
            throw new IllegalStateException("At least 2 approved teams are required to generate a bracket. Current count: " + teams.size());
        }

        // Clear existing matches
        dataStore.deleteMatchesByTournamentId(tournamentId);

        // Sort teams by seed
        teams.sort(Comparator.comparingInt(t -> t.getSeed() != null ? t.getSeed() : 999));

        int n = teams.size();
        // Next power of 2
        int powerOfTwo = 1;
        int totalRounds = 0;
        while (powerOfTwo < n) {
            powerOfTwo *= 2;
            totalRounds++;
        }
        if (powerOfTwo == 1) {
            powerOfTwo = 2;
            totalRounds = 1;
        }

        int numByes = powerOfTwo - n;
        int round1MatchesCount = (n - numByes) / 2;

        List<Match> allMatches = new ArrayList<>();
        Map<String, Match> roundMatchesMap = new LinkedHashMap<>();

        // Helper to label round names
        String[] roundNames = getRoundLabels(totalRounds);

        // Generate bracket tree structure backwards from Finals down to Round 1
        // For powerOfTwo = 8: Round 3 has 1 match (Final), Round 2 has 2 matches (SF), Round 1 has 4 match slots
        Map<Integer, List<Match>> matchesByRound = new HashMap<>();

        int matchNumberCounter = 1;

        // Build all rounds
        for (int r = totalRounds; r >= 1; r--) {
            int matchesInRound = (int) Math.pow(2, totalRounds - r);
            List<Match> roundMatches = new ArrayList<>();

            for (int m = 1; m <= matchesInRound; m++) {
                Match match = new Match();
                match.setId("match-" + tournamentId.substring(Math.max(0, tournamentId.length() - 6)) + "-r" + r + "m" + m);
                match.setTournamentId(tournamentId);
                match.setRound(r);
                match.setMatchNumber(matchNumberCounter++);
                match.setRoundName(roundNames[r - 1]);
                match.setMode(tournament.getMode());
                match.setStatus(MatchStatus.UPCOMING);
                match.setScheduledTime("TBD");
                roundMatches.add(match);
            }
            matchesByRound.put(r, roundMatches);
        }

        // Link nextMatchId and nextMatchSlot
        for (int r = 1; r < totalRounds; r++) {
            List<Match> currentRoundMatches = matchesByRound.get(r);
            List<Match> nextRoundMatches = matchesByRound.get(r + 1);

            for (int i = 0; i < currentRoundMatches.size(); i++) {
                Match curr = currentRoundMatches.get(i);
                Match parent = nextRoundMatches.get(i / 2);
                curr.setNextMatchId(parent.getId());
                curr.setNextMatchSlot(i % 2 == 0 ? "A" : "B");
            }
        }

        // Seed teams into Round 1
        List<Match> round1 = matchesByRound.get(1);

        // Standard bracket placement with Byes
        // Teams receiving byes are the top seeded teams.
        List<Team> byeTeams = new ArrayList<>();
        List<Team> playingTeams = new ArrayList<>();

        for (int i = 0; i < numByes; i++) {
            byeTeams.add(teams.get(i));
        }
        for (int i = numByes; i < n; i++) {
            playingTeams.add(teams.get(i));
        }

        // Populate Round 1 matches
        int playIdx = 0;
        int byeIdx = 0;

        for (int i = 0; i < round1.size(); i++) {
            Match m = round1.get(i);
            if (byeIdx < byeTeams.size()) {
                // This branch has a BYE
                Team byeTeam = byeTeams.get(byeIdx++);
                m.setTeamAId(byeTeam.getId());
                m.setTeamAName(byeTeam.getName());
                m.setTeamBId(null);
                m.setTeamBName("BYE");
                m.setScoreA(1);
                m.setScoreB(0);
                m.setWinnerId(byeTeam.getId());
                m.setWinnerName(byeTeam.getName());
                m.setStatus(MatchStatus.COMPLETE);
                m.setBye(true);

                // Auto-advance to next match
                if (m.getNextMatchId() != null) {
                    for (Match nextM : matchesByRound.get(2)) {
                        if (nextM.getId().equals(m.getNextMatchId())) {
                            if ("A".equals(m.getNextMatchSlot())) {
                                nextM.setTeamAId(byeTeam.getId());
                                nextM.setTeamAName(byeTeam.getName());
                            } else {
                                nextM.setTeamBId(byeTeam.getId());
                                nextM.setTeamBName(byeTeam.getName());
                            }
                        }
                    }
                }
            } else {
                // Normal matchup
                if (playIdx < playingTeams.size()) {
                    Team tA = playingTeams.get(playIdx++);
                    m.setTeamAId(tA.getId());
                    m.setTeamAName(tA.getName());
                }
                if (playIdx < playingTeams.size()) {
                    Team tB = playingTeams.get(playIdx++);
                    m.setTeamBId(tB.getId());
                    m.setTeamBName(tB.getName());
                }
                m.setStatus(MatchStatus.UPCOMING);
            }
        }

        // Collect all matches
        for (int r = 1; r <= totalRounds; r++) {
            allMatches.addAll(matchesByRound.get(r));
        }

        dataStore.saveAllMatches(allMatches);

        // Update tournament state to IN_PROGRESS
        tournament.setStatus(TournamentStatus.IN_PROGRESS);
        dataStore.saveTournament(tournament);

        dataStore.saveAuditLog(new AuditLog(null, actor, "BRACKET_GENERATED", "TOURNAMENT", tournamentId,
                "Generated " + totalRounds + "-round bracket with " + n + " teams and " + numByes + " byes"));

        return allMatches;
    }

    private String[] getRoundLabels(int totalRounds) {
        String[] labels = new String[totalRounds];
        for (int i = 0; i < totalRounds; i++) {
            int roundNum = i + 1;
            int remaining = totalRounds - i;
            if (remaining == 1) {
                labels[i] = "Grand Final";
            } else if (remaining == 2) {
                labels[i] = "Semifinals";
            } else if (remaining == 3) {
                labels[i] = "Quarterfinals";
            } else if (remaining == 4) {
                labels[i] = "Round of 16";
            } else {
                labels[i] = "Round " + roundNum;
            }
        }
        return labels;
    }

    // ==========================================
    // MATCH SCORING & WINNER ADVANCEMENT
    // ==========================================

    public Match updateMatchSchedule(String matchId, String scheduledTime, String roomId, String password, String map, String actor) {
        Match match = dataStore.findMatchById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found: " + matchId));

        match.setScheduledTime(scheduledTime);
        match.setRoomId(roomId);
        match.setRoomPassword(password);
        if (map != null && !map.isBlank()) {
            match.setMapName(map);
        }

        Match saved = dataStore.saveMatch(match);
        dataStore.saveAuditLog(new AuditLog(null, actor, "SCHEDULE_UPDATED", "MATCH", matchId, "Room: " + roomId + ", Time: " + scheduledTime));
        return saved;
    }

    public synchronized Match recordMatchResult(String matchId, int scoreA, int scoreB, String reason, String actor) {
        Match match = dataStore.findMatchById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found: " + matchId));

        if (match.getTeamAId() == null || match.getTeamBId() == null) {
            throw new IllegalStateException("Both teams must be determined before recording results.");
        }

        if (scoreA < 0 || scoreB < 0) {
            throw new IllegalArgumentException("Scores cannot be negative.");
        }

        if (scoreA == scoreB) {
            throw new IllegalArgumentException("Match cannot end in a draw in single-elimination tournament.");
        }

        String winnerId = scoreA > scoreB ? match.getTeamAId() : match.getTeamBId();
        String winnerName = scoreA > scoreB ? match.getTeamAName() : match.getTeamBName();

        match.setScoreA(scoreA);
        match.setScoreB(scoreB);
        match.setWinnerId(winnerId);
        match.setWinnerName(winnerName);
        match.setStatus(MatchStatus.COMPLETE);
        match.setLastResultReason(reason);

        Match saved = dataStore.saveMatch(match);

        // Advance winner to next round match
        if (match.getNextMatchId() != null) {
            advanceWinnerToNextMatch(match.getNextMatchId(), match.getNextMatchSlot(), winnerId, winnerName);
        } else {
            // This was the Grand Final!
            Tournament tournament = dataStore.findTournamentById(match.getTournamentId()).orElse(null);
            if (tournament != null) {
                tournament.setStatus(TournamentStatus.COMPLETED);
                dataStore.saveTournament(tournament);
                dataStore.saveAuditLog(new AuditLog(null, actor, "TOURNAMENT_COMPLETED", "TOURNAMENT", tournament.getId(),
                        "Champion crowned: " + winnerName));
            }
        }

        dataStore.saveAuditLog(new AuditLog(null, actor, "RESULT_CONFIRMED", "MATCH", matchId,
                "Score: " + scoreA + "-" + scoreB + ", Winner: " + winnerName + (reason != null ? " (" + reason + ")" : "")));

        return saved;
    }

    private void advanceWinnerToNextMatch(String nextMatchId, String slot, String winnerId, String winnerName) {
        Optional<Match> nextOpt = dataStore.findMatchById(nextMatchId);
        if (nextOpt.isPresent()) {
            Match nextMatch = nextOpt.get();
            if ("A".equalsIgnoreCase(slot)) {
                nextMatch.setTeamAId(winnerId);
                nextMatch.setTeamAName(winnerName);
            } else {
                nextMatch.setTeamBId(winnerId);
                nextMatch.setTeamBName(winnerName);
            }
            dataStore.saveMatch(nextMatch);
        }
    }

    public synchronized Match recordBattleRoyalePlacements(String matchId, List<BrTeamPlacement> placements, String actor) {
        Match match = dataStore.findMatchById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found: " + matchId));

        Tournament tournament = dataStore.findTournamentById(match.getTournamentId())
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found: " + match.getTournamentId()));

        ScoringConfig config = tournament.getScoringConfig();

        // Calculate points
        for (BrTeamPlacement p : placements) {
            int placementPts = config.getPlacementPoints().getOrDefault(p.getPlacement(), 0);
            int killPts = p.getKills() * config.getPointsPerKill();
            p.setPlacementPoints(placementPts);
            p.setKillPoints(killPts);
            p.setTotalPoints(placementPts + killPts);
        }

        // Sort by total points descending
        placements.sort(Comparator.comparingInt(BrTeamPlacement::getTotalPoints).reversed());

        match.setBrPlacements(placements);
        match.setStatus(MatchStatus.COMPLETE);
        if (!placements.isEmpty()) {
            match.setWinnerId(placements.get(0).getTeamId());
            match.setWinnerName(placements.get(0).getTeamName());
        }

        Match saved = dataStore.saveMatch(match);
        dataStore.saveAuditLog(new AuditLog(null, actor, "BR_RESULTS_RECORDED", "MATCH", matchId,
                "Recorded placements for " + placements.size() + " squads. Winner: " + match.getWinnerName()));
        return saved;
    }

    // ==========================================
    // DISPUTES & CONDUCT INTEGRITY
    // ==========================================

    public List<Dispute> getDisputesByTournament(String tournamentId) {
        return dataStore.findDisputesByTournamentId(tournamentId);
    }

    public Dispute submitDispute(Dispute dispute, String actor) {
        if (dispute.getId() == null) {
            dispute.setId("disp-" + UUID.randomUUID().toString().substring(0, 8));
        }
        dispute.setStatus(DisputeStatus.SUBMITTED);
        dispute.setCreatedAt(Instant.now().toString());

        // Mark match as disputed
        if (dispute.getMatchId() != null) {
            dataStore.findMatchById(dispute.getMatchId()).ifPresent(m -> {
                m.setStatus(MatchStatus.DISPUTED);
                dataStore.saveMatch(m);
            });
        }

        Dispute saved = dataStore.saveDispute(dispute);
        dataStore.saveAuditLog(new AuditLog(null, actor, "DISPUTE_SUBMITTED", "DISPUTE", saved.getId(), "Dispute filed for match: " + dispute.getMatchId()));
        return saved;
    }

    public Dispute resolveDispute(String disputeId, String decision, String notes, String reviewer) {
        Dispute dispute = dataStore.findDisputeById(disputeId)
                .orElseThrow(() -> new IllegalArgumentException("Dispute not found: " + disputeId));

        dispute.setDecision(decision);
        dispute.setReviewerNotes(notes);
        dispute.setReviewedBy(reviewer);
        dispute.setStatus(DisputeStatus.RESOLVED);
        dispute.setResolvedAt(Instant.now().toString());

        Dispute saved = dataStore.saveDispute(dispute);

        if (dispute.getSubmittedByUserId() != null) {
            dataStore.saveNotification(new Notification(null, dispute.getSubmittedByUserId(), "Dispute Decision",
                    "Dispute on match has been resolved: " + decision + ". Notes: " + notes, "DISPUTE_UPDATE"));
        }

        dataStore.saveAuditLog(new AuditLog(null, reviewer, "DISPUTE_RESOLVED", "DISPUTE", disputeId, "Decision: " + decision + ", Notes: " + notes));
        return saved;
    }

    public List<ConductReport> getConductReportsByTournament(String tournamentId) {
        return dataStore.findConductReportsByTournamentId(tournamentId);
    }

    public ConductReport submitConductReport(ConductReport report, String actor) {
        if (report.getId() == null) {
            report.setId("rep-" + UUID.randomUUID().toString().substring(0, 8));
        }
        report.setStatus(ReportStatus.SUBMITTED);
        report.setCreatedAt(Instant.now().toString());

        ConductReport saved = dataStore.saveConductReport(report);
        dataStore.saveAuditLog(new AuditLog(null, actor, "CONDUCT_REPORT_SUBMITTED", "CONDUCT", saved.getId(),
                "Report against " + report.getReportedPlayerName() + " (" + report.getCategory() + ")"));
        return saved;
    }

    public ConductReport resolveConductReport(String reportId, ModerationAction action, String reason, String reviewer) {
        ConductReport report = dataStore.findConductReportById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found: " + reportId));

        report.setModerationAction(action);
        report.setModerationReason(reason);
        report.setReviewedBy(reviewer);
        report.setStatus(ReportStatus.RESOLVED);

        ConductReport saved = dataStore.saveConductReport(report);

        if (report.getReportedUserId() != null) {
            dataStore.saveNotification(new Notification(null, report.getReportedUserId(), "Tournament Moderation Notice",
                    "Action taken regarding conduct report: " + action + ". Reason: " + reason, "CONDUCT_ACTION"));
        }

        dataStore.saveAuditLog(new AuditLog(null, reviewer, "MODERATION_ACTION", "CONDUCT", reportId,
                "Action: " + action + " against " + report.getReportedPlayerName() + " Reason: " + reason));
        return saved;
    }

    // ==========================================
    // NOTIFICATIONS & AUDIT LOGS
    // ==========================================

    public List<Notification> getNotificationsForUser(String userId) {
        return dataStore.findNotificationsByRecipient(userId);
    }

    public void markNotificationRead(String id) {
        dataStore.findNotificationById(id).ifPresent(n -> {
            n.setRead(true);
            dataStore.saveNotification(n);
        });
    }

    public List<AuditLog> getAllAuditLogs() {
        return dataStore.findAllAuditLogs();
    }

    // ==========================================
    // EXPORT TO CSV
    // ==========================================

    public String exportResultsToCsv(String tournamentId) {
        Tournament t = dataStore.findTournamentById(tournamentId)
                .orElseThrow(() -> new IllegalArgumentException("Tournament not found: " + tournamentId));
        List<Match> matches = dataStore.findMatchesByTournamentId(tournamentId);

        StringBuilder sb = new StringBuilder();
        sb.append("Tournament:,").append(t.getName()).append("\n");
        sb.append("Game:,").append(t.getGame()).append("\n");
        sb.append("Mode:,").append(t.getMode()).append("\n");
        sb.append("Status:,").append(t.getStatus()).append("\n\n");

        if (t.getMode() == GameMode.BATTLE_ROYALE) {
            sb.append("Round,Match,Map,Placement,Team,Kills,PlacementPts,KillPts,TotalPts\n");
            for (Match m : matches) {
                for (BrTeamPlacement p : m.getBrPlacements()) {
                    sb.append(m.getRound()).append(",")
                            .append(m.getMatchNumber()).append(",")
                            .append(m.getMapName() != null ? m.getMapName() : "N/A").append(",")
                            .append(p.getPlacement()).append(",")
                            .append("\"").append(p.getTeamName()).append("\",")
                            .append(p.getKills()).append(",")
                            .append(p.getPlacementPoints()).append(",")
                            .append(p.getKillPoints()).append(",")
                            .append(p.getTotalPoints()).append("\n");
                }
            }
        } else {
            sb.append("Round,Match,RoundName,TeamA,ScoreA,TeamB,ScoreB,Winner,Status,ScheduledTime,Map\n");
            for (Match m : matches) {
                sb.append(m.getRound()).append(",")
                        .append(m.getMatchNumber()).append(",")
                        .append("\"").append(m.getRoundName()).append("\",")
                        .append("\"").append(m.getTeamAName()).append("\",")
                        .append(m.getScoreA()).append(",")
                        .append("\"").append(m.getTeamBName()).append("\",")
                        .append(m.getScoreB()).append(",")
                        .append("\"").append(m.getWinnerName() != null ? m.getWinnerName() : "TBD").append("\",")
                        .append(m.getStatus()).append(",")
                        .append("\"").append(m.getScheduledTime() != null ? m.getScheduledTime() : "TBD").append("\",")
                        .append("\"").append(m.getMapName() != null ? m.getMapName() : "N/A").append("\"\n");
            }
        }

        return sb.toString();
    }
}
