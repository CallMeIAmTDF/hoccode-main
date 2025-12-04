package com.example.hoccode.repository;

import com.example.hoccode.entity.Plagiarism;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlagiarismRepository  extends JpaRepository<Plagiarism, Integer> {
    Plagiarism findFirstByCid(Integer cid);
}
