package com.tekbridge.alertapp.Firebase;

import com.google.cloud.storage.Storage;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.storage.BlobInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;
import com.google.cloud.storage.BlobId;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.DocumentReference;

@Service
public class MediaService {

    private final Firestore firestore;
    private final Storage storage;

    @Autowired
    public MediaService(Firestore firestore, Storage storage) {
        this.firestore = firestore;
        this.storage = storage;
    }

    public void saveGeneratedMedia(String uid, MediaDisplay media) throws Exception {
        DocumentReference userDoc = firestore.collection("users").document(uid);

        Map<String, Object> mediaMap = media.toMap();

        ApiFuture<WriteResult> result = userDoc.update("media", FieldValue.arrayUnion(mediaMap));
        result.get(); // wait
    }

    public void updateMedia(String uid, MediaDisplay updated) throws Exception {
        DocumentReference userDoc = firestore.collection("users").document(uid);

        DocumentSnapshot snapshot = userDoc.get().get();
        List<Map<String, Object>> mediaList = (List<Map<String, Object>>) snapshot.get("media");

        List<Map<String, Object>> updatedList = mediaList.stream()
                .map(m -> {
                    MediaDisplay current = MediaDisplay.fromMap(m);
                    if (current.getVideoId() == updated.getVideoId()) {
                        return updated.toMap();
                    }
                    return m;
                })
                .toList();

        userDoc.update("media", updatedList).get();
    }

    public String uploadVideoToFirebaseFromUrlAndUpdate(String videoUrl, String uid, MediaDisplay media) throws Exception {
        // Download the video from the given URL
        URL url = new URL(videoUrl);
        try (InputStream inputStream = url.openStream()) {

            // Create a unique path in the bucket
            String path = String.format("videos/video_%d.mp4", System.currentTimeMillis());

            // Get the bucket name from FirebaseApp
            String bucketName = FirebaseApp.getInstance().getOptions().getStorageBucket();

            if (bucketName == null || bucketName.isEmpty()) {
                throw new IllegalStateException("Firebase Storage bucket name is not configured.");
            }

            // Build BlobId and BlobInfo
            BlobId blobId = BlobId.of(bucketName, path);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType("video/mp4")
                    .build();

            // Upload the video
            storage.createFrom(blobInfo, inputStream);

            // Build public URL (or generate signed URL if needed)
            String firebaseUrl = String.format("https://storage.googleapis.com/%s/%s", bucketName, path);

            // Update MediaDisplay and Firestore
            media.setVideoUrl(firebaseUrl);
            media.setStatusPending(false);

            updateMedia(uid, media);

            return firebaseUrl;
        }
    }

    public List<MediaDisplay> loadUserMedia(String uid) throws Exception {
        DocumentSnapshot snapshot = firestore.collection("users").document(uid).get().get();

        List<Map<String, Object>> mediaListRaw = (List<Map<String, Object>>) snapshot.get("media");

        if (mediaListRaw == null) return List.of();

        return mediaListRaw.stream()
                .map(MediaDisplay::fromMap)
                .toList();
    }

    public void refreshMediaStatuses(String uid) throws Exception {
        List<MediaDisplay> mediaList = loadUserMedia(uid);

        for (MediaDisplay media : mediaList) {
            if (media.isStatusPending()) {
                StatusOfVideo status = fetchUpdatedVideoInfo(media.getVideoId());

                if ("completed".equals(status.getStatus())) {
                    uploadVideoToFirebaseFromUrlAndUpdate(status.getUrl(), uid, media);
                }
            }
        }
    }

    public StatusOfVideo fetchUpdatedVideoInfo(Long videoId) {
        // 🔷 TODO: call your backend API and map to StatusOfVideo
        return new StatusOfVideo("completed", "https://example.com/video.mp4");
    }
}

