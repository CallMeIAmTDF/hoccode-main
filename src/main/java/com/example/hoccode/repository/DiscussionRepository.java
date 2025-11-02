package com.example.hoccode.repository;

import com.example.hoccode.entity.Discussion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface DiscussionRepository extends JpaRepository<Discussion, Integer> {
    List<Discussion> findAllByPidAndDeleteFalse(Integer pid);
}
