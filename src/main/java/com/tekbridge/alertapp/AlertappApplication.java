package com.tekbridge.alertapp;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;
import org.opencv.core.Core;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.openai.api.OpenAiImageApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AlertappApplication implements CommandLineRunner {


	public static void main(String[] args) throws IOException {
		SpringApplication.run(AlertappApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		Path uploadDir = Paths.get(System.getProperty("user.dir"), "uploads");
		Files.createDirectories(uploadDir);
		System.out.println("✅ Upload directory created at: " + uploadDir.toAbsolutePath());
	}


	@Bean
	ImageAnnotatorSettings getAnnotatorSetting() throws IOException {
		InputStream credentialsStream = AlertappApplication.class.getClassLoader()
				.getResourceAsStream("service-account.json");

		if (credentialsStream == null) {
			System.out.println("Service Not found Found");
			throw new RuntimeException("Service account JSON not found in resources!");
		}else{
			System.out.println("Service Found");
		}
		GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);

		// Build ImageAnnotatorSettings with credentials
		ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
				.setCredentialsProvider(() -> credentials)
				.build();
		return settings;
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

	@Bean
	public HttpHeaders httpHeaders(@Value("${spring.video.need1}") String need1,
								   @Value("${spring.video.need2}") String need2){
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.set(need1,need2);
		return httpHeaders;
	}

	@Bean
	public OpenAiImageModel imageClient(@Value("${spring.ai.openai.api-key}") String key){
		return new OpenAiImageModel(new OpenAiImageApi(key));
	}


	@Bean
	public HttpFirewall httpFirewall(){
		DefaultHttpFirewall firewall = new DefaultHttpFirewall();
		return firewall;
	}

	@Bean
	public HttpFirewall allowUrlEncodedSlash() {
		StrictHttpFirewall firewall = new StrictHttpFirewall();
		firewall.setAllowUrlEncodedSlash(true); // Allow encoded slashes (%2F)
		firewall.setAllowUrlEncodedDoubleSlash(true); // Allow double slashes
		return firewall;
	}

}
