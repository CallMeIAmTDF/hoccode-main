package com.example.hoccode.config.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {
    private Map<String, String> enforcedEndpoints = new HashMap<>();

    public Map<String, String> getEnforcedEndpoints() {
        return enforcedEndpoints;
    }

    public void setEnforcedEndpoints(Map<String, String> enforcedEndpoints) {
        this.enforcedEndpoints = enforcedEndpoints;
    }
}