package com.tekbridge.alertapp.Servcies;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.tekbridge.alertapp.Firebase.MediaDisplay;
import com.tekbridge.alertapp.Models.VideoStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.lang.InterruptedException;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

@Service
public class MediaService {

    @Autowired
    private Firestore firestore;

    @Autowired
    private VideoStatusService videoStatusService;

    @Autowired
    private FirebaseUploader firebaseUploader;

    private void replaceInList(List<Map<String, Object>> updatedList, MediaDisplay media) {
    for (int i = 0; i < updatedList.size(); i++) {
        Map<String, Object> item = updatedList.get(i);
        if (item.get("videoId").equals(media.getVideoId())) {
            updatedList.set(i, media.toMap());
            return;
        }
    }
    updatedList.add(media.toMap()); // fallback if not found
}

    public void refreshMediaStatuses(String userFcmToken,String uid) throws Exception {
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
               
                VideoStatus updatedStatus = videoStatusService.fetchUpdatedVideoInfo(
                        media.getVideoId(), media);

                if ("completed".equals(updatedStatus.getStatus())) {
                    // mark uploading
                     media.setUploading(true);

// add updated
updatedList.add(media.toMap());
userDoc.update("media", updatedList);

String firebaseUrl = firebaseUploader.uploadVideoToFirebaseFromUrlAndUpdate(
        updatedStatus.getUrl(), media);

media.setStatusPending(false);
media.setVideoUrl(firebaseUrl);

// remove last
if (!updatedList.isEmpty()) {
    updatedList.remove(updatedList.size() - 1);
}

// add updated
updatedList.add(media.toMap());
userDoc.update("media", updatedList);
                    sendFcmNotification(userFcmToken);

                } else {
                    updatedList.add(media.toMap());
                }

            } else {
                updatedList.add(media.toMap());
            }
        }

        userDoc.update("media", updatedList);
    }

    public void sendFcmNotification(String userFcmToken) throws InterruptedException {
    Message message = Message.builder()
        .setToken(userFcmToken)
        .setNotification(Notification.builder()
            .setTitle("Matrend-AI")
            .setBody("Your Content is ready..")
            .build())
        .build();

    
    try {
        String response = FirebaseMessaging.getInstance().sendAsync(message).get();
        System.out.println("✅ FCM sent: " + response);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt(); // restore interrupt status
        System.err.println("❌ FCM send interrupted: " + e.getMessage());
    } 

   }
}

