package com.tekbridge.alertapp.Servcies;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.cloud.vision.v1.*;
import com.google.cloud.vision.v1.Image;
import com.google.protobuf.ByteString;
import com.tekbridge.alertapp.Models.WordBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Service
public class TextDetectionService {

    ImageAnnotatorSettings annotatorSettings;

    @Autowired
    TextDetectionService(ImageAnnotatorSettings settings){
        this.annotatorSettings = settings;
    }
    public BoundPolyAndDescription detectTextFromPublicUrl(String imageUrl) throws Exception {
        ImageSource imgSource = ImageSource.newBuilder().setImageUri(imageUrl).build();
        Image image = Image.newBuilder().setSource(imgSource).build();
        Feature feature = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();

        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addFeatures(feature)
                .setImage(image)
                .build();

        try (ImageAnnotatorClient client = ImageAnnotatorClient.create(annotatorSettings)) {
            AnnotateImageResponse response = client
                    .batchAnnotateImages(List.of(request))
                    .getResponses(0);

            if (response.hasError()) {
                throw new RuntimeException("API Error: " + response.getError().getMessage());
            }



            return new BoundPolyAndDescription(
                    response.getTextAnnotationsList().isEmpty()?true:false,
                    response.getTextAnnotationsList().isEmpty()?"No text found":response.getTextAnnotationsList().get(0).getDescription(),
                    response.getTextAnnotationsList().isEmpty()?null:response.getTextAnnotationsList().get(0).getBoundingPoly()
            );

//            return response.getTextAnnotationsList().isEmpty()
//                    ? "No text found"
//                    : response.getTextAnnotationsList().get(0).getDescription();
        }
    }

    public List<WordBox> detectWordBoundingBoxes(String imageUrl) throws Exception {
        ImageSource imgSource = ImageSource.newBuilder().setImageUri(imageUrl).build();
        Image image = Image.newBuilder().setSource(imgSource).build();
        Feature feature = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();

        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addFeatures(feature)
                .setImage(image)
                .build();

        try (ImageAnnotatorClient client = ImageAnnotatorClient.create(annotatorSettings)) {
            AnnotateImageResponse response = client
                    .batchAnnotateImages(List.of(request))
                    .getResponses(0);

            if (response.hasError()) {
                throw new RuntimeException("API Error: " + response.getError().getMessage());
            }

            List<EntityAnnotation> annotations = response.getTextAnnotationsList();

            List<WordBox> wordBoxes = new ArrayList<>();

            // Skip index 0 (full text block), start from 1
            for (int i = 1; i < annotations.size(); i++) {
                EntityAnnotation word = annotations.get(i);
                BoundingPoly poly = word.getBoundingPoly();

                if (poly.getVerticesCount() >= 2) {
                    int x = poly.getVertices(0).getX();
                    int y = poly.getVertices(0).getY();
                    int width = poly.getVertices(1).getX() - x;
                    int height = poly.getVertices(2).getY() - y;

                    wordBoxes.add(new WordBox(word.getDescription(), new Rectangle(x, y, width, height)));
                }
            }

            return wordBoxes;
        }
    }

}


