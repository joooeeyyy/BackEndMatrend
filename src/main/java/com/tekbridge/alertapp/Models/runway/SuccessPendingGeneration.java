package com.tekbridge.alertapp.Models.runway;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // Optional: for ignoring unknown properties

@JsonIgnoreProperties(ignoreUnknown = true) // Optional: If JSON might have more fields than your class
public class SuccessPendingGeneration {

    private String id;
    private String createdAt; // Keep as String if you want to parse date later, or use java.time.OffsetDateTime
    private String status;
    private double progress;

    // Constructors (default is often needed by Jackson)
    public SuccessPendingGeneration() {
    }

    public SuccessPendingGeneration(String id, String createdAt, String status, double progress) {
        this.id = id;
        this.createdAt = createdAt;
        this.status = status;
        this.progress = progress;
    }

    // Getters and Setters (essential for Jackson to work)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    // Jackson by default maps JSON keys to field names if they match (case-insensitive by default with some configurations).
    // If names don't match, or for clarity, use @JsonProperty
    @JsonProperty("createdAt")
    public String getCreatedAt() {
        return createdAt;
    }

    @JsonProperty("createdAt")
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    @Override
    public String toString() {
        return "MyResponseObject{" +
                "id='" + id + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", status='" + status + '\'' +
                ", progress=" + progress +
                '}';
    }
}

