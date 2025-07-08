package com.tekbridge.alertapp.Servcies;


import com.tekbridge.alertapp.Firebase.MediaDisplay;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class FirebaseUploader {

    @Value("${firebase.storage.bucket}")
    private String firebaseBucket;

    public String uploadVideoToFirebaseFromUrlAndUpdate(String videoUrl, MediaDisplay media) throws Exception {
        // 1️⃣ Download video to temp file
        Path tempFile = downloadVideoToTempFile(videoUrl, media.getVideoId());

        // 2️⃣ Upload to Firebase Storage
        String firebasePath = "videos/video_" + System.currentTimeMillis() + ".mp4";

        Bucket bucket = StorageClient.getInstance().bucket(firebaseBucket);
        Blob blob = bucket.create(firebasePath, Files.newInputStream(tempFile), "video/mp4");

        // Optional: make the file public
        blob.createAcl(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));

        // Clean up temp file
        Files.deleteIfExists(tempFile);

        // Return public URL
        return blob.getMediaLink(); // or construct: `https://storage.googleapis.com/{bucket}/{path}`
    }

    private Path downloadVideoToTempFile(String videoUrl, Long videoId) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(videoUrl))
                .build();

        HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() != 200) {
            throw new IOException("Failed to download video, HTTP " + response.statusCode());
        }

        Path tempFile = Files.createTempFile("video_" + videoId + "_", ".mp4");
        try (InputStream in = response.body();
             OutputStream out = Files.newOutputStream(tempFile)) {
            in.transferTo(out);
        }

        return tempFile;
    }
}
