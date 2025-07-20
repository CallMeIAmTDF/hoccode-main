package com.example.hoccode.repository;

import com.example.hoccode.entity.TestCase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, Integer> {

    List<TestCase> findByProblemId(Integer pid);
    void deleteAllByProblemId(Integer pid);
}
