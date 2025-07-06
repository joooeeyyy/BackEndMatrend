package com.tekbridge.alertapp.OpenAi.Controllers;

import com.tekbridge.alertapp.OpenAi.Configurations.DalleImageGeneratorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientException;
import reactor.core.publisher.Mono;

import java.net.URI;


public class DallecoolController {
    private static final Logger LOGGER = LoggerFactory.getLogger(DallecoolController.class);
    private final DalleImageGeneratorService imageGenerator;

    public DallecoolController(DalleImageGeneratorService imageGenerator) {
        this.imageGenerator = imageGenerator;
    }

    @GetMapping("/api/v1/image")
    Mono<ImageResponse> generateImage(@RequestParam("prompt") String prompt) {
        return imageGenerator.generateImage(prompt).map(resp -> new ImageResponse(prompt, resp));
    }

    @ExceptionHandler(WebClientException.class)
    ProblemDetail onError(WebClientException e) {
        LOGGER.warn("Error while generating image with DALL-E", e);
        final var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        problemDetail.setTitle("Image generation failed");
        problemDetail.setType(URI.create("urn:problem-type:image-generation-failed"));
        return problemDetail;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail onError(IllegalArgumentException e) {
        LOGGER.warn("Input error while generating image with DALL-E", e);
        final var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        problemDetail.setTitle("Input error");
        problemDetail.setType(URI.create("urn:problem-type:input-error"));
        return problemDetail;
    }

    public record ImageResponse(String prompt, String url) {
    }
}
