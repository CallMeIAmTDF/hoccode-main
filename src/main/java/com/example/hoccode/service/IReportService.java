package com.example.hoccode.service;

import com.example.hoccode.dto.request.SolveReportRequest;
import com.example.hoccode.dto.request.create.CreateReportRequest;
import com.example.hoccode.entity.Report;

import java.util.List;

public interface IReportService {
    Report create(CreateReportRequest request);

    void solved(SolveReportRequest request);

    List<Report> getReports();

//    List<Report> myReport();
}
