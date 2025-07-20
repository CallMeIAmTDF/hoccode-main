package com.example.hoccode.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "code-runner-api")
@Data
public class CodeRunnerApiProperties {
    String host;
    String port;
}