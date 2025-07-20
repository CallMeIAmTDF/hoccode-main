package com.example.hoccode.config;

import com.example.hoccode.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("auditAwareImpl")
@RequiredArgsConstructor
public class AuditAwareImpl implements AuditorAware<String> {
    private final SecurityUtil securityUtil;

    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.of(securityUtil.getCurrentUserLogin().get());
    }
}