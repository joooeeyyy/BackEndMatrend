package com.tekbridge.alertapp;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.InputStream;

@Configuration
public class GoogleCredentialsConfig {

    @Bean
    public ImageAnnotatorSettings imageAnnotatorSettings() throws Exception {

        // Read the GOOGLE_APPLICATION_CREDENTIALS env var set in Railway
        InputStream credentialsStream = getClass()
                .getClassLoader()
                .getResourceAsStream("service-account.json");

        if (credentialsStream == null) {
            throw new IllegalStateException("service-account.json not found in resources!");
        }

        GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);


        CredentialsProvider credentialsProvider = FixedCredentialsProvider.create(credentials);

        return ImageAnnotatorSettings.newBuilder()
                .setCredentialsProvider(credentialsProvider)
                .build();
    }
}
