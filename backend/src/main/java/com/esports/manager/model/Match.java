package com.esports.manager.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Match {
    private String id;
    private String tournamentId;
    private int round = 1;
    private int matchNumber = 1;
    private String roundName = "Round 1";
    private GameMode mode = GameMode.SINGLE_ELIMINATION_TDM;
    
    private String teamAId;
    private String teamBId;
    private String teamAName = "TBD";
    private String teamBName = "TBD";
    
    private int scoreA = 0;
    private int scoreB = 0;
    private String winnerId;
    private String winnerName;
    
    private MatchStatus status = MatchStatus.UPCOMING;
    private String scheduledTime;
    private String roomId;
    private String roomPassword;
    private String serverRegion = "Mumbai / Asia";
    private String mapName = "Ascent";
    
    private String nextMatchId;
    private String nextMatchSlot; // "A" or "B"
    private boolean bye = false;
    
    private List<BrTeamPlacement> brPlacements = new ArrayList<>();
    private String lastResultReason;
    private String updatedAt = Instant.now().toString();

    public Match() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(String tournamentId) {
        this.tournamentId = tournamentId;
    }

    public int getRound() {
        return round;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public int getMatchNumber() {
        return matchNumber;
    }

    public void setMatchNumber(int matchNumber) {
        this.matchNumber = matchNumber;
    }

    public String getRoundName() {
        return roundName;
    }

    public void setRoundName(String roundName) {
        this.roundName = roundName;
    }

    public GameMode getMode() {
        return mode;
    }

    public void setMode(GameMode mode) {
        this.mode = mode;
    }

    public String getTeamAId() {
        return teamAId;
    }

    public void setTeamAId(String teamAId) {
        this.teamAId = teamAId;
    }

    public String getTeamBId() {
        return teamBId;
    }

    public void setTeamBId(String teamBId) {
        this.teamBId = teamBId;
    }

    public String getTeamAName() {
        return teamAName;
    }

    public void setTeamAName(String teamAName) {
        this.teamAName = teamAName;
    }

    public String getTeamBName() {
        return teamBName;
    }

    public void setTeamBName(String teamBName) {
        this.teamBName = teamBName;
    }

    public int getScoreA() {
        return scoreA;
    }

    public void setScoreA(int scoreA) {
        this.scoreA = scoreA;
    }

    public int getScoreB() {
        return scoreB;
    }

    public void setScoreB(int scoreB) {
        this.scoreB = scoreB;
    }

    public String getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(String winnerId) {
        this.winnerId = winnerId;
    }

    public String getWinnerName() {
        return winnerName;
    }

    public void setWinnerName(String winnerName) {
        this.winnerName = winnerName;
    }

    public MatchStatus getStatus() {
        return status;
    }

    public void setStatus(MatchStatus status) {
        this.status = status;
    }

    public String getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(String scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getRoomPassword() {
        return roomPassword;
    }

    public void setRoomPassword(String roomPassword) {
        this.roomPassword = roomPassword;
    }

    public String getServerRegion() {
        return serverRegion;
    }

    public void setServerRegion(String serverRegion) {
        this.serverRegion = serverRegion;
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public String getNextMatchId() {
        return nextMatchId;
    }

    public void setNextMatchId(String nextMatchId) {
        this.nextMatchId = nextMatchId;
    }

    public String getNextMatchSlot() {
        return nextMatchSlot;
    }

    public void setNextMatchSlot(String nextMatchSlot) {
        this.nextMatchSlot = nextMatchSlot;
    }

    public boolean isBye() {
        return bye;
    }

    public void setBye(boolean bye) {
        this.bye = bye;
    }

    public List<BrTeamPlacement> getBrPlacements() {
        return brPlacements;
    }

    public void setBrPlacements(List<BrTeamPlacement> brPlacements) {
        this.brPlacements = brPlacements;
    }

    public String getLastResultReason() {
        return lastResultReason;
    }

    public void setLastResultReason(String lastResultReason) {
        this.lastResultReason = lastResultReason;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
