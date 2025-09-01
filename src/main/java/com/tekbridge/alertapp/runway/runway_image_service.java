package com.tekbridge.alertapp.runway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.tekbridge.alertapp.Models.runway.SuccessGeneratedContent;
import com.tekbridge.alertapp.Models.runway.SuccessPendingGeneration;
import com.tekbridge.alertapp.Models.runway.runway_model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Arrays;
import java.util.Map;

@Service
public class runway_image_service {

    RestTemplate restTemplate;
    HttpHeaders httpHeaders;

    @Autowired
    public runway_image_service(RestTemplate restTemplate,
                                HttpHeaders httpHeaders ,@Value("${runway.ai.api.key}") String key) {
        this.restTemplate = restTemplate;
        HttpHeaders httpHeaders2 = new HttpHeaders();
        httpHeaders2.set("Authorization", "Bearer "+key);
//        httpHeaders2.set("Content-Type","application/json");
//        httpHeaders2.set("Accept","application/json");
        httpHeaders2.set("X-Runway-Version", "2024-11-06");
        this.httpHeaders = httpHeaders2;
    }


    public String requestRunwayImageGeneration(String promptToGenerateFrom, String businessName) throws JsonProcessingException {

        // 1. Create instances of your POJOs
        runway_model.ContentModeration moderation =
                new runway_model.ContentModeration("auto");

        runway_model.ReferenceImage image1 =
                new runway_model.ReferenceImage("uri_path_1", "tag1");
        runway_model.ReferenceImage image2 =
                new runway_model.ReferenceImage("uri_path_2", "tag2");

        runway_model runwayModel = new runway_model(
                promptToGenerateFrom,
              //  "You are a grapic designer , design a flyer for my business " + promptToGenerateFrom + " put my business name '" + businessName + "' on it with a nice caption",
                "1920:1080",
                4294967295L, // Note the L for long
                "gen4_image",
                //Arrays.asList(image1, image2),
                moderation
        );
        HttpEntity<Object> requestEntity = new HttpEntity<>(runwayModel, httpHeaders);
        ResponseEntity<String> response = restTemplate.postForEntity("https://api.dev.runwayml.com/v1/text_to_image", requestEntity, String.class);
        System.out.println("RUNWAY Request End Block " + response.getBody());

        String json = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = objectMapper.readValue(json, Map.class);
        return (String) map.get("id");
    }

    public Object getIdRunwayAndVerify(String id) throws JsonProcessingException {

        System.out.println("RUNWAY Request Started End Block " + id);
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUri(URI.create("https://api.dev.runwayml.com/v1/tasks/"+id));
        System.out.println("RUNWAY Request Started End Block two" + id);
        String apiUrlWithParams = builder.toUriString(); //
        System.out.println("RUNWAY Request Started End Block three" + id);
        HttpEntity<Object> requestEntity = new HttpEntity<>(httpHeaders);
        System.out.println("RUNWAY Request Started End Block four" + id +" "+apiUrlWithParams);
        ResponseEntity<Map<String, String>> responseString = null;
        try {
             responseString = restTemplate.exchange(
                    apiUrlWithParams,
                    HttpMethod.GET,
                    requestEntity,
                    new ParameterizedTypeReference<Map<String, String>>() {
                    }
            );
            System.out.println("RUNWAY Request Started End Block four" + responseString.getBody());
        }catch (Exception e){
            System.out.println("RUNWAY Request Started End Block Exception " + e.getMessage());
        }
        System.out.println("RUNWAY Request Started End Block four" + responseString.getBody());

        String jsonString = null;
        System.out.println("RUNWAY Request JsonString is null End Block " + id);
        if (responseString.getStatusCode().is2xxSuccessful() && responseString.getBody() != null) {
            System.out.println("RUNWAY Request Started Success End Block " + id);
            Map<String, String> responseMap;
            Gson gson = new Gson(); // Gson instance

            ObjectMapper objectMapper = null;
            try{
                responseMap = responseString.getBody();
                System.out.println(responseMap.get("id"));
                jsonString = gson.toJson(responseMap);
                System.out.println("Converted JSON String (Gson): " + jsonString);
                objectMapper = new ObjectMapper();
            }catch (Exception e){
                System.out.println("RUNWAY Request Started Failure End Block " + id +e.getMessage());
            }

            // If your mapResponse is already a Map<String, Object> from some other process
            // Map<String, Object> mapResponse = ... ;
            // MyResponseObject myObject = objectMapper.convertValue(mapResponse, MyResponseObject.class);

            // If your response is a JSON String
            try {
                SuccessPendingGeneration myObject = objectMapper.readValue(jsonString, SuccessPendingGeneration.class);

                System.out.println("Converted Java Object: " + myObject);
                System.out.println("ID: " + myObject.getId());
                System.out.println("Progress: " + myObject.getProgress());
                return  myObject;
            }catch (JsonProcessingException e){
                System.out.println("Error: SuccessPendingGeneration " + e.getMessage());

            }

            try {
                SuccessGeneratedContent objectSuccess = objectMapper.readValue(jsonString, SuccessGeneratedContent.class);
                System.out.println("Converted Java Object: " + objectSuccess);
                System.out.println("ID: " + objectSuccess.getId());
                return objectSuccess;
            }catch (JsonProcessingException e){
                System.out.println("Error: SuccessGeneratedContent " + e.getMessage());
                return null;

            }

        } else {
            System.err.println("Request failed or body is null. Status: " + responseString.getStatusCode());
            return null;
        }

//        String jsonString = "{\"id\":\"8b31c345-a682-482b-a27e-2e5452fa393b\",\"createdAt\":\"2025-08-21T20:05:55.785Z\",\"status\":\"RUNNING\",\"progress\":0.6980000000000001}";


//
//        {
//            "id": "8b31c345-a682-482b-a27e-2e5452fa393b",
//                "createdAt": "2025-08-21T20:05:55.785Z",
//                "status": "RUNNING",
//                "progress": 0.6980000000000001
//        }

//        {
//            "id": "8b31c345-a682-482b-a27e-2e5452fa393b",
//                "createdAt": "2025-08-21T20:05:55.785Z",
//                "status": "SUCCEEDED",
//                "output": [
//            "https://dnznrvs05pmza.cloudfront.net/4f7706bf-90d9-4d7d-9a0e-efc8e8248566.png?_jwt=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJrZXlIYXNoIjoiYTY4ZjE3ZTY2OTYzZTJkNCIsImJ1Y2tldCI6InJ1bndheS10YXNrLWFydGlmYWN0cyIsInN0YWdlIjoicHJvZCIsImV4cCI6MTc1NTkwNzIwMH0.78wjukChvnSUnHlEH_fmaaKQRsN7waoaExTt9QswfwM"
//    ]
//        }

    }

}
