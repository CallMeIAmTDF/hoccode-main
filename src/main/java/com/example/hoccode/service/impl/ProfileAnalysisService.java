package com.example.hoccode.service.impl;

import com.example.hoccode.advice.exception.BadRequestException;
import com.example.hoccode.dto.analysis.*;
import com.example.hoccode.dto.analysis.ProfileAnalysisDTO.DayOfWeekAnalysis;
import com.example.hoccode.dto.analysis.ProfileAnalysisDTO.SubmissionSummary;
import com.example.hoccode.dto.response.SubmissionCategoryCount;
import com.example.hoccode.entity.Submission;
import com.example.hoccode.entity.User;
import com.example.hoccode.repository.SubmissionRepository;
import com.example.hoccode.repository.UserRepository;
import com.example.hoccode.service.IProfileAnalysisService;
import com.example.hoccode.service.UserService;
import com.example.hoccode.util.SecurityUtil;
import com.example.hoccode.util.constant.StatusEnum;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProfileAnalysisService implements IProfileAnalysisService {
    SubmissionRepository submissionRepository;
    SecurityUtil securityUtil;
    UserService userService;
    UserRepository userRepository;

    @Override
    @Cacheable(value = "hourlyCache", key = "T(String).format('profileAnalysis:%s', @securityUtil.getCurrentUserLogin().get())")
    public ProfileAnalysisDTO analyzeUserProfile(){
        String email = securityUtil.getCurrentUserLogin().get();

        SubmissionSummary summary = getOptimizedSubmissionSummary(email);
        Map<String, Long> categoryAnalysis = getCategorySubmissionCounts(email);
        List<Submission> submissions = submissionRepository.findNonContestSubmissionsByCreatedBy(email, false);
//        Map<String, Long> languageAnalysis = getLanguageSubmissionCounts(email);

        Map<String, LanguageStatusSummary> languageAnalysis = getLanguageSubmissionCounts(email);
        Map<String, Long> timeSlotCounts = analyzeTimeSlots(submissions);
        DayOfWeekAnalysis dayOfWeekAnalysis = analyzeDaysAndBusiestTime(submissions, timeSlotCounts);
        User user = userRepository.findByEmail(email).orElseThrow(() -> new BadRequestException("Không tìm thấy người dùng"));
        Map<String, Object> stats = userService.getStats(user);

        return ProfileAnalysisDTO.builder()
                .submissionSummary(summary)
                .stats(
                        ProfileAnalysisDTO.Stats.builder()
                                .rank((Integer) stats.getOrDefault("rank", 999))
                                .joined((LocalDateTime) stats.getOrDefault("joined", LocalDateTime.now()))
                                .point((Integer) stats.getOrDefault("point", 0))
                                .build()
                )
                .languageSubmissionSummary(languageAnalysis)
                .categorySubmissionCount(categoryAnalysis)
                .dailyTimeSlotAnalysis(timeSlotCounts)
                .dayOfWeekAnalysis(dayOfWeekAnalysis)
                .build();
    }

    private Map<String, LanguageStatusSummary> getLanguageSubmissionCounts(String userEmail) {
        List<LanguageStatusCountDTO> results = submissionRepository.countLanguageStatus(userEmail);

        Map<String, Map<StatusEnum, Long>> groupedByLangAndStatus = results.stream()
                .collect(Collectors.groupingBy(
                        LanguageStatusCountDTO::getLanguage,
                        Collectors.toMap(
                                LanguageStatusCountDTO::getStatus,
                                LanguageStatusCountDTO::getCount
                        )
                ));

        return groupedByLangAndStatus.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            Map<StatusEnum, Long> statusCounts = entry.getValue();
                            long passed = statusCounts.getOrDefault(StatusEnum.ACCEPTED, 0L);
                            long total = statusCounts.values().stream().mapToLong(Long::longValue).sum();

                            return LanguageStatusSummary.builder()
                                    .passed(passed)
                                    .failed(total - passed)
                                    .total(total)
                                    .build();
                        }
                ));
    }

    private SubmissionSummary getOptimizedSubmissionSummary(String userEmail) {
        List<SubmissionStatusCountDTO> results = submissionRepository.countStatusByCreatedByAndNonContestProblem(userEmail);

        long passed = 0;
        long total = 0;

        for (SubmissionStatusCountDTO result : results) {
            StatusEnum status = result.getStatus();
            Long count = result.getCount();
            if (status == StatusEnum.ACCEPTED) {
                passed = count;
            }
            total += count;
        }

        return SubmissionSummary.builder()
                .passedSubmissions(passed)
                .failedSubmissions(total - passed)
                .build();
    }

    private Map<String, Long> analyzeTimeSlots(List<Submission> submissions) {
        return submissions.stream()
                .collect(Collectors.groupingBy(s -> {
                    int hour = s.getCreatedAt().getHour();

                    if (hour < 6) return "EARLY_MORNING";
                    if (hour < 12) return "MORNING";
                    if (hour < 18) return "AFTERNOON";
                    return "EVENING";
                }, Collectors.counting()));
    }

    private DayOfWeekAnalysis analyzeDaysAndBusiestTime(List<Submission> submissions, Map<String, Long> timeSlotCounts) {
        String busiestTimeSlot = timeSlotCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        Map<String, Long> dayOfWeekCounts = submissions.stream()
                .collect(Collectors.groupingBy(s ->
                                s.getCreatedAt().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase(),
                        Collectors.counting()));

        String busiestDay = dayOfWeekCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        return DayOfWeekAnalysis.builder()
                .busiestDay(busiestDay)
                .busiestTimeSlot(busiestTimeSlot)
                .build();
    }

    private Map<String, Long> getCategorySubmissionCounts(String userEmail) {
        // Sử dụng JPQL method mới
        List<SubmissionCategoryCount> results = submissionRepository.countAcceptedByCategory(userEmail);

        return results.stream()
                .collect(Collectors.toMap(
                        SubmissionCategoryCount::getCategory,
                        SubmissionCategoryCount::getCount
                ));
    }
}
