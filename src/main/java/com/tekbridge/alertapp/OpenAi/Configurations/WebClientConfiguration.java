package com.tekbridge.alertapp.OpenAi.Configurations;

//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.client.reactive.ReactorClientHttpConnector;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.netty.http.client.HttpClient;
//
//
//public class WebClientConfiguration {
//    @Bean
//    WebClient.Builder webClientBuilder() {
//        return WebClient.builder()
//                .defaultHeader(HttpHeaders.USER_AGENT, "dallecool")
//                .clientConnector(new ReactorClientHttpConnector(HttpClient.create().proxyWithSystemProperties()));
//    }
//
//    @Bean
//    WebClient webClient(WebClient.Builder clientBuilder) {
//        return clientBuilder.build();
//    }
//}