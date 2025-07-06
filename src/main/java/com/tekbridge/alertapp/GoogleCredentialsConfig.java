package com.tekbridge.alertapp;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
public class GoogleCredentialsConfig {

    @PostConstruct
    public void init() throws IOException {
        String gcpCredsJson = System.getenv("GOOGLE_APPLICATION_CREDENTIALS_JSON");
        if (gcpCredsJson == null || gcpCredsJson.isEmpty()) {
            throw new IllegalStateException("GOOGLE_APPLICATION_CREDENTIALS_JSON env var not set");
        }

        Files.write(
                Paths.get("/tmp/service-account.json"),
                gcpCredsJson.getBytes()
        );

        System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", "/tmp/service-account.json");
        System.out.println("✅ Wrote service account file and set system property.");
    }

    @Bean
    public ImageAnnotatorSettings imageAnnotatorSettings() throws IOException {
        try (InputStream credentialsStream =
                     new FileInputStream("/tmp/service-account.json")) {

            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);

            return ImageAnnotatorSettings.newBuilder()
                    .setCredentialsProvider(() -> credentials)
                    .build();
        }
    }
}
