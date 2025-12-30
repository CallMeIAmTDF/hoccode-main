package com.example.hoccode.repository;

import com.example.hoccode.entity.PremiumUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PremiumUserRepository extends JpaRepository<PremiumUser, Long> {
    PremiumUser findFirstByEmail(String email);
}
