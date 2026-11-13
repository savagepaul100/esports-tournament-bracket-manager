package com.esports.desktop.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConductReportDto {
    private String id;
    private String tournamentId;
    private String matchId;
    private String reportedPlayerName;
    private String reportedTeamName;
    private String submittedByName;
    private String category;
    private String description;
    private String evidenceUrl;
    private String status;
    private String moderationAction;
    private String moderationReason;
    private String reviewedBy;
    private String createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTournamentId() { return tournamentId; }
    public void setTournamentId(String tournamentId) { this.tournamentId = tournamentId; }
    public String getMatchId() { return matchId; }
    public void setMatchId(String matchId) { this.matchId = matchId; }
    public String getReportedPlayerName() { return reportedPlayerName; }
    public void setReportedPlayerName(String reportedPlayerName) { this.reportedPlayerName = reportedPlayerName; }
    public String getReportedTeamName() { return reportedTeamName; }
    public void setReportedTeamName(String reportedTeamName) { this.reportedTeamName = reportedTeamName; }
    public String getSubmittedByName() { return submittedByName; }
    public void setSubmittedByName(String submittedByName) { this.submittedByName = submittedByName; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getEvidenceUrl() { return evidenceUrl; }
    public void setEvidenceUrl(String evidenceUrl) { this.evidenceUrl = evidenceUrl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getModerationAction() { return moderationAction; }
    public void setModerationAction(String moderationAction) { this.moderationAction = moderationAction; }
    public String getModerationReason() { return moderationReason; }
    public void setModerationReason(String moderationReason) { this.moderationReason = moderationReason; }
    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
