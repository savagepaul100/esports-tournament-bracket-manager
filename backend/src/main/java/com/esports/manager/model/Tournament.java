package com.esports.manager.model;

import java.time.Instant;

public class Tournament {
    private String id;
    private String name;
    private String description;
    private String game;
    private GameMode mode = GameMode.SINGLE_ELIMINATION_TDM;
    private TournamentStatus status = TournamentStatus.DRAFT;
    private String registrationStartDate;
    private String registrationEndDate;
    private String tournamentStartDate;
    private String timeZone = "UTC+05:30";
    private int teamSize = 5;
    private int maxParticipants = 8;
    private String rules = "Standard competitive fair-play rules. Screen recording mandatory upon dispute.";
    private ScoringConfig scoringConfig = new ScoringConfig();
    private String organizerId;
    private boolean published = false;
    private String bannerUrl;
    private String createdAt = Instant.now().toString();

    public Tournament() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGame() {
        return game;
    }

    public void setGame(String game) {
        this.game = game;
    }

    public GameMode getMode() {
        return mode;
    }

    public void setMode(GameMode mode) {
        this.mode = mode;
    }

    public TournamentStatus getStatus() {
        return status;
    }

    public void setStatus(TournamentStatus status) {
        this.status = status;
    }

    public String getRegistrationStartDate() {
        return registrationStartDate;
    }

    public void setRegistrationStartDate(String registrationStartDate) {
        this.registrationStartDate = registrationStartDate;
    }

    public String getRegistrationEndDate() {
        return registrationEndDate;
    }

    public void setRegistrationEndDate(String registrationEndDate) {
        this.registrationEndDate = registrationEndDate;
    }

    public String getTournamentStartDate() {
        return tournamentStartDate;
    }

    public void setTournamentStartDate(String tournamentStartDate) {
        this.tournamentStartDate = tournamentStartDate;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public int getTeamSize() {
        return teamSize;
    }

    public void setTeamSize(int teamSize) {
        this.teamSize = teamSize;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public void setMaxParticipants(int maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public String getRules() {
        return rules;
    }

    public void setRules(String rules) {
        this.rules = rules;
    }

    public ScoringConfig getScoringConfig() {
        return scoringConfig;
    }

    public void setScoringConfig(ScoringConfig scoringConfig) {
        this.scoringConfig = scoringConfig;
    }

    public String getOrganizerId() {
        return organizerId;
    }

    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public String getBannerUrl() {
        return bannerUrl;
    }

    public void setBannerUrl(String bannerUrl) {
        this.bannerUrl = bannerUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
