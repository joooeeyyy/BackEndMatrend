package com.tekbridge.alertapp.Models;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VideoStatus {
    private String status;
    private String url;

    public String getStatus() { return status; }
    public void setStatus(String value) { this.status = value; }

    public String getUrl() { return url; }
    public void setURL(String value) { this.url = value; }
}
