package com.example.hoccode.repository;

import com.example.hoccode.entity.VerificationCode;
import com.example.hoccode.util.constant.VerifyTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerifyCodeRepository extends JpaRepository<VerificationCode, String> {
    boolean existsByEmail(String email);

    Optional<VerificationCode> findFirstByCodeAndType(String code, VerifyTypeEnum type);

    Optional<VerificationCode> findFirstByEmail(String email);
}