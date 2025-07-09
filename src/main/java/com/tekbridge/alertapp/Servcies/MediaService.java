package com.tekbridge.alertapp.Servcies;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.tekbridge.alertapp.Firebase.MediaDisplay;
import com.tekbridge.alertapp.Models.VideoStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MediaService {

    @Autowired
    private Firestore firestore;

    @Autowired
    private VideoStatusService videoStatusService;

    @Autowired
    private FirebaseUploader firebaseUploader;

    public void refreshMediaStatuses(String uid) throws Exception {
        DocumentReference userDoc = firestore.collection("users").document(uid);
        DocumentSnapshot snapshot = userDoc.get().get();

        if (!snapshot.exists()) {
            throw new IllegalStateException("User document not found.");
        }

        List<Map<String, Object>> mediaListRaw =
                (List<Map<String, Object>>) snapshot.get("media");
        if (mediaListRaw == null) mediaListRaw = new ArrayList<>();

        List<Map<String, Object>> updatedList = new ArrayList<>();

        for (Map<String, Object> mediaMap : mediaListRaw) {
            MediaDisplay media = MediaDisplay.fromMap(mediaMap);

            if (media.isStatusPending() && !media.isUploading()) {
                media.setIsUploading(true);
                VideoStatus updatedStatus = videoStatusService.fetchUpdatedVideoInfo(
                        media.getVideoId(), media);

                if ("completed".equals(updatedStatus.getStatus())) {
                    String firebaseUrl = firebaseUploader.uploadVideoToFirebaseFromUrlAndUpdate(
                            updatedStatus.getUrl(), media);

                    media.setStatusPending(false);
                    media.setUploading(false);
                    media.setVideoUrl(firebaseUrl);

                    updatedList.add(media.toMap());
                } else {
                    updatedList.add(media.toMap());
                }

            } else {
                updatedList.add(media.toMap());
            }
        }

        userDoc.update("media", updatedList);
    }
}

