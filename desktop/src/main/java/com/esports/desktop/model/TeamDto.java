package com.esports.desktop.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TeamDto {
    private String id;
    private String tournamentId;
    private String name;
    private String tag;
    private String captainId;
    private String captainEmail;
    private List<String> playerNames = new ArrayList<>();
    private boolean checkedIn;
    private Integer seed;
    private String registrationStatus;
    private String password;

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTournamentId() { return tournamentId; }
    public void setTournamentId(String tournamentId) { this.tournamentId = tournamentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
    public String getCaptainId() { return captainId; }
    public void setCaptainId(String captainId) { this.captainId = captainId; }
    public String getCaptainEmail() { return captainEmail; }
    public void setCaptainEmail(String captainEmail) { this.captainEmail = captainEmail; }
    public List<String> getPlayerNames() { return playerNames; }
    public void setPlayerNames(List<String> playerNames) { this.playerNames = playerNames; }
    public boolean isCheckedIn() { return checkedIn; }
    public void setCheckedIn(boolean checkedIn) { this.checkedIn = checkedIn; }
    public Integer getSeed() { return seed; }
    public void setSeed(Integer seed) { this.seed = seed; }
    public String getRegistrationStatus() { return registrationStatus; }
    public void setRegistrationStatus(String registrationStatus) { this.registrationStatus = registrationStatus; }
}
