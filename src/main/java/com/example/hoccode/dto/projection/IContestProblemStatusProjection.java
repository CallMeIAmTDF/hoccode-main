package com.example.hoccode.dto.projection;

public interface IContestProblemStatusProjection {
    String getProblemName();
    String getStatus();
    Integer getAcceptedCount();
    Integer getWrongCount();
}
