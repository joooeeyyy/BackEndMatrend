package com.tekbridge.alertapp.OpenAi.Controllers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.vision.v1.BoundingPoly;
import com.google.cloud.vision.v1.Word;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.tekbridge.alertapp.Firebase.MediaDisplay;
import com.tekbridge.alertapp.Models.ResponseVideoPicture;
import com.tekbridge.alertapp.Models.VideoGenRequestModel;
import com.tekbridge.alertapp.Models.WordBox;
import com.tekbridge.alertapp.Servcies.BoundPolyAndDescription;
import com.tekbridge.alertapp.Servcies.ImageGenerationService.ImageGenerationService;
import com.tekbridge.alertapp.Servcies.TextDetectionService;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.springframework.ai.image.*;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class ImageController {

    private final Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads");
    HttpHeaders httpHeaders;
    RestTemplate restTemplate;
    OpenAiImageModel imageModel;
    ImageGenerationService imageGenerationService;

    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;

    private TextDetectionService detectionService;
    byte[] imageBytes;
    byte[] imageBytes1;
    byte[] imageBytes2;
    byte[] imageBytes3;

    @Autowired
    private Firestore firestore;



    @Autowired
    public ImageController(RestTemplate restTemplate,TextDetectionService detectionService , OpenAiImageModel imageModel,ImageGenerationService imageGenerationService,HttpHeaders httpHeaders) {
        this.imageModel = imageModel;
        this.imageGenerationService=imageGenerationService;
        this.detectionService = detectionService;
        this.restTemplate = restTemplate;
        this.httpHeaders = httpHeaders;
    }

    String generateTestUrl(String parsedPrompt){
        ImageOptions imageOptions = ImageOptionsBuilder
                .builder().model("dall-e-3").build();
        System.out.println("Gone Through two "+imageOptions.toString());
        ImagePrompt imagePrompt = new ImagePrompt(parsedPrompt,imageOptions);
        System.out.println("Gone Through three"+imagePrompt.toString());
        ImageResponse imageResponse = imageModel.call(imagePrompt);
        System.out.println("Gone Through three"+imageResponse.getResult().toString());
        String imageUrl = imageResponse.getResult().getOutput().getUrl();
        System.out.println(imageUrl);
        return  imageUrl;
    }

    byte[] getByteFromEachImage(String imageUrl,com.tekbridge.alertapp.Models.ImagePrompt imagePromptUser) throws Exception {

        BoundPolyAndDescription foundString = detectionService.detectTextFromPublicUrl(imageUrl);
        List<WordBox> boxes = detectionService.detectWordBoundingBoxes(imageUrl);
        byte[] newImage = new byte[0];
        System.out.println("String Result:"+foundString);
        BufferedImage image = ImageIO.read(new URL(imageUrl));
        if(!Objects.equals(foundString.getDescription(), "No text found")){
            newImage = removeAllText(foundString.getBoundingPoly(),image,imagePromptUser.getNameOfCompany(),boxes);
        }else{
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            baos.flush();
            newImage = baos.toByteArray();
            baos.close();
        }

        byte[] imageByte = captionImage(newImage,imagePromptUser.getNameOfCompany(),imagePromptUser.getPhoneNumber()+" ");

        return  imageByte;

    }



    public void saveMediaToFirestore(String uid, MediaDisplay media) throws Exception {
        DocumentReference userDoc = firestore.collection("users").document(uid);

        Map<String, Object> mediaMap = media.toMap();

        ApiFuture<WriteResult> result = userDoc.update("media", FieldValue.arrayUnion(mediaMap));

        result.get(); // wait for completion

        System.out.println("✅ Media saved to Firestore for user: " + uid);
    }
    @PostMapping("/imageGen")
    public ResponseEntity<ResponseVideoPicture> imageGen(
            @RequestHeader("Authorization") String authorization,
            @RequestBody com.tekbridge.alertapp.Models.ImagePrompt imagePromptUser) throws Exception {
        // Remove "Bearer " prefix
        String idToken = authorization.replace("Bearer ", "").trim();

        System.out.println("Token Gotten yesss "+authorization+idToken);
        // Verify the token with Firebase Admin SDK
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);

        // Get the UID of the authenticated user
        String uid = decodedToken.getUid();
        List<String> resultPictures = new ArrayList<>();

        String parsedPrompt = imageGenerationService.getDetailsFromImagePrompt(imagePromptUser);
        String videoDescription = "My Business is " + imagePromptUser.getWhatYouDo() +
                " business, Say the name " + imagePromptUser.getNameOfCompany() +
                " in various part of the video, with pictures indicating my services";
        String instruction = "My Business is " + imagePromptUser.getWhatYouDo();

        System.out.println("Gone Through " + parsedPrompt);

        // generate test URLs
        String[] generatedUrls = {
                generateTestUrl(parsedPrompt),
                generateTestUrl(parsedPrompt),
                generateTestUrl(parsedPrompt),
                generateTestUrl(parsedPrompt)
        };

        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

        // uploads directory
        Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads");
        Files.createDirectories(uploadDir);

        // save each image and build URLs
        for (int i = 0; i < generatedUrls.length; i++) {
            String generatedUrl = generatedUrls[i];
            byte[] imageBytes = getByteFromEachImage(generatedUrl, imagePromptUser);

            String fileName = "test" + (i == 0 ? "" : i) +System.currentTimeMillis()+ ".png";
            Path savePath = uploadDir.resolve(fileName);

            Files.write(savePath, imageBytes);

            System.out.println("✅ Image saved to: " + savePath.toAbsolutePath());

            // Add accessible URL


            resultPictures.add(baseUrl + "/" + fileName);
        }

        resultPictures.forEach(url -> System.out.println("Image loaded, access at: " + url));

        VideoGenRequestModel videoGenRequestModel = new VideoGenRequestModel(
                "Custom", videoDescription, instruction
        );

        Long videoId = requestVideoGeneration(videoGenRequestModel,
                "https://viralapi.vadoo.tv/api/generate_video");

        ResponseVideoPicture responseVideoPicture = new ResponseVideoPicture(
                videoId, resultPictures
        );

        System.out.println("Company Name" +imagePromptUser.getNameOfCompany());
        System.out.println("Video Id "+videoId);
        MediaDisplay media = new MediaDisplay(
                videoId,
                "",
                resultPictures,
                true,
                false,
               uid,
                imagePromptUser.getNameOfCompany()
                );

        saveMediaToFirestore(uid, media);

        return ResponseEntity.ok(responseVideoPicture);
    }

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            Path filePath = uploadDir.resolve(filename).normalize();
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(filePath.toUri());
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);

        } catch (MalformedURLException ex) {
            return ResponseEntity.badRequest().build();
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().build();
        }
    }

    public Long requestVideoGeneration(
            VideoGenRequestModel videoGenRequest,
            String need3Url
    ) throws JsonProcessingException {

        HttpEntity<Object> requestEntity = new HttpEntity<>(videoGenRequest, httpHeaders);

        ResponseEntity<String> response = restTemplate.postForEntity(need3Url, requestEntity, String.class);

        System.out.println("Video Request End Block " + response.getBody());

        String json = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = objectMapper.readValue(json, Map.class);

        return  (Long) map.get("vid");
    }

    private ResponseEntity<String> getGeneratedVideoUrl(Map<String,Object> map){
        System.out.println("getGenerated Video Url Start ");
        Long parseInt = (Long) map.get("vid");
        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUri(URI.create("https://viralapi.vadoo.tv/api/get_video_url"))
                .queryParam("id", parseInt);
        String apiUrlWithParams = builder.toUriString(); //
        HttpEntity<Object> requestEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<Map<String,String>> responseString = restTemplate.exchange(
                apiUrlWithParams,
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<Map<String, String>>() {}
        );
        Map<String ,String> mapResponse = responseString.getBody();
        String Url = mapResponse.get("url");
        System.out.println("getGenerated Video Url End  "+Url);
        return ResponseEntity.ok()
                .body(responseString.getBody().toString()+" Track video creation progress with Id : "+parseInt);


    }

    byte[] removeAllText(
            BoundingPoly poly,
            BufferedImage bufferedImage,
            String nameOfCompany,
            List<WordBox> wordBounds) throws IOException {
        int x = poly.getVertices(0).getX();
        int y = poly.getVertices(0).getY();
        int width = poly.getVertices(1).getX() - poly.getVertices(0).getX();
        int height = poly.getVertices(2).getY() - poly.getVertices(1).getY();

        //System.out.printf("Text: %s at (%d, %d, %d, %d)\n", detectedText, x, y, width, height);

        BufferedImage argbImage = new BufferedImage(
                bufferedImage.getWidth(),
                bufferedImage.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D g2 = argbImage.createGraphics();
        g2.drawImage(bufferedImage, 0, 0, null);
        g2.dispose();

        // Prepare Graphics2D for clearing word regions
        Graphics2D g2d = argbImage.createGraphics();
        g2d.setComposite(AlphaComposite.Clear); // Enables transparent drawing
        for (WordBox wordBox : wordBounds) {
            Rectangle rectangle =wordBox.getBounds();
            g2d.fillRect(rectangle.x, rectangle.y,rectangle.width, rectangle.height);
        }

        g2d.dispose();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(argbImage, "png", baos);
        baos.flush();
        byte[] imageInBytes = baos.toByteArray();
        baos.close();
        return imageInBytes;
    }

    // Serve image as byte[] when URL is hit
//    @GetMapping("/image/test")
//    public ResponseEntity<byte[]> getImage() {
//        if (imageBytes == null) {
//            return ResponseEntity.notFound().build();
//        }
//
//        return ResponseEntity.ok()
//                .contentType(MediaType.IMAGE_PNG)
//                .body(imageBytes);
//    }

    @GetMapping("/image/test1")
    public ResponseEntity<byte[]> getImage4() {
        if (imageBytes1 == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(imageBytes1);
    }

    @GetMapping("/image/test2")
    public ResponseEntity<byte[]> getImage2() {
        if (imageBytes2 == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(imageBytes2);
    }

    @GetMapping("/image/test3")
    public ResponseEntity<byte[]> getImage3() {
        if (imageBytes3 == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(imageBytes3);
    }

    public byte[] downloadImage(String imageUrl) throws IOException {
        try (InputStream in = new URL(imageUrl).openStream()) {
            return in.readAllBytes();
        }
    }

    public String generateCaption(byte[] imageBytes, String openAiApiKey , String phoneNumber , String companyName) throws IOException, UnsupportedEncodingException {
        // Prepare HTTP Client
        HttpClient httpClient = HttpClients.createDefault();

        String base64ImageString = Base64.getEncoder().encodeToString(imageBytes);
        String requestBody = String.format("""
{
  "model": "gpt-4o",
  "messages": [
    {
      "role": "user",
      "content": [
        {
          "type": "text",
          "text": "Generate a good caption for this image for customers of the business %s. Include my phone number %s keep space after phone number in an organized way."
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
""", companyName, phoneNumber, base64ImageString);



        HttpPost request = new HttpPost("https://api.openai.com/v1/chat/completions");
        request.setHeader("Authorization", "Bearer " + openAiApiKey);
        request.setHeader("Content-Type", "application/json");
        request.setEntity(new StringEntity(requestBody));

        // Execute request
        try (CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(request)) {
            String jsonResponse = new String(response.getEntity().getContent().readAllBytes());
            // Parse the response
            System.out.println("Full OpenAI API response: " + jsonResponse);
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(jsonResponse);
            return rootNode
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();
        } catch (IOException e) {
            System.out.println("Full OpenAI API response: " + e.toString());
            throw new RuntimeException(e);
        }
    }

    /**
     * Draws wrapped caption and returns the total height used
     */
    public static int drawWrappedCaption(Graphics2D g, String text, int x, int y, int maxWidth, int padding, int lineHeight) {
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

        // Draw transparent rounded rectangle
        g.setColor(new Color(0, 0, 0, 192));
        g.fillRoundRect(x - padding, y - fm.getAscent() - padding, boxWidth, boxHeight, 20, 20);

        // Draw text lines
        g.setColor(Color.WHITE);
        int curY = y;
        for (String l : lines) {
            g.drawString(l, x, curY);
            curY += lineHeight;
        }

        return boxHeight; // Return height used
    }

    public byte[] captionImage(@RequestParam byte[] imageBytes,String companyName,String phoneNumber) {
        try {
            String caption;
//            byte[] imageBytes = downloadImage(imageUrl);
             caption = generateCaption(imageBytes, openAiApiKey,phoneNumber+" ",companyName);
            if(Objects.equals(caption, "i'm sorry, but I can't help with that.") || Objects.equals(caption, "i'm sorry, but i can't help with that.")){
              caption = generateCaption(imageBytes, openAiApiKey,phoneNumber+" ",companyName);
            }
            if(Objects.equals(caption, "i'm sorry, but I can't help with that.") || Objects.equals(caption, "i'm sorry, but i can't help with that.")){
                caption = "Reach us now for the best of our services , contact us at:"+phoneNumber;
            }
            if(caption.contains("i'm sorry") || caption.contains("I'm sorry") ){
                caption = "Reach us now for the best of our services , contact us at:"+phoneNumber;
            }
            ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
            BufferedImage image = ImageIO.read(bais);
            bais.close();

            Graphics2D g = image.createGraphics();

            // Load logo image
            //BufferedImage logo = ImageIO.read(new File("C:/Joseph/logotest.png"));

            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Font font = new Font("Arial", Font.BOLD, 50);
            g.setFont(font);



            int imageWidth = image.getWidth();
            int x = 50; // margin from left
            int y = image.getHeight() - 300; // place near bottom
            int maxWidth = imageWidth - x - 50; // leave 50px margin on right
            int padding = 10;
            int lineHeight = g.getFontMetrics().getHeight() + 4; // line height with small gap

           // drawWrappedCaption(g, removeCaption(caption), x, y, maxWidth, padding, lineHeight);

            int firstBoxHeight = drawWrappedCaption(g, removeCaption(companyName), x, y, maxWidth, padding, lineHeight);

            Graphics2D gCaption = image.createGraphics();

            gCaption.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Font fontCaption = new Font("Arial", Font.PLAIN, 33);
            gCaption.setFont(fontCaption);
            // Second caption, placed below the first
            int spacingBetweenCaptions = 10;
            int y2 = y + firstBoxHeight + spacingBetweenCaptions;

            // Coordinates where to draw logo
            int xLogo = 50;  // from left
            int yLogo = 50;  // from top

            // Optional: scale logo to desired size (e.g., 100x100 px)
            int logoWidth = 250;
            int logoHeight = 250;

            // Draw the logo on the background
           // gCaption.drawImage(logo, xLogo, yLogo, logoWidth, logoHeight, null);

            drawWrappedCaption(gCaption, removeCaption(caption), x, y2, maxWidth, padding, lineHeight);
            g.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            byte[] imageBytesOut = baos.toByteArray();

            return imageBytesOut;
        } catch (Exception e) {
            System.out.println("This is the exception "+e);
            return new byte[0];  // e
        }
    }



    String removeCaption (String inputText){
        return inputText.replace("\"", "");
    }


}
