package com.esports.manager.model;

public class BrTeamPlacement {
    private String teamId;
    private String teamName;
    private int placement;
    private int kills;
    private int placementPoints;
    private int killPoints;
    private int totalPoints;

    public BrTeamPlacement() {}

    public BrTeamPlacement(String teamId, String teamName, int placement, int kills, int placementPoints, int killPoints) {
        this.teamId = teamId;
        this.teamName = teamName;
        this.placement = placement;
        this.kills = kills;
        this.placementPoints = placementPoints;
        this.killPoints = killPoints;
        this.totalPoints = placementPoints + killPoints;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public int getPlacement() {
        return placement;
    }

    public void setPlacement(int placement) {
        this.placement = placement;
    }

    public int getKills() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills = kills;
    }

    public int getPlacementPoints() {
        return placementPoints;
    }

    public void setPlacementPoints(int placementPoints) {
        this.placementPoints = placementPoints;
        this.totalPoints = this.placementPoints + this.killPoints;
    }

    public int getKillPoints() {
        return killPoints;
    }

    public void setKillPoints(int killPoints) {
        this.killPoints = killPoints;
        this.totalPoints = this.placementPoints + this.killPoints;
    }

    public int getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(int totalPoints) {
        this.totalPoints = totalPoints;
    }
}
