package com.tekbridge.alertapp.OpenAi.Controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import java.io.ByteArrayOutputStream;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Controller
@RequestMapping("/api/caption")
public class ImageCaptionController {

    public static void drawWrappedCaption(Graphics2D g, String text, int x, int y, int maxWidth, int padding, int lineHeight) {
        FontMetrics fm = g.getFontMetrics();
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        java.util.List<String> lines = new java.util.ArrayList<>();

        for (String word : words) {
            String testLine = line.length() == 0 ? word : line + " " + word;
            int testWidth = fm.stringWidth(testLine);

            if (testWidth > maxWidth && line.length() > 0) {
                lines.add(line.toString());
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(testLine);
            }
        }
        if (line.length() > 0) {
            lines.add(line.toString());
        }

        // Calculate box size
        int boxWidth = 0;
        for (String l : lines) {
            int lineWidth = fm.stringWidth(l);
            if (lineWidth > boxWidth) boxWidth = lineWidth;
        }
        boxWidth += padding * 2;
        int boxHeight = lineHeight * lines.size() + padding * 2;

        // Draw transparent rounded rectangle box behind text
        g.setColor(new Color(0, 0, 0, 128));
        g.fillRoundRect(x - padding, y - fm.getAscent() - padding, boxWidth, boxHeight, 20, 20);

        // Draw each line of text
        g.setColor(Color.WHITE);
        int curY = y;
        for (String l : lines) {
            g.drawString(l, x, curY);
            curY += lineHeight;
        }
    }



    @GetMapping("/link")
    public ResponseEntity<byte[]> captionImage(@RequestParam byte[] imageBytes) {
        try {

            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"image.png\"")
                    .header("Content-Type", "image/png")
                    .body(imageBytes);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("Content-Type", "image/png")  // still respond with image/png so client knows
                    .body(new byte[0]);  // e
        }
    }

    String removeCaption (String inputText){
        return inputText.replace("\"", "");
    }

    public byte[] downloadImage(String imageUrl) throws IOException {
        try (InputStream in = new URL(imageUrl).openStream()) {
            return in.readAllBytes();
        }
    }

    public String generateCaption(byte[] imageBytes, String openAiApiKey) throws IOException, UnsupportedEncodingException {
        // Prepare HTTP Client
        HttpClient httpClient = HttpClients.createDefault();

        // Convert image to Base64
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);

        // Build request JSON
        String requestBody = """
    {
      "model": "gpt-4o",
      "messages": [
        {
          "role": "user",
          "content": [
            {
              "type": "text",
              "text": "Generate a short caption for this image for customers of the business"
            },
            {
              "type": "image_url",
              "image_url": {
                "url": "data:image/jpeg;base64,%s"
              }
            }
          ]
        }
      ],
      "max_tokens": 100
    }
    """.formatted(base64Image);

        HttpPost request = new HttpPost("https://api.openai.com/v1/chat/completions");
        request.setHeader("Authorization", "Bearer " + openAiApiKey);
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(requestBody));

        // Execute request
        try (CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(request)) {
            String jsonResponse = new String(response.getEntity().getContent().readAllBytes());
            // Parse the response
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            return rootNode
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
