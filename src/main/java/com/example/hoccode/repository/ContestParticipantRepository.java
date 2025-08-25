package com.example.hoccode.repository;

import com.example.hoccode.entity.Contest;
import com.example.hoccode.entity.ContestParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContestParticipantRepository extends JpaRepository<ContestParticipant, Integer> {
    boolean existsByContestIdAndUserId(Integer contestId, String userId);
    void deleteByContestIdAndUserId(Integer contestId, String userId);
    long countByContestId(Integer contestId);
    List<ContestParticipant> findByContest(Contest contest);
}
