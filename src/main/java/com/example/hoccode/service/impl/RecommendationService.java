package com.example.hoccode.service.impl;

import com.example.hoccode.dto.mapper.ProblemMapper;
import com.example.hoccode.dto.simple.UserSimilarity;
import com.example.hoccode.entity.Problem;
import com.example.hoccode.entity.Submission;
import com.example.hoccode.repository.ProblemRepository;
import com.example.hoccode.repository.SubmissionRepository;
import com.example.hoccode.service.IRecommendationService;
import com.example.hoccode.util.SecurityUtil;
import com.example.hoccode.util.constant.StatusEnum;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RecommendationService implements IRecommendationService {
    SubmissionRepository submissionRepository;
    ProblemRepository problemRepository;
    ProblemMapper problemMapper;
    SecurityUtil securityUtil;

    @Override
    public Problem recommendProblem() {
        String userEmail = securityUtil.getCurrentUserLogin().get();
        // 1. Lấy danh sách submission của user hiện tại
        List<Submission> userSubmissions = submissionRepository
                .findByCreatedBy(userEmail);

        if (userSubmissions.isEmpty()) {
            return getRandomProblem();
        }

        // 2. Tạo user profile (problem ID -> số lần submit hoặc success rate)
        Map<Integer, Double> userProfile = buildUserProfile(userSubmissions);

        // 3. Lấy set các problem mà user đã làm
        Set<Integer> solvedProblems = userProfile.keySet();

        // 4. Tìm user tương đồng nhất dựa trên Cosine Similarity
        String similarUser = findMostSimilarUser(userEmail, userProfile);
        System.out.println("similarUser: " + similarUser);
        if (similarUser == null) {
            return getRandomProblem();
        }

        // 5. Lấy danh sách problem của user tương đồng
        List<Submission> similarUserSubmissions = submissionRepository
                .findByCreatedBy(similarUser);

        Map<Integer, Double> similarUserProfile = buildUserProfile(similarUserSubmissions);

        // 6. Tìm những problem mà user tương đồng đã làm nhưng user hiện tại chưa làm
        // Sắp xếp theo score (rating) của user tương đồng
        List<Map.Entry<Integer, Double>> recommendedProblems = similarUserProfile.entrySet()
                .stream()
                .filter(entry -> !solvedProblems.contains(entry.getKey()))
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .collect(Collectors.toList());

        // 7. Nếu không có problem mới, lấy random từ tất cả problem của user tương đồng
        if (recommendedProblems.isEmpty()) {
            recommendedProblems = new ArrayList<>(similarUserProfile.entrySet());
        }

        // 8. Trả về một problem ngẫu nhiên từ top recommendations
        if (recommendedProblems.isEmpty()) {
            return getRandomProblem();
        }

        // Lấy random từ top 5 problems (hoặc ít hơn nếu không đủ)
        int topN = Math.min(5, recommendedProblems.size());
        Random random = new Random();
        Integer selectedProblemId = recommendedProblems.get(random.nextInt(topN)).getKey();

        return problemRepository.findById(selectedProblemId)
                .filter(p -> !Boolean.TRUE.equals(p.getIsContestProblem()))
                .orElse(getRandomProblem());
    }

    /**
     * Xây dựng user profile: problem ID -> score
     * Score = (số submission AC / tổng số submission) * số lần submit
     * Hoặc đơn giản hơn: số lần submit problem đó
     */
    private Map<Integer, Double> buildUserProfile(List<Submission> submissions) {
        Map<Integer, List<Submission>> problemSubmissions = submissions.stream()
                .collect(Collectors.groupingBy(Submission::getPid));

        Map<Integer, Double> profile = new HashMap<>();

        for (Map.Entry<Integer, List<Submission>> entry : problemSubmissions.entrySet()) {
            Integer problemId = entry.getKey();
            List<Submission> subs = entry.getValue();

            // Tính score: số submission AC / tổng số submission
            long acceptedCount = subs.stream()
                    .filter(s -> s.getStatus() == StatusEnum.ACCEPTED)
                    .count();

            double score = subs.isEmpty() ? 0.0 : (double) acceptedCount / subs.size();

            // Bonus cho problem có nhiều submission (quan tâm nhiều)
            score += Math.log(subs.size() + 1) * 0.1;

            profile.put(problemId, score);
        }

        return profile;
    }

    /**
     * Tìm user có submission tương đồng nhất với user hiện tại
     * Sử dụng Cosine Similarity
     */
    private String findMostSimilarUser(String currentUser, Map<Integer, Double> userProfile) {
        List<String> allUsers = submissionRepository.findDistinctCreatedBy();

        String mostSimilarUser = null;
        double maxSimilarity = -1.0;

        for (String otherUser : allUsers) {
            if (otherUser.equals(currentUser)) {
                continue;
            }

            // Lấy profile của user khác
            List<Submission> otherSubmissions = submissionRepository
                    .findByCreatedBy(otherUser);

            if (otherSubmissions.isEmpty()) {
                continue;
            }

            Map<Integer, Double> otherProfile = buildUserProfile(otherSubmissions);

            // Tính Cosine Similarity
            double similarity = calculateCosineSimilarity(userProfile, otherProfile);

            if (similarity > maxSimilarity) {
                maxSimilarity = similarity;
                mostSimilarUser = otherUser;
            }
        }

        return mostSimilarUser;
    }

    /**
     * Tính Cosine Similarity giữa 2 user profiles
     * Công thức: cos(θ) = (A · B) / (||A|| * ||B||)
     *
     * A · B = tích vô hướng (dot product)
     * ||A|| = độ dài vector A (magnitude)
     */
    private double calculateCosineSimilarity(Map<Integer, Double> profile1,
                                             Map<Integer, Double> profile2) {
        // Tìm tất cả problem IDs từ cả 2 profiles
        Set<Integer> allProblems = new HashSet<>();
        allProblems.addAll(profile1.keySet());
        allProblems.addAll(profile2.keySet());

        if (allProblems.isEmpty()) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double magnitude1 = 0.0;
        double magnitude2 = 0.0;

        for (Integer problemId : allProblems) {
            double value1 = profile1.getOrDefault(problemId, 0.0);
            double value2 = profile2.getOrDefault(problemId, 0.0);

            dotProduct += value1 * value2;
            magnitude1 += value1 * value1;
            magnitude2 += value2 * value2;
        }

        magnitude1 = Math.sqrt(magnitude1);
        magnitude2 = Math.sqrt(magnitude2);

        if (magnitude1 == 0.0 || magnitude2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (magnitude1 * magnitude2);
    }

    /**
     * Lấy một problem ngẫu nhiên (fallback)
     */
    private Problem getRandomProblem() {
        List<Problem> allProblems = problemRepository.findByDeletedFalseAndIsContestProblemFalse();

        if (allProblems.isEmpty()) {
            return null;
        }

        Random random = new Random();
        return allProblems.get(random.nextInt(allProblems.size()));
    }

    /**
     * API để xem top N users tương đồng nhất (để debug/analysis)
     */
    public List<UserSimilarity> getTopSimilarUsers(String userEmail, int topN) {
        List<Submission> userSubmissions = submissionRepository
                .findByCreatedBy(userEmail);

        if (userSubmissions.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Integer, Double> userProfile = buildUserProfile(userSubmissions);
        List<String> allUsers = submissionRepository.findDistinctCreatedBy();

        List<UserSimilarity> similarities = new ArrayList<>();

        for (String otherUser : allUsers) {
            if (otherUser.equals(userEmail)) {
                continue;
            }

            List<Submission> otherSubmissions = submissionRepository
                    .findByCreatedBy(otherUser);

            if (otherSubmissions.isEmpty()) {
                continue;
            }

            Map<Integer, Double> otherProfile = buildUserProfile(otherSubmissions);
            double similarity = calculateCosineSimilarity(userProfile, otherProfile);

            similarities.add(new UserSimilarity(otherUser, similarity));
        }

        return similarities.stream()
                .sorted(Comparator.comparingDouble(UserSimilarity::getSimilarity).reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }
}
