package com.example.hoccode.service.impl;

import com.example.hoccode.config.property.CodeRunnerApiProperties;
import com.example.hoccode.dto.request.call.CodeRunnerRequest;
import com.example.hoccode.dto.request.call.TestCaseSubmissionResponse;
import com.example.hoccode.dto.response.call.CodeRunnerResponse;
import com.example.hoccode.service.ICodeRunnerService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CodeRunnerService implements ICodeRunnerService {
    CodeRunnerApiProperties config;

    @Override
    public void execute(CodeRunnerRequest request) {
        WebClient client = WebClient.builder()
                .baseUrl(config.getProtocol() + "://" + config.getHost() + ":" + config.getPort())
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer
                                .defaultCodecs()
                                .maxInMemorySize(10 * 1024 * 1024))
                        .build())
                .build();

        client.post()
                .uri("/runcode")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe();
    }

    @Override
    public List<TestCaseSubmissionResponse> viewResults(String submissionId) {
        WebClient client = WebClient.builder()
                .baseUrl(config.getProtocol() + "://" + config.getHost() + ":" + config.getPort())
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer
                                .defaultCodecs()
                                .maxInMemorySize(10 * 1024 * 1024))
                        .build())
                .build();

        return client.get()
                .uri("/submission/"+submissionId+"/results")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<TestCaseSubmissionResponse>>() {})
                .block();
    }

    @Override
    public CodeRunnerResponse run(CodeRunnerRequest request) {
        WebClient client = WebClient.builder()
                .baseUrl(config.getProtocol() + "://" + config.getHost() + ":" + config.getPort())
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer
                                .defaultCodecs()
                                .maxInMemorySize(10 * 1024 * 1024))
                        .build())
                .build();
        return client.post()
                .uri("/run")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(CodeRunnerResponse.class)
                .block();
    }
    /*
        GET http://localhost:1337/submission/123adsd1334/results
        POST http://localhost:1337/runcode

    */
}
