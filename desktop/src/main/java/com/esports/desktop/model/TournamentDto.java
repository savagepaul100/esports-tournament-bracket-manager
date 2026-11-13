package com.esports.desktop.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TournamentDto {
    private String id;
    private String name;
    private String description;
    private String game;
    private String mode;
    private String status;
    private int teamSize = 5;
    private int maxParticipants = 8;
    private boolean published;
    private String rules;
    private String tournamentStartDate;
    private String password = "ADMIN";

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getGame() { return game; }
    public void setGame(String game) { this.game = game; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getTeamSize() { return teamSize; }
    public void setTeamSize(int teamSize) { this.teamSize = teamSize; }
    public int getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(int maxParticipants) { this.maxParticipants = maxParticipants; }
    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }
    public String getRules() { return rules; }
    public void setRules(String rules) { this.rules = rules; }
    public String getTournamentStartDate() { return tournamentStartDate; }
    public void setTournamentStartDate(String tournamentStartDate) { this.tournamentStartDate = tournamentStartDate; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    @Override
    public String toString() {
        return name + " (" + game + " - " + status + ")";
    }
}
