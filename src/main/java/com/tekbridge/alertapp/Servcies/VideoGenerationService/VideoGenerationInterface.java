package com.tekbridge.alertapp.Servcies.VideoGenerationService;

import com.tekbridge.alertapp.Models.ImagePrompt;
import com.tekbridge.alertapp.Models.VideoPrompt;

public interface VideoGenerationInterface {
    String getDetailsFromVideoPrompt(VideoPrompt videoPrompt);
}
