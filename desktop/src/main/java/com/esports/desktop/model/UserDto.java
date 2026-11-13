package com.esports.desktop.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto {
    private String id;
    private String username;
    private String email;
    private String role;
    private String gamerTag;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getGamerTag() { return gamerTag; }
    public void setGamerTag(String gamerTag) { this.gamerTag = gamerTag; }
}
