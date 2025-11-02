package com.example.hoccode.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {


    private WebClient createWebClient(String baseUrl) {
        HttpClient httpClient = HttpClient.create()
                .protocol(HttpProtocol.HTTP11);
        return WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.USER_AGENT, "SpringBoot-WebClient")
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> {
                            configurer.defaultCodecs().maxInMemorySize(50 * 1024 * 1024);
                            configurer.defaultCodecs().enableLoggingRequestDetails(true);
                        })
                        .build())
                .build();
    }

    @Bean
    public WebClient plagiarismClient(
            @Value("${plagiarism-api.protocol}") String protocol,
            @Value("${plagiarism-api.host}") String host,
            @Value("${plagiarism-api.port}") int port) {
        return createWebClient(String.format("%s://%s:%d", protocol, host, port));
    }

    @Bean
    public WebClient gptClient(
            @Value("${gpt-api.protocol}") String protocol,
            @Value("${gpt-api.host}") String host,
            @Value("${gpt-api.port}") int port) {
        return createWebClient(String.format("%s://%s:%d", protocol, host, port));
    }


//    @Bean
//    public WebClient gptClient(@Value("${gpt-api.protocol}") String protocol,
//                                      @Value("${gpt-api.host}") String host,
//                                      @Value("${gpt-api.port}") int port) {
//        HttpClient httpClient = HttpClient.create()
//                .protocol(HttpProtocol.HTTP11);
//        return WebClient.builder()
//                .baseUrl(protocol + "://" + host + ":" + port)
//                .clientConnector(new ReactorClientHttpConnector(httpClient))
//                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
//                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
//                .exchangeStrategies(ExchangeStrategies.builder()
//                        .codecs(configurer -> {
//                            configurer.defaultCodecs().maxInMemorySize(50 * 1024 * 1024);
//                            configurer.defaultCodecs().enableLoggingRequestDetails(true);
//                        })
//                        .build())
//                .defaultHeader(HttpHeaders.USER_AGENT, "SpringBoot-WebClient")
//                .build();
//    }
}