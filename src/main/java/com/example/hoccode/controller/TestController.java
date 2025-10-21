package com.example.hoccode.controller;

import com.example.hoccode.repository.SubmissionRepository;
import com.example.hoccode.service.IContestService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
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

    @GetMapping("/{contestId}")
    public Map<String, Object> getContestStats(@PathVariable Integer contestId){
        return contestService.getContestStats(contestId);
    }
}
