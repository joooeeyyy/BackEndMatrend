package com.tekbridge.alertapp.Servcies;

import com.tekbridge.alertapp.Firebase.MediaDisplay;
import com.tekbridge.alertapp.Models.VideoStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@Service
public class VideoStatusService {

    @Autowired
    HttpHeaders httpHeaders;

    @Autowired
    RestTemplate restTemplate;
    public VideoStatus fetchUpdatedVideoInfo(Long videoId, MediaDisplay media) {

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString("https://viralapi.vadoo.tv/api/get_video_url")
                .queryParam("id", videoId);

        String apiUrlWithParams = builder.toUriString();

        HttpEntity<Void> requestEntity = new HttpEntity<>(httpHeaders);

        ResponseEntity<Map<String, String>> response = restTemplate.exchange(
                apiUrlWithParams,
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<Map<String, String>>() {}
        );

        Map<String, String> mapResponse = response.getBody();

        if (mapResponse == null || !mapResponse.containsKey("url")) {
            throw new IllegalStateException("Video status response missing 'url'");
        }

        String url = mapResponse.get("url");
        System.out.println("✅ Video URL fetched: " + url);

        // You can enhance this: check if URL is empty/null and adjust status accordingly
        String status = (url != null && !url.isBlank()) ? "completed" : "pending";

        return new VideoStatus(status, url);
    }

}
