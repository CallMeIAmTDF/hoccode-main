package com.example.hoccode.service;

import com.example.hoccode.dto.request.create.CreateDiscussionRequest;
import com.example.hoccode.entity.Discussion;

import java.util.List;

public interface IDiscussionService {
    Discussion create(CreateDiscussionRequest request);

    void delete(Integer id);

    List<Discussion> getDiscussions(Integer pid);

    Discussion view(Integer id);
}
