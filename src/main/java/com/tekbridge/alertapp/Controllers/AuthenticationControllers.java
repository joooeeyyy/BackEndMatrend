package com.tekbridge.alertapp.Controllers;

import com.tekbridge.alertapp.Models.UserModel;
//import com.tekbridge.alertapp.Servcies.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/api/v1/auth")
//public class AuthenticationControllers {
//
//    private final AuthenticationService authenticationService;
//
//    @Autowired
//    public AuthenticationControllers(AuthenticationService authenticationService) {
//        this.authenticationService = authenticationService;
//    }
//
//    @PostMapping("/sign-up")
//    ResponseEntity<UserModel> createUser(@RequestBody UserModel userModel){
//       System.out.println("Sign Up Triggered");
//       return ResponseEntity.ok(authenticationService.createUser(userModel));
//    }
//
//
//}
