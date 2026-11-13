package com.esports.manager.model;

import java.util.HashMap;
import java.util.Map;

public class ScoringConfig {
    private int pointsPerKill = 1;
    private Map<Integer, Integer> placementPoints = new HashMap<>();
    private int scoreToWin = 13; // default for TDM or Clash
    private boolean overtimeAllowed = false;
    private String tieBreakerRule = "HEAD_TO_HEAD_KILLS";

    public ScoringConfig() {
        // Standard Battle Royale default placement points (1st: 10, 2nd: 6, 3rd: 5, 4th: 4, 5th: 3, 6th: 2, 7th: 1, 8th: 1)
        placementPoints.put(1, 10);
        placementPoints.put(2, 6);
        placementPoints.put(3, 5);
        placementPoints.put(4, 4);
        placementPoints.put(5, 3);
        placementPoints.put(6, 2);
        placementPoints.put(7, 1);
        placementPoints.put(8, 1);
    }

    public int getPointsPerKill() {
        return pointsPerKill;
    }

    public void setPointsPerKill(int pointsPerKill) {
        this.pointsPerKill = pointsPerKill;
    }

    public Map<Integer, Integer> getPlacementPoints() {
        return placementPoints;
    }

    public void setPlacementPoints(Map<Integer, Integer> placementPoints) {
        this.placementPoints = placementPoints;
    }

    public int getScoreToWin() {
        return scoreToWin;
    }

    public void setScoreToWin(int scoreToWin) {
        this.scoreToWin = scoreToWin;
    }

    public boolean isOvertimeAllowed() {
        return overtimeAllowed;
    }

    public void setOvertimeAllowed(boolean overtimeAllowed) {
        this.overtimeAllowed = overtimeAllowed;
    }

    public String getTieBreakerRule() {
        return tieBreakerRule;
    }

    public void setTieBreakerRule(String tieBreakerRule) {
        this.tieBreakerRule = tieBreakerRule;
    }
}
