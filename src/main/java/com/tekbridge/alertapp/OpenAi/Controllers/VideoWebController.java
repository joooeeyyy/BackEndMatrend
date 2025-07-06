package com.tekbridge.alertapp.OpenAi.Controllers;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tekbridge.alertapp.Models.VideoGenRequestModel;
import com.tekbridge.alertapp.Models.VideoStatus;
import com.tekbridge.alertapp.OpenAi.Records.VideoGenRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Map;

@Controller
public class VideoWebController {

        RestTemplate restTemplate;
        HttpHeaders httpHeaders;

        @Autowired
        public VideoWebController(RestTemplate restTemplate,
                                  HttpHeaders httpHeaders) {
                this.restTemplate = restTemplate;
                this.httpHeaders = httpHeaders;
        }

    public ResponseEntity<String> requestVideoGeneration(
            VideoGenRequestModel videoGenRequest,
            String need3Url
    ) throws JsonProcessingException {

        HttpEntity<Object> requestEntity = new HttpEntity<>(videoGenRequest, httpHeaders);

        ResponseEntity<String> response = restTemplate.postForEntity(need3Url, requestEntity, String.class);

        System.out.println("Video Request End Block " + response.getBody());

        String json = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = objectMapper.readValue(json, Map.class);

        return getGeneratedVideoUrl(map);
    }
        @PostMapping("/matrend-video")
        public ResponseEntity<String> getVideoRequest(
                @RequestBody VideoGenRequestModel videoGenRequest,
                @Value("${spring.video.need3}") String need3
        ) throws JsonProcessingException {
               // System.out.println("Video Request Start Block "+videoGenRequest);
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            headers.set("Authorization", "Bearer YOUR_API_KEY");


                HttpEntity<Object> requestEntity = new HttpEntity<>(videoGenRequest,httpHeaders);

            ResponseEntity<String> response = restTemplate.postForEntity(need3, requestEntity, String.class);
                // Send the POST request and return the response
//                ResponseEntity<Map<String , Object>> responseMap = restTemplate.exchange(
//                        URI.create(need3)
//                        ,HttpMethod.POST,
//                        requestEntity,
//                        new ParameterizedTypeReference<Map<String, Object>>() {}
//                );

                System.out.println("Video Request End Block "+response.getBody());
            String json = response.getBody();

            ObjectMapper objectMapper = new ObjectMapper();

            Map<String, Object> map = objectMapper.readValue(json, Map.class);
                return getGeneratedVideoUrl(map);
        }

        private ResponseEntity<String> getGeneratedVideoUrl(Map<String,Object> map){
                System.out.println("getGenerated Video Url Start ");
            Long parseInt = (Long) map.get("vid");
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromUri(URI.create("https://viralapi.vadoo.tv/api/get_video_url"))
                        .queryParam("id", parseInt);
            String apiUrlWithParams = builder.toUriString(); //
            HttpEntity<Object> requestEntity = new HttpEntity<>(httpHeaders);
            ResponseEntity<Map<String,String>> responseString = restTemplate.exchange(
                    apiUrlWithParams,
                    HttpMethod.GET,
                    requestEntity,
                    new ParameterizedTypeReference<Map<String, String>>() {}
            );
            Map<String ,String> mapResponse = responseString.getBody();
            String Url = mapResponse.get("url");
                    System.out.println("getGenerated Video Url End  "+Url);
                    return ResponseEntity.ok()
                            .body(responseString.getBody().toString()+" Track video creation progress with Id : "+parseInt);


        }


        @GetMapping("/matrend-video/id")
        public ResponseEntity<Map<String,String>> getVideoById(@RequestParam String videoId){
            Long parseInt = Long.valueOf(videoId);
            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromUri(URI.create("https://viralapi.vadoo.tv/api/get_video_url"))
                    .queryParam("id",parseInt);
                    String apiUrlWithParams = builder.toUriString(); //
                    HttpEntity<Object> requestEntity = new HttpEntity<>(httpHeaders);
                     ResponseEntity<Map<String,String>> responseString = restTemplate.exchange(
                    apiUrlWithParams,
                    HttpMethod.GET,
                    requestEntity,
                    new ParameterizedTypeReference<Map<String, String>>() {}
            );
            Map<String ,String> mapResponse = responseString.getBody();
            String Url = mapResponse.get("url");
            return ResponseEntity.ok()
                    .body(responseString.getBody());
        }

}

