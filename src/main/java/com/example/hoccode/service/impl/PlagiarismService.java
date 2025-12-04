package com.example.hoccode.service.impl;

import com.example.hoccode.advice.exception.BadRequestException;
import com.example.hoccode.dto.SocketMessage;
import com.example.hoccode.dto.projection.SubmissionWithProblemTitleProjection;
import com.example.hoccode.dto.response.PlagiarismDetailResponse;
import com.example.hoccode.dto.response.contest.ContestDetailResponse;
import com.example.hoccode.entity.Contest;
import com.example.hoccode.entity.Plagiarism;
import com.example.hoccode.event.NotificationEvent;
import com.example.hoccode.repository.ContestRepository;
import com.example.hoccode.repository.PlagiarismRepository;
import com.example.hoccode.repository.SubmissionRepository;
import com.example.hoccode.service.IContestService;
import com.example.hoccode.service.IPlagiarismService;
import com.example.hoccode.service.IProblemService;
import com.example.hoccode.util.SecurityUtil;
import com.example.hoccode.util.constant.PlagiarismStatus;
import com.example.hoccode.util.constant.SocketType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.example.hoccode.dto.request.PlagiarismRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.hoccode.util.WebClientUtil.safeCall;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PlagiarismService implements IPlagiarismService {
    ContestRepository contestRepository;
    PlagiarismRepository plagiarismRepository;
    SecurityUtil securityUtil;
    SubmissionRepository submissionRepository;
    SimpMessagingTemplate messagingTemplate;
    KafkaTemplate<String, Object> kafkaTemplate;
    IProblemService problemService;
    IContestService contestService;

    @Qualifier("plagiarismClient")
    WebClient plagiarismClient;

    @Value("${api.base-url}")
    @NonFinal
    String baseUrl;

    @Value("${api.path.noti-plagiarism}")
    @NonFinal
    String urlNoticePlagiarism;

    @Override
    @Transactional
    public List<PlagiarismDetailResponse> plagiarismChecking(Integer contestId) throws JsonProcessingException {
        Contest contest = contestRepository.findFirstById(contestId);
        if(contest == null){
            throw new BadRequestException("Không tìm thấy cuộc thi có id:" + contestId);
        }

        if(!contest.getCreatedBy().equals(securityUtil.getCurrentUserLogin().get())){
            throw new BadRequestException("Truy cập bị từ chối");
        }

        Plagiarism plagiarism = plagiarismRepository.findFirstByCid(contestId);

        if(plagiarism != null && plagiarism.getStatus().equals(PlagiarismStatus.SUCCESS)){
            List<PlagiarismDetailResponse> result = plagiarismClient.get()
                    .uri("/getPlagiarism?file=" + plagiarism.getFile())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<PlagiarismDetailResponse>>() {})
                    .block();

            return result != null ? result : Collections.emptyList();
        }
        else{
            List<SubmissionWithProblemTitleProjection> submissions =
                    submissionRepository.findFirstAcceptedSubmissionsWithProblemTitle(contestId);

            List<PlagiarismRequest.Submission> submissionList = submissions.stream()
                    .map(s ->
                        PlagiarismRequest.Submission.builder()
                                .username(s.getUsername())
                                .code(s.getCode())
                                .language(s.getLanguage())
                                .pid(s.getProblemId())
                                .pTitle(s.getProblemTitle())
                                .build()
                    )
                    .toList();

            PlagiarismRequest.Contest contestRequest =  PlagiarismRequest.Contest.builder()
                    .id(contestId)
                    .title(contest.getTitle())
                    .description(contest.getDescription())
                    .build();

            PlagiarismRequest request = PlagiarismRequest.builder()
                    .submissions(submissionList)
                    .contest(contestRequest)
                    .build();

            ObjectMapper mapper = new ObjectMapper();
            String jsonBody = mapper.writeValueAsString(request);
            System.out.println(">>> JSON Body: " + jsonBody);

            // Call API
            plagiarismClient.post()
                    .uri("/plagiarism")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .doOnError(e -> log.error("Error when calling API: " + e.getMessage()))
                    .subscribe();

            // Lưu lịch sử
            plagiarism = Plagiarism.builder()
                    .file("contest_"+contestId)
                    .cid(contestId)
                    .status(PlagiarismStatus.PENDING)
                    .build();
            plagiarismRepository.save(plagiarism);

        }
        return null;
    }

    @Override
    public void callBack(Integer contestId){
        Plagiarism plagiarism = plagiarismRepository.findFirstByCid(contestId);
        if(plagiarism != null){
            if(plagiarism.getStatus().equals(PlagiarismStatus.PENDING)){
                plagiarism.setStatus(PlagiarismStatus.SUCCESS);
                plagiarismRepository.save(plagiarism);

                messagingTemplate.convertAndSendToUser(
                        plagiarism.getCreatedBy(),
                        "/queue",
                        new SocketMessage(
                                SocketType.SIMPLE_NOTIFICATION,
                                "Kiểm tra gian lận đã hoàn tất",
                                Map.of(
                                        "contestId", contestId,
                                        "status", "SUCCESS"
                                )
                        )
                );

                Map<String, String> param = new HashMap<>();
                param.put("url", baseUrl + urlNoticePlagiarism + contestId + "/submissions");

                NotificationEvent notificationEvent = NotificationEvent.builder()
                        .channel("EMAIL")
                        .recipient(plagiarism.getCreatedBy())
                        .subject("Kết quả kiểm tra gian lận!!!")
                        .param(param)
                        .body("Kết quả kiểm tra gian lận!!!")
                        .build();
                kafkaTemplate.send("plagiarism", notificationEvent);
            }
            else{
                throw new BadRequestException("Đã kiểm tra đạo văn trước đó");
            }
        }
        else{
            throw new BadRequestException("Don't Use API!!!");
        }
    }

    @Override
    public Boolean isPlagiarismChecked(Integer contestId){
        Plagiarism plagiarism = plagiarismRepository.findFirstByCid(contestId);
        if(plagiarism != null){
            return plagiarism.getStatus().equals(PlagiarismStatus.SUCCESS);
        }
        else{
            return false;
        }
    }

    @Override
    public byte[] getFile(Integer contestId) {

        ContestDetailResponse contest = contestService.getContestDetail(contestId);

        return safeCall(
                () -> plagiarismClient.post()
                        .uri("/generate-pdf")
                        .accept(MediaType.APPLICATION_PDF)
                        .bodyValue(contest)
                        .retrieve()
                        .bodyToMono(byte[].class),
                "Dịch vụ GPT gặp lỗi, vui lòng thử lại sau"
        );
    }

    @Override
    public Object getFile2(Integer contestId) {
        ContestDetailResponse contest = contestService.getContestDetail(contestId);
        return contest;
    }

}
