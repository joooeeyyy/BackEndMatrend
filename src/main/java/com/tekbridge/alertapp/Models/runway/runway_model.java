package com.tekbridge.alertapp.Models.runway;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty; // Optional, but good practice for explicit mapping

public class runway_model {

    private String promptText;
    private String ratio;
    private long seed; // Using long for seed as it's a large number
    private String model;

//    @JsonProperty("referenceImages") // Explicitly maps to JSON key "referenceImages"
//    private List<ReferenceImage> referenceImages;

    @JsonProperty("contentModeration") // Explicitly maps to JSON key "contentModeration"
    private ContentModeration contentModeration;

    // Default constructor (often needed by JSON deserializers)
    public runway_model() {
    }

    // Constructor with all fields (optional, but can be convenient)
    public runway_model(String promptText, String ratio, long seed, String model,
                              ContentModeration contentModeration) {
        this.promptText = promptText;
        this.ratio = ratio;
        this.seed = seed;
        this.model = model;
        //this.referenceImages = referenceImages;
        this.contentModeration = contentModeration;
    }

    // Getters and Setters for all fields
    // (Lombok can also be used to auto-generate these: @Getter, @Setter, @NoArgsConstructor, @AllArgsConstructor)

    public String getPromptText() {
        return promptText;
    }

    public void setPromptText(String promptText) {
        this.promptText = promptText;
    }

    public String getRatio() {
        return ratio;
    }

    public void setRatio(String ratio) {
        this.ratio = ratio;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

//    public List<ReferenceImage> getReferenceImages() {
//        return referenceImages;
//    }

//    public void setReferenceImages(List<ReferenceImage> referenceImages) {
//        this.referenceImages = referenceImages;
//    }

    public ContentModeration getContentModeration() {
        return contentModeration;
    }

    public void setContentModeration(ContentModeration contentModeration) {
        this.contentModeration = contentModeration;
    }

    // --- Nested Classes for JSON structure ---

    public static class ReferenceImage {
        private String uri;
        private String tag;

        public ReferenceImage() {
        }

        public ReferenceImage(String uri, String tag) {
            this.uri = uri;
            this.tag = tag;
        }

        public String getUri() {
            return uri;
        }

        public void setUri(String uri) {
            this.uri = uri;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }
    }

    public static class ContentModeration {
        private String publicFigureThreshold;

        public ContentModeration() {
        }

        public ContentModeration(String publicFigureThreshold) {
            this.publicFigureThreshold = publicFigureThreshold;
        }

        public String getPublicFigureThreshold() {
            return publicFigureThreshold;
        }

        public void setPublicFigureThreshold(String publicFigureThreshold) {
            this.publicFigureThreshold = publicFigureThreshold;
        }
    }

    @Override
    public String toString() {
        return "runway_model{" +
                "promptText='" + promptText + '\'' +
                ", ratio='" + ratio + '\'' +
                ", seed=" + seed +
                ", model='" + model + '\'' +
                ", contentModeration=" + contentModeration +
                '}';
    }


//    @Override
//    public String toString() {
//        return "RunwayModelRequest{" +
//                "promptText='" + promptText + '\'' +
//                ", ratio='" + ratio + '\'' +
//                ", seed=" + seed +
//                ", model='" + model + '\'' +
//                ", referenceImages=" + referenceImages +
//                ", contentModeration=" + contentModeration +
//                '}';
//    }
}
