package com.esports.manager.model;

import java.time.Instant;

public class Dispute {
    private String id;
    private String tournamentId;
    private String matchId;
    private String submittedByUserId;
    private String submittedByTeamName;
    private String reason;
    private String evidenceUrl;
    private DisputeStatus status = DisputeStatus.SUBMITTED;
    private String decision;
    private String reviewerNotes;
    private String reviewedBy;
    private String resolvedAt;
    private String createdAt = Instant.now().toString();

    public Dispute() {}

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

    public String getMatchId() {
        return matchId;
    }

    public void setMatchId(String matchId) {
        this.matchId = matchId;
    }

    public String getSubmittedByUserId() {
        return submittedByUserId;
    }

    public void setSubmittedByUserId(String submittedByUserId) {
        this.submittedByUserId = submittedByUserId;
    }

    public String getSubmittedByTeamName() {
        return submittedByTeamName;
    }

    public void setSubmittedByTeamName(String submittedByTeamName) {
        this.submittedByTeamName = submittedByTeamName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getEvidenceUrl() {
        return evidenceUrl;
    }

    public void setEvidenceUrl(String evidenceUrl) {
        this.evidenceUrl = evidenceUrl;
    }

    public DisputeStatus getStatus() {
        return status;
    }

    public void setStatus(DisputeStatus status) {
        this.status = status;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getReviewerNotes() {
        return reviewerNotes;
    }

    public void setReviewerNotes(String reviewerNotes) {
        this.reviewerNotes = reviewerNotes;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public String getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(String resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
