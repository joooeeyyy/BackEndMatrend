package com.tekbridge.alertapp.Models;
import jakarta.persistence.*;

@Table
@Entity
public class UserModel {

    @Id
    @SequenceGenerator(
            name = "user_sequence",
            sequenceName = "user_sequence",
            allocationSize = 2
    )
    @GeneratedValue(
            generator = "user_sequence",
            strategy = GenerationType.SEQUENCE
    )
    private Long id;

    @Column
    private String userName;

    @Column
    private String email;

    @Column
    private String profilePicture;

    @Column
    private String coverPhoto;

    @Column
    private String password;

    @Column
    private Boolean isAdmin;

    public UserModel() {
    }

    public UserModel(String userName, String email, String profilePicture, String coverPhoto,
                     String password, Boolean isAdmin) {
        this.userName = userName;
        this.email = email;
        this.profilePicture = profilePicture;
        this.coverPhoto = coverPhoto;
        this.password = password;
        this.isAdmin = isAdmin;
    }

    public UserModel(Long id, String userName,
                     String email, String profilePicture,
                     String coverPhoto,
                     String password, Boolean isAdmin) {
        this.id = id;
        this.userName = userName;
        this.email = email;
        this.profilePicture = profilePicture;
        this.coverPhoto = coverPhoto;
        this.password = password;
        this.isAdmin = isAdmin;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getCoverPhoto() {
        return coverPhoto;
    }

    public void setCoverPhoto(String coverPhoto) {
        this.coverPhoto = coverPhoto;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getAdmin() {
        return isAdmin;
    }

    public void setAdmin(Boolean admin) {
        isAdmin = admin;
    }
}
