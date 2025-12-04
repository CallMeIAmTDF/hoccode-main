package com.example.hoccode.repository;

import com.example.hoccode.entity.Discussion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscussionRepository extends JpaRepository<Discussion, Integer> {
    List<Discussion> findAllByPidAndDeleteFalse(Integer pid);
}
