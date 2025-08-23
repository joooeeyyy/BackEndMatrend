package com.tekbridge.alertapp.Models.runway;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List; // Import List for the 'output' array

@JsonIgnoreProperties(ignoreUnknown = true) // Always a good practice
public class SuccessGeneratedContent {

    private String id;
    private String createdAt;
    private String status;
    private List<String> output; // To hold the array of strings

    // Default constructor (often needed by Jackson)
    public SuccessGeneratedContent() {
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("createdAt") // Use if JSON key might differ or for clarity
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

    public List<String> getOutput() {
        return output;
    }

    public void setOutput(List<String> output) {
        this.output = output;
    }

    @Override
    public String toString() {
        return "SuccessPendingGeneration{" +
                "id='" + id + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", status='" + status + '\'' +
                ", output=" + (output != null ? String.join(", ", output) : "null") + // Nicer printing for list
                '}';
    }
}
