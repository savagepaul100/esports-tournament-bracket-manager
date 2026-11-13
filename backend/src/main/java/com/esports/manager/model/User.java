package com.esports.manager.model;

import java.time.Instant;

public class User {
    private String id;
    private String email;
    private String username;
    private String password;
    private Role role = Role.VIEWER;
    private String gamerTag;
    private String avatarUrl;
    private String createdAt = Instant.now().toString();

    public User() {}

    public User(String id, String email, String username, String password, Role role, String gamerTag) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.password = password;
        this.role = role;
        this.gamerTag = gamerTag;
        this.createdAt = Instant.now().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getGamerTag() {
        return gamerTag;
    }

    public void setGamerTag(String gamerTag) {
        this.gamerTag = gamerTag;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
