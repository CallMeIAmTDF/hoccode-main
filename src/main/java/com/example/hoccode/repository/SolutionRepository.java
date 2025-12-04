package com.example.hoccode.repository;

import com.example.hoccode.entity.Solution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SolutionRepository extends JpaRepository<Solution, Integer> {
    Solution findFirstById(Integer id);

    List<Solution> findAllByPid(Integer pid);

    List<Solution> findAllByPidAndDeletedFalse(Integer pid);
}
