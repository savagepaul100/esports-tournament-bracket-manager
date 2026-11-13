package com.esports.manager.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Team {
    private String id;
    private String tournamentId;
    private String name;
    private String tag;
    private String logoUrl;
    private String captainId;
    private String captainEmail;
    private List<String> playerIds = new ArrayList<>();
    private List<String> playerNames = new ArrayList<>();
    private boolean checkedIn = false;
    private Integer seed = 0;
    private RegistrationStatus registrationStatus = RegistrationStatus.PENDING;
    private String registeredAt = Instant.now().toString();

    public Team() {}

    public Team(String id, String tournamentId, String name, String tag, String captainId, List<String> playerNames) {
        this.id = id;
        this.tournamentId = tournamentId;
        this.name = name;
        this.tag = tag;
        this.captainId = captainId;
        this.playerNames = playerNames != null ? playerNames : new ArrayList<>();
        this.registeredAt = Instant.now().toString();
    }

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getCaptainId() {
        return captainId;
    }

    public void setCaptainId(String captainId) {
        this.captainId = captainId;
    }

    public String getCaptainEmail() {
        return captainEmail;
    }

    public void setCaptainEmail(String captainEmail) {
        this.captainEmail = captainEmail;
    }

    public List<String> getPlayerIds() {
        return playerIds;
    }

    public void setPlayerIds(List<String> playerIds) {
        this.playerIds = playerIds;
    }

    public List<String> getPlayerNames() {
        return playerNames;
    }

    public void setPlayerNames(List<String> playerNames) {
        this.playerNames = playerNames;
    }

    public boolean isCheckedIn() {
        return checkedIn;
    }

    public void setCheckedIn(boolean checkedIn) {
        this.checkedIn = checkedIn;
    }

    public Integer getSeed() {
        return seed;
    }

    public void setSeed(Integer seed) {
        this.seed = seed;
    }

    public RegistrationStatus getRegistrationStatus() {
        return registrationStatus;
    }

    public void setRegistrationStatus(RegistrationStatus registrationStatus) {
        this.registrationStatus = registrationStatus;
    }

    public String getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(String registeredAt) {
        this.registeredAt = registeredAt;
    }
}
