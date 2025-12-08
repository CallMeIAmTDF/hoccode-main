package com.example.hoccode.controller;

import com.example.hoccode.repository.SubmissionRepository;
import com.example.hoccode.service.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/test")
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TestController {
    KafkaTemplate<String, Object> kafkaTemplate;
    SubmissionRepository submissionRepository;
    IContestService contestService;
    IGPTService gptService;
    IPlagiarismService plagiarismService;

//    @GetMapping("/test")
//    public Object hello(){
////        int totalUsers = 100;
////        long start = System.currentTimeMillis();
////        List<String> emails = new ArrayList<>();
////        for (int i = 1; i <= totalUsers; i++) {
////            String fakeEmail = "user" + i + "@example.com";
////            emails.add(fakeEmail);
////        }
////        NotificationEvent event = NotificationEvent.builder()
////                .channel("EMAIL")
////                .recipient(String.join(",", emails))
////                .subject("Contest Reminder")
////                .param(Map.of(
////                        "code", "1",
////                        "title", "Contest Demo",
////                        "startTime", "08:00:00 20/08/2025",
////                        "endTime", "12:00:00 20/08/2025",
////                        "description", "This is a test contest"
////                ))
////                .build();
////
////        kafkaTemplate.send("contest-delivery", event);
////        long end = System.currentTimeMillis();
////        return ("✅ Sent " + totalUsers + " events in " + (end - start) + " ms");
//
//        return submissionRepository.countAcceptedByCategory();
//    }

    INotificationService notificationService;
    IRecommendationService recommendationService;
//    ISubmissionService submissionService;
    @GetMapping("/{contestId}")
    public Map<String, Object> getContestStats(@PathVariable Integer contestId){
        return contestService.getContestStats(contestId);
    }

    @GetMapping("")
    public Object test(){
        return gptService.inDepthCodeAnalysis(4);
    }

    @GetMapping("/{id}/generatePDF")
    public ResponseEntity<byte[]> generatePDF(@PathVariable Integer id){
        byte[] fileData = plagiarismService.getFile(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.inline().filename("file.pdf").build());
        return ResponseEntity.ok()
                .headers(headers)
                .body(fileData);
    }

    @GetMapping("/{id}/generatePDF2")
    public ResponseEntity<Object> generatePDF2(@PathVariable Integer id){
        return ResponseEntity.ok()
                .body(plagiarismService.getFile2(id));
    }

    @GetMapping("/recommend")
    public ResponseEntity<Object> recommend(){
        return ResponseEntity.ok()
                .body(recommendationService.recommendProblem());
    }

    IUltraRecommendationService ultraRecommendationService;
    @GetMapping("/ultraRecommend")
    public ResponseEntity<Object> ultraRecommend(){
        return ResponseEntity.ok()
                .body(ultraRecommendationService.recommend());
    }

    @GetMapping("/ultraRecommend/clear")
    public ResponseEntity<Object> ultraRecommendClearCache(){
        ultraRecommendationService.clearCache();
        return ResponseEntity.ok()
                .body(null);
    }
}
