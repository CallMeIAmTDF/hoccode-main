package com.example.hoccode.repository;

import com.example.hoccode.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository <Report, Integer>{
    List<Report> findAllByOrderBySolvedAscCreatedAtDesc();
    List<Report> findAllByCreatedBy(String email);
}
