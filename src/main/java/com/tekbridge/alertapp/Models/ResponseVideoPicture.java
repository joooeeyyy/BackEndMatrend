package com.tekbridge.alertapp.Models;

import java.util.List;

public class ResponseVideoPicture {

    Long videoId;
    List<String> videoUrls;

    public ResponseVideoPicture(Long videoId, List<String> videoUrls) {
        this.videoId = videoId;
        this.videoUrls = videoUrls;
    }

    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }

    public List<String> getVideoUrls() {
        return videoUrls;
    }

    public void setVideoUrls(List<String> videoUrls) {
        this.videoUrls = videoUrls;
    }
}
