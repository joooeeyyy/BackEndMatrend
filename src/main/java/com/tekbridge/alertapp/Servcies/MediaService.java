package com.tekbridge.alertapp.Servcies;

import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.database.*;
import com.tekbridge.alertapp.Firebase.MediaDisplay;
import com.tekbridge.alertapp.Models.VideoStatus;
import com.tekbridge.alertapp.Models.runway.SuccessGeneratedContent;
import com.tekbridge.alertapp.Models.runway.SuccessPendingGeneration;
import com.tekbridge.alertapp.runway.runway_image_service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.InterruptedException;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.QueryDocumentSnapshot;

import java.util.HashMap;

import com.google.cloud.firestore.QuerySnapshot;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;

@Service
public class MediaService {

    @Autowired
    private runway_image_service runwayImageService;

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

    CompletableFuture<List<String>> getAllTheUrlsRunWayAndDeleteNode(String videoNode,String userId) throws JsonProcessingException {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("runway_generation").child(userId).child(videoNode);
        CompletableFuture<List<String>> future = new CompletableFuture<>();
        databaseReference.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                List<String> currentList = null;
                Object rawValue = mutableData.getValue();
                if(rawValue == null){
                    currentList = new ArrayList<>();
                }else if(rawValue instanceof List){
                    currentList = new ArrayList<>();
                    try {
                        for (Object item : (List<Object>) rawValue) {
                            if(item instanceof String){
                                Object resultFromVideoStatus = runwayImageService.getIdRunwayAndVerify(item.toString());
                                if(resultFromVideoStatus instanceof SuccessPendingGeneration) {
                                    currentList.add(((SuccessPendingGeneration) resultFromVideoStatus).getId());
                                }

                                if(resultFromVideoStatus instanceof SuccessGeneratedContent) {
                                    currentList.add(((SuccessGeneratedContent) resultFromVideoStatus).getOutput().get(0));
                                }
                            }
                        }
                    }catch (Exception ignored){
                        System.out.println(ignored.getMessage());

                    }
                }
                mutableData.setValue(currentList);
                future.complete(currentList);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                if (databaseError != null) {
                    // logger.error("Firebase transaction failed for path '{}': {}", nodePath, databaseError.getMessage());
                    future.completeExceptionally(databaseError.toException());
                } else if (!b) {
                    // This can happen due to contention if retries are exhausted.
                    //logger.warn("Firebase transaction for path '{}' was not committed. The data may not have been updated.", nodePath);
                    future.completeExceptionally(new RuntimeException("Transaction not committed for path: " ));
                }
            }
        });
        return  future;
    };



    public void refreshMediaStatuses(String userFcmToken, String uid) throws Exception {
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

                    //TODO : When status is completed then get all the url from videoId node and add it to the media nodd, delete that video node
//                    CompletableFuture<List<String>> completableFuture = getAllTheUrlsRunWayAndDeleteNode(String.valueOf(media.getVideoId()),uid);
//                    List<String> resultNewUrls =  completableFuture.get();
//                    media.getPictures().addAll(resultNewUrls);
//
//
//                    DatabaseReference videoNodeRef = FirebaseDatabase.getInstance().getReference("runway_generation").child(uid).child(String.valueOf(media.getVideoId()));// Or your specific node name for videos
//                    videoNodeRef.removeValueAsync(); // Using removeValueAsync for non-blocking operation
//                    System.out.println("Attempting to delete video node: " + media.getVideoId() + " for user: " + uid);
                    //TODO : When status is completed then get all the url from videoId node and add it to the media nodd, delete that video node

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

    public void sendFcmNotification(String userFcmToken) throws InterruptedException, ExecutionException {
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
    public Optional<Map<String, Object>> findUserByVideoId(Long videoId) throws ExecutionException, InterruptedException {
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


    public void processUserMediaByVideoId(String videoId) throws ExecutionException, InterruptedException, Exception {
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
                Long mVideoId = (Long) mediaMap.get("videoId");
                if (videoId.equals(mVideoId)) {
                    return Optional.of(userId);
                }
            }
        }

        return Optional.empty();
    }


}

