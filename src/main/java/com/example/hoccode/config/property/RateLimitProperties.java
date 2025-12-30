package com.example.hoccode.config.property;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.checkerframework.checker.units.qual.N;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "app.rate-limit")
public class RateLimitProperties {
    private List<EnforcedEndpoint> enforcedEndpoints = new ArrayList<>();

    public List<EnforcedEndpoint> getEnforcedEndpoints() {
        return enforcedEndpoints;
    }

    public void setEnforcedEndpoints(List<EnforcedEndpoint> enforcedEndpoints) {
        this.enforcedEndpoints = enforcedEndpoints;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class EnforcedEndpoint {
        private String method;
        private String path;
        private String limitKey;
    }
}