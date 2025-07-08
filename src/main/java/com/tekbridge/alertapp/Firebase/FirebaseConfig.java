package com.tekbridge.alertapp.Firebase;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class FirebaseConfig {


    @Bean
    public Firestore firestore() throws IOException {

        try (InputStream serviceAccount =
            new FileInputStream("/tmp/service-account.json")) {
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setStorageBucket("matrend-c45ab.firebasestorage.app")
                        .build();

                FirebaseApp.initializeApp(options);
            }

        }
        return FirestoreClient.getFirestore();
    }

    @Bean
    public Storage storage() throws Exception {

        try (InputStream serviceAccount =
                     new FileInputStream("/tmp/service-account.json")) {

            return StorageOptions.newBuilder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build()
                    .getService();
        }
    }

    @Bean
    public FirebaseApp initializeFirebase() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {

            // Read JSON credentials from environment variable
            String json = System.getenv("GOOGLE_APPLICATION_CREDENTIALS_JSON");
            if (json == null || json.isEmpty()) {
                throw new IllegalStateException("GOOGLE_APPLICATION_CREDENTIALS_JSON env var is not set!");
            }

            // Write to /tmp/service-account.json
            Path tempPath = Paths.get("/tmp/service-account.json");
            Files.writeString(tempPath, json);

            try (InputStream serviceAccount = new FileInputStream(tempPath.toFile())) {

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setStorageBucket("matrend-c45ab.firebasestorage.app") // replace with your actual bucket
                        .build();

                return FirebaseApp.initializeApp(options);
            }

        } else {
            return FirebaseApp.getInstance(); // reuse existing instance
        }
    }

}
