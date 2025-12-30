package com.example.hoccode.service.impl;

import com.example.hoccode.entity.PremiumUser;
import com.example.hoccode.repository.PremiumUserRepository;
import com.example.hoccode.service.IPremiumUserService;
import com.example.hoccode.util.SecurityUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PremiumUserService implements IPremiumUserService {
    PremiumUserRepository premiumUserRepository;
    SecurityUtil securityUtil;

    @Override
    public Boolean isPremium(){
        PremiumUser p = premiumUserRepository.findFirstByEmail(securityUtil.getCurrentUserLogin().get());
        return p != null;
    }
}
