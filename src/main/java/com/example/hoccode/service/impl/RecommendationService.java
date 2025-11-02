package com.example.hoccode.service.impl;

import com.example.hoccode.advice.exception.BadRequestException;
import com.example.hoccode.dto.mapper.ProblemMapper;
import com.example.hoccode.dto.response.RecommendationResult;
import com.example.hoccode.entity.Problem;
import com.example.hoccode.entity.Submission;
import com.example.hoccode.repository.ProblemRepository;
import com.example.hoccode.repository.SubmissionRepository;
import com.example.hoccode.service.IRecommendationService;
import com.example.hoccode.util.constant.StatusEnum;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RecommendationService implements IRecommendationService {
    SubmissionRepository submissionRepository;
    ProblemRepository problemRepository;
    ProblemMapper problemMapper;

    public RecommendationResult recommendOne(String currentUserId) {
        List<Submission> allSubmissions = submissionRepository.findAll();

        Map<String, List<Submission>> submissionsByUser = allSubmissions.stream()
                .collect(Collectors.groupingBy(Submission::getCreatedBy));

        Map<Integer, Double> currentVector = buildUserVector(submissionsByUser, currentUserId);
        if (currentVector.isEmpty()) {
            throw new BadRequestException("User chưa có submission nào, không thể gợi ý.");
        }

        List<String> allUsers = submissionsByUser.keySet().stream()
                .filter(u -> !u.equals(currentUserId))
                .toList();

        if (allUsers.isEmpty()) {
            throw new BadRequestException("Không đủ dữ liệu người dùng để tính tương đồng.");
        }

        Map.Entry<String, Double> best = allUsers.stream()
                .map(u -> Map.entry(u, cosineSimilarity(currentVector, buildUserVector(submissionsByUser, u))))
                .filter(e -> e.getValue() > 0)
                .max(Map.Entry.comparingByValue())
                .orElseThrow(() -> new BadRequestException("Không tìm thấy người dùng tương đồng."));

        String mostSimilarUser = best.getKey();
        double bestSim = best.getValue();

        Map<Integer, Double> similarVector = buildUserVector(submissionsByUser, mostSimilarUser);

        List<Integer> candidateIds = similarVector.entrySet().stream()
                .filter(e -> currentVector.getOrDefault(e.getKey(), 0.0) < 1.0)
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (candidateIds.isEmpty()) {
            candidateIds = new ArrayList<>(similarVector.keySet());
        }

        Collections.shuffle(candidateIds);
        Problem problem = problemRepository.findById(candidateIds.get(0))
                .orElseThrow(() -> new BadRequestException("Problem không tồn tại trong DB."));

        return new RecommendationResult(
                mostSimilarUser,
                round(bestSim, 3),
                problemMapper.toPublicProblemResponse(problem, true)
        );
    }

    private Map<Integer, Double> buildUserVector(Map<String, List<Submission>> map, String userId) {
        return map.getOrDefault(userId, Collections.emptyList()).stream()
                .filter(s -> s.getTotalTestCases() != null && s.getTotalTestCases() > 0)
                .collect(Collectors.toMap(
                        Submission::getPid,
                        s -> s.getStatus() == StatusEnum.ACCEPTED ? 1.0
                                : (double) s.getPassedTestCase() / s.getTotalTestCases()
                ));
    }

    private double cosineSimilarity(Map<Integer, Double> a, Map<Integer, Double> b) {
        double dot = a.entrySet().stream()
                .mapToDouble(e -> e.getValue() * b.getOrDefault(e.getKey(), 0.0))
                .sum();
        double normA = Math.sqrt(a.values().stream().mapToDouble(v -> v * v).sum());
        double normB = Math.sqrt(b.values().stream().mapToDouble(v -> v * v).sum());

        return (normA == 0 || normB == 0) ? 0 : dot / (normA * normB);
    }

    private double round(double value, int places) {
        double scale = Math.pow(10, places);
        return Math.round(value * scale) / scale;
    }
}
