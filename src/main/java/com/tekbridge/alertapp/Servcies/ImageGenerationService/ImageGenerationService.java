package com.tekbridge.alertapp.Servcies.ImageGenerationService;

import com.tekbridge.alertapp.Models.ImagePrompt;
import org.springframework.stereotype.Service;

@Service
public class ImageGenerationService implements ImageGenerationInterface {

    @Override
    public String getNameOfCompany() {
        return nameOfCompany;
    }

    @Override
    public String getShortDescriptionOfPurpose() {
        return descriptionOfPurpose;
    }

    @Override
    public String getBusinessSlogan() {
        return businessSlogan;
    }

    @Override
    public Boolean getIsWorldWideAudience() {
        return isWorldWideAudience;
    }

    @Override
    public String getCustomerDescription() {
        return customerDescription;
    }

    @Override
    public String getWhatYouDo() {
        return whatYouDo;
    }

    @Override
    public String getPictureTypeinBak() {
        return pictureInBackGround;
    }

    private String nameOfCompany;
    private String businessSlogan;
    private String customerDescription;
    private Boolean isWorldWideAudience;
    private String descriptionOfPurpose;
    private String whatYouDo;
    private String pictureInBackGround;




    @Override
    public String getDetailsFromImagePrompt(ImagePrompt imagePrompt) {

         nameOfCompany = imagePrompt.getNameOfCompany();
         businessSlogan = imagePrompt.getBusinessSlogan();
         customerDescription = imagePrompt.getDescribeYourCustomers();
         isWorldWideAudience = imagePrompt.isWorldWideAudience();
         descriptionOfPurpose = imagePrompt.getShortDescriptionOfPurpose();
         whatYouDo = imagePrompt.getWhatYouDo();
         pictureInBackGround = imagePrompt.getTypeOfPictureYouWantInPicture();
         String worldwide = "for worldwide ";
         if(!isWorldWideAudience){
             worldwide="";
         }
         //First Sentence(WhatYouDo)= "Cleaning warehouse and industrial plant"
         //Second Sentence(PICTURE IN BACKGROUND-??) = LION HEAD IN LOGO PAGE
         //Name of the company (commander cleaners)

        //My Business is Hair Protector maintaniance , Say the name TIARA HAIR STUDIOS in various part of the video , with pictures indicating my services

        return whatYouDo+", "+worldwide+ "Single "+pictureInBackGround+". "+"a very plain no textual display,no text element. Avoid including any text or textual elements in the page.make a realistic poster picture for my business advert using a canvas template sample , appealing to a social media users .";

        //return  "Generate a caption for a poster with a  background for the text. The background should be beautiful and appealing to any kind of user looking at the poster";

       // return "Cleaning warehouse and Industrial plant lion head in logo page only text display Overlay the text  'Commander Cleaners' in a bold handwritten font centered at the bottom of the image . Avoid including any text or textual elements in the page. more realistic  ";

    }

}
