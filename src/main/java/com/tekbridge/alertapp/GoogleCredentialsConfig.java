package com.tekbridge.alertapp;

import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class GoogleCredentialsConfig {

//    @PostConstruct
//    public void init() throws Exception {
//        InputStream resourceStream = getClass()
//                .getClassLoader()
//                .getResourceAsStream("serviceAccountKey.json");
//
//        if (resourceStream == null) {
//            throw new IllegalStateException("serviceAccountKey.json not found in resources folder!");
//        }
//
//        String tmpDir = System.getProperty("java.io.tmpdir");
//        Path tmpPath = Paths.get(tmpDir, "service-account.json");
//
//        Files.copy(resourceStream, tmpPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
//
//        System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", tmpPath.toString());
//        System.out.println("✅ Loaded serviceAccountKey.json from resources and set system property to: " + tmpPath);
//    }

    // At startup, write the JSON from env var to a file
    @PostConstruct
    public void init() throws Exception {
        String json = System.getenv("GOOGLE_APPLICATION_CREDENTIALS_JSON");
        if (json == null || json.isEmpty()) {
            throw new IllegalStateException("GOOGLE_APPLICATION_CREDENTIALS_JSON env var not set!");
        }

        Files.write(Paths.get("/tmp/service-account.json"), json.getBytes());
        System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", "/tmp/service-account.json");
        System.out.println("✅ Wrote service-account.json to /tmp and set system property.");
    }

    @Bean
    public ImageAnnotatorSettings imageAnnotatorSettings() throws Exception {
        try (InputStream credentialsStream =
                     new FileInputStream("/tmp/service-account.json")) {

            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);
            CredentialsProvider credentialsProvider = FixedCredentialsProvider.create(credentials);

            return ImageAnnotatorSettings.newBuilder()
                    .setCredentialsProvider(credentialsProvider)
                    .build();
        }
    }

//    @Bean
//    public ImageAnnotatorSettings imageAnnotatorSettings() throws Exception {
//        try (InputStream credentialsStream =
//                     getClass().getClassLoader().getResourceAsStream("serviceAccountKey.json")) {
//
//            if (credentialsStream == null) {
//                throw new IllegalStateException("serviceAccountKey.json not found in resources folder!");
//            }
//
//            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);
//            CredentialsProvider credentialsProvider = FixedCredentialsProvider.create(credentials);
//
//            return ImageAnnotatorSettings.newBuilder()
//                    .setCredentialsProvider(credentialsProvider)
//                    .build();
//        }
//    }

}
