package com.example.hoccode.service.impl;

import com.example.hoccode.dto.response.DashboardStatsItem;
import com.example.hoccode.entity.Contest;
import com.example.hoccode.entity.Report;
import com.example.hoccode.repository.ContestRepository;
import com.example.hoccode.repository.ProblemRepository;
import com.example.hoccode.repository.ReportRepository;
import com.example.hoccode.repository.UserRepository;
import com.example.hoccode.service.IAdminService;
import com.example.hoccode.util.constant.ReportTypeEnum;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminService implements IAdminService {
    UserRepository userRepository;
    ProblemRepository problemRepository;
    ContestRepository contestRepository;
    ReportRepository reportRepository;

    @Override
    public Map<String, Object> dashboardStatsItemList(){
        Map<String, Object> res = new HashMap<>();
        List<DashboardStatsItem> statsItems = new ArrayList<>();

        LocalDateTime startOfThisMonth = LocalDate.now()
                .withDayOfMonth(1)
                .atStartOfDay();
        /*
        *
        * String.format("%s://%s:%d", protocol, host, port)
        * */

        // total user
        Long totalUserBeforeMonth = userRepository.countUsersBeforeMonth(startOfThisMonth);
        Long totalUser = userRepository.count();
        long change = totalUser - totalUserBeforeMonth;
        statsItems.add(
                DashboardStatsItem.builder()
                        .title("users")
                        .change(String.format("Tăng %d người dùng mới tháng này", change))
                        .value(totalUser.toString())
                        .build()
        );

        // total problem
        Long totalProblemBeforeMonth = problemRepository.countProblemsBeforeMonth(startOfThisMonth);
        Long totalProblem = problemRepository.count();
        change = totalProblem - totalProblemBeforeMonth;
        statsItems.add(
                DashboardStatsItem.builder()
                        .title("problems")
                        .change(String.format("Tăng %d thử thách mới tháng này", change))
                        .value(totalUser.toString())
                        .build()
        );

        // total contest
        List<Contest> contests = contestRepository.findAll();
        Contest latestContest = contests.stream()
                .max(Comparator.comparing(Contest::getCreatedAt))
                .orElse(null);
        String message = "";
        if (latestContest != null) {
            message = "Contest gần nhất: " + timeAgo(latestContest.getCreatedAt());
        }
        statsItems.add(
                DashboardStatsItem.builder()
                        .title("contests")
                        .change(message)
                        .value(String.valueOf(contests.size()))
                        .build()
        );

        // total report
        List<Report> reports = reportRepository.findAll();
        statsItems.add(
                DashboardStatsItem.builder()
                        .title("reports")
                        .change("Chưa xử lý: " + reports.stream().filter(t -> !t.getSolved()).count())
                        .value(String.valueOf(reports.size()))
                        .build()
        );

        res.put("stats", statsItems);
        res.put("reports", reports);

        Map<ReportTypeEnum, Long> countMap = reports.stream()
                .collect(Collectors.groupingBy(Report::getType, Collectors.counting()));

        List<Map<String, Object>> result = Arrays.stream(ReportTypeEnum.values())
                .map(type -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("type", type);
                    map.put("count", countMap.getOrDefault(type, 0L));
                    return map;
                })
                .toList();
        res.put("chart", result);
        return res;
    }

    private String timeAgo(LocalDateTime createdAt) {
        Duration duration = Duration.between(createdAt, LocalDateTime.now());
        long seconds = duration.getSeconds();
        if (seconds < 60) return "vừa xong";
        long minutes = seconds / 60;
        if (minutes < 60) return minutes + " phút trước";
        long hours = minutes / 60;
        if (hours < 24) return hours + " giờ trước";
        long days = hours / 24;
        if (days < 30) return days + " ngày trước";
        long months = days / 30;
        if (months < 12) return months + " tháng trước";
        long years = months / 12;
        return years + " năm trước";
    }


}
