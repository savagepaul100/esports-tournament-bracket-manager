package com.esports.manager.model;

import java.time.Instant;

public class ConductReport {
    private String id;
    private String tournamentId;
    private String matchId;
    private String reportedUserId;
    private String reportedPlayerName;
    private String reportedTeamName;
    private String submittedByUserId;
    private String submittedByName;
    private ConductCategory category = ConductCategory.CHEATING_SUSPICION;
    private String description;
    private String evidenceUrl;
    private ReportStatus status = ReportStatus.SUBMITTED;
    private ModerationAction moderationAction = ModerationAction.NONE;
    private String moderationReason;
    private String appealMessage;
    private String reviewedBy;
    private String createdAt = Instant.now().toString();

    public ConductReport() {}

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

    public String getReportedUserId() {
        return reportedUserId;
    }

    public void setReportedUserId(String reportedUserId) {
        this.reportedUserId = reportedUserId;
    }

    public String getReportedPlayerName() {
        return reportedPlayerName;
    }

    public void setReportedPlayerName(String reportedPlayerName) {
        this.reportedPlayerName = reportedPlayerName;
    }

    public String getReportedTeamName() {
        return reportedTeamName;
    }

    public void setReportedTeamName(String reportedTeamName) {
        this.reportedTeamName = reportedTeamName;
    }

    public String getSubmittedByUserId() {
        return submittedByUserId;
    }

    public void setSubmittedByUserId(String submittedByUserId) {
        this.submittedByUserId = submittedByUserId;
    }

    public String getSubmittedByName() {
        return submittedByName;
    }

    public void setSubmittedByName(String submittedByName) {
        this.submittedByName = submittedByName;
    }

    public ConductCategory getCategory() {
        return category;
    }

    public void setCategory(ConductCategory category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEvidenceUrl() {
        return evidenceUrl;
    }

    public void setEvidenceUrl(String evidenceUrl) {
        this.evidenceUrl = evidenceUrl;
    }

    public ReportStatus getStatus() {
        return status;
    }

    public void setStatus(ReportStatus status) {
        this.status = status;
    }

    public ModerationAction getModerationAction() {
        return moderationAction;
    }

    public void setModerationAction(ModerationAction moderationAction) {
        this.moderationAction = moderationAction;
    }

    public String getModerationReason() {
        return moderationReason;
    }

    public void setModerationReason(String moderationReason) {
        this.moderationReason = moderationReason;
    }

    public String getAppealMessage() {
        return appealMessage;
    }

    public void setAppealMessage(String appealMessage) {
        this.appealMessage = appealMessage;
    }

    public String getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(String reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
