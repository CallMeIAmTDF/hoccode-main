package com.example.hoccode.repository;

import com.example.hoccode.entity.Hint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HintRepository extends JpaRepository<Hint, Integer> {
    List<Hint> findByProblemId(Integer pid);
    void deleteAllByProblemId(Integer pid);
}
