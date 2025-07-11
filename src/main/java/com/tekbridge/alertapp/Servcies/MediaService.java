package com.tekbridge.alertapp.Servcies;
import java.util.Optional;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.tekbridge.alertapp.Firebase.MediaDisplay;
import com.tekbridge.alertapp.Models.VideoStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.lang.InterruptedException;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import java.util.concurrent.ExecutionException;
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

    public void sendFcmNotification(String userFcmToken) throws InterruptedException , ExecutionException {
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
    } catch (ExecutionException e) {
            System.err.println("❌ Execution failed: " + e.getCause());
        }

   }

    /**
     * Find user document and media node where media.videoId matches.
     *
     * @param videoId Video ID to search for
     * @return Optional result: Map with keys: "userId" and "media"
     */
    public Optional<Map<String, Object>> findUserByVideoId(String videoId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> usersFuture = firestore.collection("users").get();
        QuerySnapshot usersSnapshot = usersFuture.get();

        for (QueryDocumentSnapshot userDoc : usersSnapshot.getDocuments()) {
            String userId = userDoc.getId();
            List<Map<String, Object>> mediaListRaw =
                    (List<Map<String, Object>>) userDoc.get("media");

            if (mediaListRaw == null) continue;

            for (Map<String, Object> mediaMap : mediaListRaw) {
                String mVideoId = (String) mediaMap.get("videoId");
                if (videoId.equals(mVideoId)) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("userId", userId);
                    result.put("media", mediaMap);
                    return Optional.of(result);
                }
            }
        }

        return Optional.empty();
    }


    public void processUserMediaByVideoId(String videoId) throws ExecutionException, InterruptedException {
        Optional<String> maybeUserId = findUserIdByVideoId(videoId);

        if (maybeUserId.isEmpty()) {
            throw new IllegalStateException("No user found with videoId: " + videoId);
        }

        String userId = maybeUserId.get();
        DocumentReference userDoc = firestore.collection("users").document(userId);
        DocumentSnapshot snapshot = userDoc.get().get();

        if (!snapshot.exists()) {
            throw new IllegalStateException("User document not found: " + userId);
        }

        List<Map<String, Object>> mediaListRaw =
                (List<Map<String, Object>>) snapshot.get("media");
        if (mediaListRaw == null) mediaListRaw = new ArrayList<>();

        List<Map<String, Object>> updatedList = new ArrayList<>();

        for (Map<String, Object> mediaMap : mediaListRaw) {
            MediaDisplay media = MediaDisplay.fromMap(mediaMap);

            if (videoId.equals(media.getVideoId()) && media.isStatusPending() && !media.isUploading()) {

                VideoStatus updatedStatus = videoStatusService.fetchUpdatedVideoInfo(
                        media.getVideoId(), media);

                if ("completed".equals(updatedStatus.getStatus())) {
                    media.setUploading(true);

                    // Add with uploading=true
                    updatedList.add(media.toMap());
                    userDoc.update("media", updatedList);

                    String firebaseUrl = firebaseUploader.uploadVideoToFirebaseFromUrlAndUpdate(
                            updatedStatus.getUrl(), media);

                    media.setStatusPending(false);
                    media.setVideoUrl(firebaseUrl);

                    // Replace last with updated
                    if (!updatedList.isEmpty()) {
                        updatedList.remove(updatedList.size() - 1);
                    }
                    updatedList.add(media.toMap());
                    userDoc.update("media", updatedList);

                    //notificationService.sendFcmNotificationForUser(userId);

                } else {
                    updatedList.add(media.toMap());
                }

            } else {
                updatedList.add(media.toMap());
            }
        }

        userDoc.update("media", updatedList);
    }

    private Optional<String> findUserIdByVideoId(String videoId) throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> usersFuture = firestore.collection("users").get();
        QuerySnapshot usersSnapshot = usersFuture.get();

        for (QueryDocumentSnapshot userDoc : usersSnapshot.getDocuments()) {
            String userId = userDoc.getId();
            List<Map<String, Object>> mediaListRaw =
                    (List<Map<String, Object>>) userDoc.get("media");

            if (mediaListRaw == null) continue;

            for (Map<String, Object> mediaMap : mediaListRaw) {
                String mVideoId = (String) mediaMap.get("videoId");
                if (videoId.equals(mVideoId)) {
                    return Optional.of(userId);
                }
            }
        }

        return Optional.empty();
    }


}

