package com.tekbridge.alertapp.Firebase;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.cloud.StorageClient;
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
    public FirebaseApp firebaseApp() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            String json = System.getenv("Firebase_JSON");
            if (json == null || json.isEmpty()) {
                throw new IllegalStateException("GOOGLE_APPLICATION_CREDENTIALS_JSON env var is not set!");
            }

            Path tempPath = Paths.get("/tmp/serviceaccount.json");
            if (!Files.exists(tempPath)) {
                Files.writeString(tempPath, json);
            }

            try (InputStream serviceAccount = new FileInputStream(tempPath.toFile())) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setStorageBucket("matrend-c45ab.firebasestorage.app") // ✅ correct bucket
                        .build();

                return FirebaseApp.initializeApp(options);
            }
        } else {
            return FirebaseApp.getInstance();
        }
    }

    @Bean
    public Firestore firestore(FirebaseApp firebaseApp) {
        return FirestoreClient.getFirestore(firebaseApp);
    }

    @Bean
    public Storage storage(FirebaseApp firebaseApp) {
        return StorageClient.getInstance(firebaseApp).bucket().getStorage();
    }
}
