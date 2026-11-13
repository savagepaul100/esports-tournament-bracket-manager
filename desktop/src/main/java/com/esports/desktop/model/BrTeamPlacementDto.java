package com.esports.desktop.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BrTeamPlacementDto {
    private String teamId;
    private String teamName;
    private int placement;
    private int kills;
    private int placementPoints;
    private int killPoints;
    private int totalPoints;

    public String getTeamId() { return teamId; }
    public void setTeamId(String teamId) { this.teamId = teamId; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public int getPlacement() { return placement; }
    public void setPlacement(int placement) { this.placement = placement; }
    public int getKills() { return kills; }
    public void setKills(int kills) { this.kills = kills; }
    public int getPlacementPoints() { return placementPoints; }
    public void setPlacementPoints(int placementPoints) { this.placementPoints = placementPoints; }
    public int getKillPoints() { return killPoints; }
    public void setKillPoints(int killPoints) { this.killPoints = killPoints; }
    public int getTotalPoints() { return totalPoints; }
    public void setTotalPoints(int totalPoints) { this.totalPoints = totalPoints; }
}
