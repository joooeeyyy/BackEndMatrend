package com.tekbridge.alertapp.Servcies.ImageGenerationService;

import com.tekbridge.alertapp.Models.ImagePrompt;

public interface ImageGenerationInterface {

    String getNameOfCompany();
    String getShortDescriptionOfPurpose();
    String getBusinessSlogan();
    Boolean getIsWorldWideAudience();
    String getCustomerDescription();

    String getWhatYouDo();

    String getPictureTypeinBak();

    String getDetailsFromImagePrompt(ImagePrompt imagePrompt);

}
