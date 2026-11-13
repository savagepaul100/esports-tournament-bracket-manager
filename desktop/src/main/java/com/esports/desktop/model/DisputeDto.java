package com.esports.desktop.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DisputeDto {
    private String id;
    private String tournamentId;
    private String matchId;
    private String submittedByTeamName;
    private String reason;
    private String evidenceUrl;
    private String status;
    private String decision;
    private String reviewerNotes;
    private String reviewedBy;
    private String resolvedAt;
    private String createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTournamentId() { return tournamentId; }
    public void setTournamentId(String tournamentId) { this.tournamentId = tournamentId; }
    public String getMatchId() { return matchId; }
    public void setMatchId(String matchId) { this.matchId = matchId; }
    public String getSubmittedByTeamName() { return submittedByTeamName; }
    public void setSubmittedByTeamName(String submittedByTeamName) { this.submittedByTeamName = submittedByTeamName; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getEvidenceUrl() { return evidenceUrl; }
    public void setEvidenceUrl(String evidenceUrl) { this.evidenceUrl = evidenceUrl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
    public String getReviewerNotes() { return reviewerNotes; }
    public void setReviewerNotes(String reviewerNotes) { this.reviewerNotes = reviewerNotes; }
    public String getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(String reviewedBy) { this.reviewedBy = reviewedBy; }
    public String getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(String resolvedAt) { this.resolvedAt = resolvedAt; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
