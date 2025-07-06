package com.tekbridge.alertapp.Models;

public class VideoGenRequestModel {

    String topic;
    String prompt;

    String custom_instruction;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getCustom_instruction() {
        return custom_instruction;
    }

    public void setCustom_instruction(String custom_instruction) {
        this.custom_instruction = custom_instruction;
    }

    public VideoGenRequestModel(String topic, String prompt, String custom_instruction) {
        this.topic = topic;
        this.prompt = prompt;
        this.custom_instruction = custom_instruction;
    }
}
