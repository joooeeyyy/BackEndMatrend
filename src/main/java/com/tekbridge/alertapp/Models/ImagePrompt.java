package com.tekbridge.alertapp.Models;

public class ImagePrompt {
    String nameOfCompany;

    //Not more than 10 words
    String shortDescriptionOfPurpose;

    String businessSlogan;

    boolean worldWideAudience;

    String describeYourCustomers;

    String whatYouDo;

    String typeOfPictureYouWantInPicture;


    String phoneNumber;

    String email;

    String videoString;



    public ImagePrompt(String nameOfCompany, String shortDescriptionOfPurpose, String businessSlogan, boolean worldWideAudience, String describeYourCustomers,
                       String whatYouDo, String typeOfPictureYouWantInPicture , String phoneNumber, String email , String videoString
    ) {
        this.phoneNumber = phoneNumber;
        this.email =email;
        this.nameOfCompany = nameOfCompany;
        this.shortDescriptionOfPurpose = shortDescriptionOfPurpose;
        this.businessSlogan = businessSlogan;
        this.worldWideAudience = worldWideAudience;
        this.describeYourCustomers = describeYourCustomers;
        this.whatYouDo = whatYouDo;
        this.typeOfPictureYouWantInPicture = typeOfPictureYouWantInPicture;
        this.videoString = videoString;
    }

    public String getVideoString() {
        return videoString;
    }

    public void setVideoString(String videoString) {
        this.videoString = videoString;
    }

    public String getWhatYouDo() {
        return whatYouDo;
    }

    public String getTypeOfPictureYouWantInPicture() {
        return typeOfPictureYouWantInPicture;
    }

    public String getNameOfCompany() {
        return nameOfCompany;
    }

    public void setNameOfCompany(String nameOfCompany) {
        this.nameOfCompany = nameOfCompany;
    }

    public String getShortDescriptionOfPurpose() {
        return shortDescriptionOfPurpose;
    }

    public void setShortDescriptionOfPurpose(String shortDescriptionOfPurpose) {
        this.shortDescriptionOfPurpose = shortDescriptionOfPurpose;
    }

    public String getBusinessSlogan() {
        return businessSlogan;
    }

    public void setBusinessSlogan(String businessSlogan) {
        this.businessSlogan = businessSlogan;
    }

    public boolean isWorldWideAudience() {
        return worldWideAudience;
    }

    public void setWorldWideAudience(boolean worldWideAudience) {
        this.worldWideAudience = worldWideAudience;
    }

    public String getDescribeYourCustomers() {
        return describeYourCustomers;
    }

    public void setDescribeYourCustomers(String describeYourCustomers) {
        this.describeYourCustomers = describeYourCustomers;
    }

    public void setWhatYouDo(String whatYouDo) {
        this.whatYouDo = whatYouDo;
    }

    public void setTypeOfPictureYouWantInPicture(String typeOfPictureYouWantInPicture) {
        this.typeOfPictureYouWantInPicture = typeOfPictureYouWantInPicture;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
