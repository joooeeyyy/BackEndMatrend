package com.tekbridge.alertapp.OpenAi.Configurations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


public class DalleImageGeneratorService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DalleImageGeneratorService.class);

    private final WebClient client;
    private final String apiEndpoint = "https://api.openai.com/v1/images/generations";

    public DalleImageGeneratorService(WebClient client) {
        this.client = client;
    }

    public Mono<String> generateImage(String prompt) {
        if (!StringUtils.hasText(prompt)) {
            throw new IllegalArgumentException("Prompt must not be empty");
        }
        LOGGER.info("Sending request to DALL-E: {}", prompt);
        final var req = new ImageGenerationRequest(prompt);
        return client.post().uri(apiEndpoint)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + "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")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .bodyValue(req)
                .retrieve()
                .bodyToMono(ImageGenerationResponse.class)
                .doOnSuccess(resp -> {
                    LOGGER.info("Received response from DALL-E for request: {}", prompt);
                })
                .filter(resp -> resp.data.length > 0)
                .map(resp -> resp.data[0].url);
    }

    private record ImageGenerationResponse(ImageGenerationResponseUrl[] data) {
    }

    private record ImageGenerationResponseUrl(String url) {
    }

    private record ImageGenerationRequest(String prompt) {
    }
}
