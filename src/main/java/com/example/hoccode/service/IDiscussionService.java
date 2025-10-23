package com.example.hoccode.service;

import com.example.hoccode.dto.request.create.CreateDiscussionRequest;
import com.example.hoccode.entity.Discussion;

public interface IDiscussionService {
    Discussion create(CreateDiscussionRequest request);
}
