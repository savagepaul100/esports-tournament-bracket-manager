package com.esports.manager.model;

import java.time.Instant;

public class Notification {
    private String id;
    private String recipientUserId;
    private String title;
    private String message;
    private String type; // REGISTRATION, MATCH_READY, RESULT_CONFIRMED, DISPUTE_UPDATE, CONDUCT_ACTION
    private boolean read = false;
    private String createdAt = Instant.now().toString();

    public Notification() {}

    public Notification(String id, String recipientUserId, String title, String message, String type) {
        this.id = id;
        this.recipientUserId = recipientUserId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.createdAt = Instant.now().toString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRecipientUserId() {
        return recipientUserId;
    }

    public void setRecipientUserId(String recipientUserId) {
        this.recipientUserId = recipientUserId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
