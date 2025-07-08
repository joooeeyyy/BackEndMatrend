package com.tekbridge.alertapp.OpenAi.Controllers;

import com.tekbridge.alertapp.Servcies.MediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

//@RestController
//@RequestMapping("/media")
public class MediaController {}
//
//    private final MediaService mediaService;
//
//    @Autowired
//    public MediaController(MediaService mediaService) {
//        this.mediaService = mediaService;
//    }
//
//    @PostMapping("/refresh/{uid}")
//    public org.springframework.http.ResponseEntity<String> refreshMediaStatuses(@PathVariable String uid) {
//        try {
//            mediaService.refreshMediaStatuses(uid);
//            return ResponseEntity.ok("Content refreshed successfully.");
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Failed to refresh: " + e.getMessage());
//        }
//    }
//}
//
