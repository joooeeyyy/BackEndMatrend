//package com.tekbridge.alertapp.Servcies;
//
//import com.tekbridge.alertapp.Models.UserModel;
//import com.tekbridge.alertapp.Repository.UserRepository;
//import jakarta.transaction.Transactional;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.util.Optional;
//
//@Service
//public class AuthenticationService {
//
//    private final UserRepository userRepository;
//
//    @Autowired
//    public AuthenticationService(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }
//
//    public UserModel createUser(UserModel model){
//        Optional<UserModel> userModelOptional = userRepository.findUserModelByEmail(model.getEmail());
//        if(userModelOptional.isPresent()) {
//          throw new IllegalArgumentException();
//        }else {
//            userRepository.save(model);
//            return model;
//        }
//    }
//}
