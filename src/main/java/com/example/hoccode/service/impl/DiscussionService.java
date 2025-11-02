package com.example.hoccode.service.impl;

import com.example.hoccode.advice.exception.BadRequestException;
import com.example.hoccode.dto.request.create.CreateDiscussionRequest;
import com.example.hoccode.entity.Discussion;
import com.example.hoccode.repository.DiscussionRepository;
import com.example.hoccode.service.IDiscussionService;
import com.example.hoccode.service.redis.IStringStringIntegerRedis;
import com.example.hoccode.util.SecurityUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DiscussionService implements IDiscussionService {

    DiscussionRepository repository;
    SecurityUtil securityUtil;
//    ProblemRepository problemRepository;
    IStringStringIntegerRedis redis;

    @Override
    public Discussion create(CreateDiscussionRequest request){
        return repository.save(
                Discussion.builder()
                        .title(request.getTitle())
                        .pid(request.getPid())
                        .view(0)
                        .description(request.getDescription())
                        .delete(false)
                        .build()
        );
    }

    @Override
    public void delete(Integer id){
        Discussion discussion = repository.findById(id).orElseThrow(() -> new BadRequestException("Không tìm thấy thảo luận"));
        if(!securityUtil.isAdmin() && !discussion.getCreatedBy().equals(securityUtil.getCurrentUserLogin().get())){
            throw new BadRequestException("Không có quyền truy cập");
        }
        discussion.setDelete(true);
        repository.save(discussion);
    }


    @Override
    public List<Discussion> getDiscussions(Integer pid){
        return repository.findAllByPidAndDeleteFalse(pid);
    }

    @Override
    public Discussion view(Integer id){
        Discussion discussion = repository.findById(id).orElseThrow(() -> new BadRequestException("Không tìm thấy thảo luận"));
        if(!redis.isUserViewedDiscussion(securityUtil.getCurrentUserLogin().get(), id)){
            redis.saveUserViewedDiscussion(securityUtil.getCurrentUserLogin().get(), id);
            discussion.incrementView();
        }
        return repository.save(discussion);
    }
}
