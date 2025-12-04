package com.example.hoccode.service.impl;

import com.example.hoccode.advice.exception.BadRequestException;
import com.example.hoccode.dto.request.InDepthCodeAnalysisRequest;
import com.example.hoccode.dto.request.call.BasicCodeAnalysisRequest;
import com.example.hoccode.dto.response.InDepthCodeAnalysisResponse;
import com.example.hoccode.dto.response.call.BasicCodeAnalysisResponse;
import com.example.hoccode.entity.Problem;
import com.example.hoccode.entity.Submission;
import com.example.hoccode.repository.ProblemRepository;
import com.example.hoccode.repository.SubmissionRepository;
import com.example.hoccode.service.IGPTService;
import com.example.hoccode.util.SecurityUtil;
import com.example.hoccode.util.constant.StatusEnum;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import static com.example.hoccode.util.WebClientUtil.safeCall;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GPTService implements IGPTService {

    @Qualifier("gptClient")
    WebClient gptClient;

    SubmissionRepository submissionRepository;
    ProblemRepository problemRepository;
    SecurityUtil securityUtil;

    @Override
    @Cacheable(value = "hourlyCache", key = "#submissionId.hashCode()")
    public BasicCodeAnalysisResponse basicCodeAnalysis(String submissionId) {

        Submission submission = submissionRepository.findFirstById(submissionId).orElseThrow(() -> new BadRequestException("Không tồn tại bài nộp"));
        if(!submission.getCreatedBy().equals(securityUtil.getCurrentUserLogin().get())){
            throw new BadRequestException("Không có quyền truy cập");
        }

        Problem problem = problemRepository.findByIdAndDeletedFalse(submission.getPid()).orElseThrow(() -> new BadRequestException("Thử thách không hợp lệ"));

        if(problem.getIsContestProblem()){
            throw new BadRequestException("Không thể phân tích thử thách trong cuộc thi");
        }

        if(!StatusEnum.ACCEPTED.equals(submission.getStatus())){
            throw new BadRequestException("Bài nộp không hợp lệ");
        }

        Submission latestOpt = submissionRepository
                .findTopByCreatedByAndPidOrderByCreatedAtDesc(submission.getCreatedBy(), submission.getPid()).orElseThrow(() -> new BadRequestException("Lỗi hệ thống, vui lòng thử lại!!"));

        if(!latestOpt.getId().equals(submissionId)){
            throw new BadRequestException("Hệ thống chỉ hỗ trợ kiểm tra bài nộp mới nhất");
        }

        return safeCall(
                () -> gptClient.post()
                        .uri("/basicCodeAnalysis")
                        .bodyValue(BasicCodeAnalysisRequest.builder().code(submission.getCode()).build())
                        .retrieve()
                        .bodyToMono(BasicCodeAnalysisResponse.class),
                "Dịch vụ GPT gặp lỗi, vui lòng thử lại sau"
        );
    }

    @Override
    @Cacheable(
            value = "shortCache",
            key = "T(String).format('%s:%s', #problemId, @securityUtil.getCurrentUserLogin().get())"
    )
    public InDepthCodeAnalysisResponse inDepthCodeAnalysis(Integer problemId){
        Problem problem = problemRepository.findByIdAndDeletedFalse(problemId).orElseThrow(() -> new BadRequestException("Thử thách không hợp lệ"));

        if(problem.getIsContestProblem()){
            throw new BadRequestException("Không thể phân tích thử thách trong cuộc thi");
        }

        List<Submission> submissions = submissionRepository.findRecentSubmissionsBetweenLastTwoAccepted(securityUtil.getCurrentUserLogin().get(), problemId);

        if(submissions.size() < 2){
            throw new BadRequestException("Không thể phân tích chuyên sâu");
        }

        return safeCall(
                () -> gptClient.post()
                        .uri("/in-depth")
                        .bodyValue(submissions.stream().map(
                                s -> InDepthCodeAnalysisRequest.builder().code(s.getCode()).status(s.getStatus()).build()
                        ))
                        .retrieve()
                        .bodyToMono(InDepthCodeAnalysisResponse.class),
                "Dịch vụ GPT gặp lỗi, vui lòng thử lại sau"
        );
    }

    public String getEmail(){
        return securityUtil.getCurrentUserLogin().get();
    }
}
