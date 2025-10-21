package com.example.hoccode.repository;

import com.example.hoccode.entity.Plagiarism;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlagiarismRepository  extends JpaRepository<Plagiarism, Integer> {
    Plagiarism findFirstByCid(Integer cid);
}
