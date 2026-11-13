package com.esports.desktop.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchDto {
    private String id;
    private String tournamentId;
    private int round;
    private int matchNumber;
    private String roundName;
    private String mode;
    private String teamAId;
    private String teamBId;
    private String teamAName = "TBD";
    private String teamBName = "TBD";
    private int scoreA;
    private int scoreB;
    private int killsA;
    private int killsB;
    private String winnerId;
    private String winnerName;
    private String status;
    private String scheduledTime;
    private String roomId;
    private String roomPassword;
    private String mapName;
    private boolean bye;
    private List<BrTeamPlacementDto> brPlacements = new ArrayList<>();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTournamentId() { return tournamentId; }
    public void setTournamentId(String tournamentId) { this.tournamentId = tournamentId; }
    public int getRound() { return round; }
    public void setRound(int round) { this.round = round; }
    public int getMatchNumber() { return matchNumber; }
    public void setMatchNumber(int matchNumber) { this.matchNumber = matchNumber; }
    public String getRoundName() { return roundName; }
    public void setRoundName(String roundName) { this.roundName = roundName; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getTeamAId() { return teamAId; }
    public void setTeamAId(String teamAId) { this.teamAId = teamAId; }
    public String getTeamBId() { return teamBId; }
    public void setTeamBId(String teamBId) { this.teamBId = teamBId; }
    public String getTeamAName() { return teamAName; }
    public void setTeamAName(String teamAName) { this.teamAName = teamAName; }
    public String getTeamBName() { return teamBName; }
    public void setTeamBName(String teamBName) { this.teamBName = teamBName; }
    public int getScoreA() { return scoreA; }
    public void setScoreA(int scoreA) { this.scoreA = scoreA; }
    public int getScoreB() { return scoreB; }
    public void setScoreB(int scoreB) { this.scoreB = scoreB; }
    public int getKillsA() { return killsA; }
    public void setKillsA(int killsA) { this.killsA = killsA; }
    public int getKillsB() { return killsB; }
    public void setKillsB(int killsB) { this.killsB = killsB; }
    public String getWinnerId() { return winnerId; }
    public void setWinnerId(String winnerId) { this.winnerId = winnerId; }
    public String getWinnerName() { return winnerName; }
    public void setWinnerName(String winnerName) { this.winnerName = winnerName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(String scheduledTime) { this.scheduledTime = scheduledTime; }
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public String getRoomPassword() { return roomPassword; }
    public void setRoomPassword(String roomPassword) { this.roomPassword = roomPassword; }
    public String getMapName() { return mapName; }
    public void setMapName(String mapName) { this.mapName = mapName; }
    public boolean isBye() { return bye; }
    public void setBye(boolean bye) { this.bye = bye; }
    public List<BrTeamPlacementDto> getBrPlacements() { return brPlacements; }
    public void setBrPlacements(List<BrTeamPlacementDto> brPlacements) { this.brPlacements = brPlacements; }
}
