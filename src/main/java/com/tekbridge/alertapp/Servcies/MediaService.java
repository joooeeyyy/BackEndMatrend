package com.tekbridge.alertapp.Servcies;

import java.util.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.database.*;
import com.google.firebase.database.Transaction;
import com.tekbridge.alertapp.Firebase.MediaDisplay;
import com.tekbridge.alertapp.Models.VideoStatus;
import com.tekbridge.alertapp.Models.runway.SuccessGeneratedContent;
import com.tekbridge.alertapp.Models.runway.SuccessPendingGeneration;
import com.tekbridge.alertapp.runway.runway_image_service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.InterruptedException;

import com.google.api.core.ApiFuture;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
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
//
//    CompletableFuture<List<String>> getAllTheUrlsRunWayAndDeleteNode(String videoNode,String userId) throws JsonProcessingException {
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("runway_generation").child(userId).child(videoNode);
//        CompletableFuture<List<String>> future = new CompletableFuture<>();
//        databaseReference.runTransaction(new Transaction.Handler() {
//            @Override
//            public Transaction.Result doTransaction(MutableData mutableData) {
//                List<String> currentList = null;
//                Object rawValue = mutableData.getValue();
//                if(rawValue == null){
//                    currentList = new ArrayList<>();
//                }else if(rawValue instanceof List){
//                    currentList = new ArrayList<>();
//                    try {
//                        for (Object item : (List<Object>) rawValue) {
//                            if(item instanceof String){
//                                Object resultFromVideoStatus = runwayImageService.getIdRunwayAndVerify(item.toString());
//                                if(resultFromVideoStatus instanceof SuccessPendingGeneration) {
//                                    currentList.add(((SuccessPendingGeneration) resultFromVideoStatus).getId());
//                                }
//
//                                if(resultFromVideoStatus instanceof SuccessGeneratedContent) {
//                                    currentList.add(((SuccessGeneratedContent) resultFromVideoStatus).getOutput().get(0));
//                                }
//                            }
//                        }
//                    }catch (Exception ignored){
//                        System.out.println(ignored.getMessage());
//
//                    }
//                }
//                mutableData.setValue(currentList);
//                future.complete(currentList);
//                return Transaction.success(mutableData);
//            }
//
//            @Override
//            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
//                if (databaseError != null) {
//                    // logger.error("Firebase transaction failed for path '{}': {}", nodePath, databaseError.getMessage());
//                    future.completeExceptionally(databaseError.toException());
//                } else if (!b) {
//                    // This can happen due to contention if retries are exhausted.
//                    //logger.warn("Firebase transaction for path '{}' was not committed. The data may not have been updated.", nodePath);
//                    future.completeExceptionally(new RuntimeException("Transaction not committed for path: " ));
//                }
//            }
//        });
//        return  future;
//    };


    public void deleteRunwayGenerationByUserId(String userId) throws ExecutionException, InterruptedException {
        System.out.println("Deleting runway generation by userId: " + userId);
        Firestore db = FirestoreClient.getFirestore();
        // Get a reference to the specific document using the userId as the document ID
        DocumentReference userDocRef = db.collection("runway_generations").document(userId);

        // Asynchronously delete the document
        ApiFuture<WriteResult> deleteFuture = userDocRef.delete();

        // Optionally, block and wait for the delete operation to complete
        // This is useful if you need to ensure the deletion happened before proceeding
        WriteResult result = deleteFuture.get();

        System.out.println("Document with ID '" + userId + "' deleted successfully at: " + result.getUpdateTime());
        // You can also check if the document existed before deletion,
        // though delete() itself doesn't fail if the document doesn't exist.
        // If you need to confirm it existed, you'd typically do a get() before delete().
    }


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

                    System.out.println("Video status updated successfully. ");
                    //TODO : When status is completed then get all the url from videoId node and add it to the media nodd, delete that video node
                    List<String> videoIdsForUrl = getVideoIdsForUser(uid);
                    System.out.println("Video IDs for url: " + videoIdsForUrl.size());
                    List<String> reVerifiedVideoIds = new ArrayList<>();

                    Object resultFromVideoStatus;

                    for(String videoId : videoIdsForUrl){
                        System.out.println("Video ID: For loop " + videoId);
                        resultFromVideoStatus = runwayImageService.getIdRunwayAndVerify(videoId);
                        System.out.println("Video ID: " + videoId + " resultFromVideoStatus: " + resultFromVideoStatus);
                        if (resultFromVideoStatus instanceof SuccessPendingGeneration) {
                            System.out.println("Video status updated successfully. failed");
                            // myObject is an instance of SuccessPendingGeneration
                            SuccessPendingGeneration PendingObject = (SuccessPendingGeneration) resultFromVideoStatus;
                            // Now you can safely use methods specific to SuccessPendingGeneration
                            String id = PendingObject.getId();
                            reVerifiedVideoIds.add(id);
                        } else if (resultFromVideoStatus instanceof SuccessGeneratedContent) {
                            System.out.println("Video status updated successfully. success");
                            // myObject is an instance of AnotherObjectType
                            SuccessGeneratedContent SuccessObject = (SuccessGeneratedContent) resultFromVideoStatus;
                            String url = SuccessObject.getOutput().get(0);
                            reVerifiedVideoIds.add(url);
                            // Now you can safely use methods specific to AnotherObjectType
                        }
                    }

                     reVerifiedVideoIds.addAll(media.getPictures());
                     media.setPictures(reVerifiedVideoIds);

                     System.out.println("Video IDs for url: " + reVerifiedVideoIds.size());

                    deleteRunwayGenerationByUserId(uid);

                    System.out.println("Video IDs for url: delete" + reVerifiedVideoIds.size());

//                   CompletableFuture<List<String>> completableFuture = getAllTheUrlsRunWayAndDeleteNode(String.valueOf(media.getVideoId()),uid);
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


    public List<String> getVideoIdsForUser(String userId) {
        Firestore db = FirestoreClient.getFirestore();
        // Get a reference to the user's document
        DocumentReference userDocRef = db.collection("runway_generations").document(userId);

        // Asynchronously retrieve the document
        ApiFuture<DocumentSnapshot> future = userDocRef.get();

        try {
            // Block and get the document snapshot
            DocumentSnapshot document = future.get();

            if (document.exists()) {
                // Get the 'video_ids' field from the document
                // It's good practice to check the type and handle potential ClassCastException
                Object videoIdsObject = document.get("video_ids");

                if (videoIdsObject instanceof List) {
                    // Firestore stores arrays as List<Object>. We need to cast carefully.
                    @SuppressWarnings("unchecked") // Suppress warning as we've checked the instance type
                    List<Object> rawVideoIds = (List<Object>) videoIdsObject;

                    // Convert List<Object> to List<String>
                    // This assumes all elements in the 'video_ids' array are indeed strings.
                    // Add error handling if the elements might not be strings.
                    return rawVideoIds.stream()
                            .filter(String.class::isInstance)
                            .map(String.class::cast)
                            .toList();
                } else {
                    System.out.println("Field 'video_ids' is not an array for user: " + userId);
                    return Collections.emptyList();
                }
            } else {
                System.out.println("No such document for user: " + userId);
                return Collections.emptyList();
            }
        } catch (InterruptedException | ExecutionException e) {
            // Handle exceptions (e.g., network issues, permission errors)
            System.err.println("Error fetching document: " + e.getMessage());
            Thread.currentThread().interrupt(); // Restore interruption status
            return Collections.emptyList();
        }
    }

}

