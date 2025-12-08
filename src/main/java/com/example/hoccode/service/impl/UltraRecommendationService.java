package com.example.hoccode.service.impl;

import com.example.hoccode.dto.mapper.ProblemMapper;
import com.example.hoccode.dto.simple.UserSimilarity;
import com.example.hoccode.entity.Problem;
import com.example.hoccode.entity.Submission;
import com.example.hoccode.repository.ProblemRepository;
import com.example.hoccode.repository.SubmissionRepository;
import com.example.hoccode.service.IUltraRecommendationService;
import com.example.hoccode.util.SecurityUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UltraRecommendationService implements IUltraRecommendationService {
    SubmissionRepository submissionRepository;
    ProblemRepository problemRepository;
    RedisTemplate<String, Object> redisTemplate;
    SecurityUtil securityUtil;
    ProblemMapper problemMapper;

    // Redis key
    static String PREFIX_RECOMMENDATION_QUEUE = "cf:rec:";
    static String PREFIX_USER_SOLVED = "cf:solved:";
    static String PREFIX_SIMILAR_USERS = "cf:similar:";
    static String KEY_AVAILABLE_PROBLEMS = "cf:problems:available";
    static String KEY_CACHE_INITIALIZED = "cf:cache:initialized";

    // Redis TTL
    static final long RECOMMENDATION_TTL = 86400; // 24 hours
    static final long PROFILE_TTL = 604800; // 7 days
    static final long SIMILARITY_TTL = 604800; // 7 days

    Random random = new Random();

    @Override
    public Map<String, Integer> recommend(){
        Problem p = recommendProblem();
        return Map.of("recommend", p.getId());
    }

    @Override
    public Problem recommendProblem() {
        String userEmail = securityUtil.getCurrentUserLogin().get();
        ensureCacheInitialized();

        String queueKey = PREFIX_RECOMMENDATION_QUEUE + userEmail;

        for (int attempts = 0; attempts < 5; attempts++) {
            Object problemIdObj = redisTemplate.opsForList().leftPop(queueKey);

            if (problemIdObj == null) {
                break;
            }

            Integer problemId = (Integer) problemIdObj;

            Optional<Problem> problemOpt = problemRepository.findById(problemId);

            if (problemOpt.isPresent()) {
                Problem problem = problemOpt.get();

                if (!Boolean.TRUE.equals(problem.getIsContestProblem())) {
                    Long queueSize = redisTemplate.opsForList().size(queueKey);
                    if (queueSize != null && queueSize < 3) {
                        refillRecommendationsAsync(userEmail);
                    }

                    return problem;
                }
            }
        }
        return handleCacheMiss(userEmail);
    }

    private Problem handleCacheMiss(String userEmail) {
        log.info("Cache miss for user: {}. Returning random and computing async.", userEmail);

        String solvedKey = PREFIX_USER_SOLVED + userEmail;
        Set<Object> solvedSet = redisTemplate.opsForSet().members(solvedKey);

        Set<Integer> solvedProblems = new HashSet<>();
        if (solvedSet != null && !solvedSet.isEmpty()) {
            solvedProblems = solvedSet.stream()
                    .map(obj -> (Integer) obj)
                    .collect(Collectors.toSet());
        }

        Problem randomProblem = getRandomProblem(solvedProblems);

        computeRecommendationsAsync(userEmail);

        return randomProblem;
    }

    @Async
    protected void computeRecommendationsAsync(String userEmail) {
        try {
            log.info("Starting async computation for user: {}", userEmail);
            long startTime = System.currentTimeMillis();

            String solvedKey = PREFIX_USER_SOLVED + userEmail;
            Set<Object> existingSolved = redisTemplate.opsForSet().members(solvedKey);

            if (existingSolved == null || existingSolved.isEmpty()) {
                Set<Integer> solvedProblems = loadUserSolvedProblemsFromDB(userEmail);

                if (solvedProblems.isEmpty()) {
                    log.info("User {} has no submissions. Skipping computation.", userEmail);
                    return;
                }
            }

            computeSimilarityForUser(userEmail);

            generateRecommendationQueue(userEmail);

            long duration = System.currentTimeMillis() - startTime;
            log.info("Async computation completed for user {} in {}ms", userEmail, duration);

        } catch (Exception e) {
            log.error("Error in async computation for user: {}", userEmail, e);
        }
    }

    public void ensureCacheInitialized() {
        Boolean initialized = (Boolean) redisTemplate.opsForValue()
                .get(KEY_CACHE_INITIALIZED);

        if (Boolean.TRUE.equals(initialized)) {
            return;
        }

        log.warn("Redis cache not fully initialized. Loading essentials...");
        lazyInitializeCache();
    }

    private void lazyInitializeCache() {
        try {
            log.info("Starting MINIMAL lazy initialization...");
            long startTime = System.currentTimeMillis();

            updateAvailableProblems();
            log.info("✓ Loaded available problems in {}ms",
                    System.currentTimeMillis() - startTime);

            redisTemplate.opsForValue().set(KEY_CACHE_INITIALIZED, true);
            redisTemplate.expire(KEY_CACHE_INITIALIZED, 1, TimeUnit.SECONDS);

            long duration = System.currentTimeMillis() - startTime;
            log.info("Minimal lazy initialization completed in {}ms", duration);
            log.info("System can now serve requests. Full preload will run in background.");

            CompletableFuture.runAsync(() -> {
                try {
                    log.info("Starting FULL background preload...");
                    long bgStart = System.currentTimeMillis();

                    loadAllUserProfiles();
                    computeSimilarityMatrix();
                    generateAllRecommendations();

                    long bgDuration = System.currentTimeMillis() - bgStart;
                    log.info("✓ Full background preload completed in {}ms", bgDuration);

                    redisTemplate.opsForValue().set(KEY_CACHE_INITIALIZED, true);

                } catch (Exception e) {
                    log.error("Background preload failed", e);
                }
            });

        } catch (Exception e) {
            log.error("Lazy initialization failed", e);
        }
    }

    private Problem generateRecommendationSync(String userEmail) {
        String solvedKey = PREFIX_USER_SOLVED + userEmail;
        Set<Object> solvedSet = redisTemplate.opsForSet().members(solvedKey);

        Set<Integer> solvedProblems = new HashSet<>();
        if (solvedSet != null) {
            solvedProblems = solvedSet.stream()
                    .map(obj -> (Integer) obj)
                    .collect(Collectors.toSet());
        }

        if (solvedProblems.isEmpty()) {
            solvedProblems = loadUserSolvedProblemsFromDB(userEmail);
        }

        String similarKey = PREFIX_SIMILAR_USERS + userEmail;
        List<Object> similarUsersList = redisTemplate.opsForList()
                .range(similarKey, 0, -1);

        if (similarUsersList != null && !similarUsersList.isEmpty()) {
            Set<Integer> finalSolvedProblems = solvedProblems;

            for (Object similarUserObj : similarUsersList) {
                String similarUser = (String) similarUserObj;
                String similarSolvedKey = PREFIX_USER_SOLVED + similarUser;

                Set<Object> similarSolvedSet = redisTemplate.opsForSet()
                        .members(similarSolvedKey);

                if (similarSolvedSet != null) {
                    Optional<Integer> recommendation = similarSolvedSet.stream()
                            .map(obj -> (Integer) obj)
                            .filter(pid -> !finalSolvedProblems.contains(pid))
                            .findFirst();

                    if (recommendation.isPresent()) {
                        generateRecommendationsAsync(userEmail);

                        return problemRepository.findById(recommendation.get())
                                .orElse(null);
                    }
                }
            }
        }

        if (similarUsersList == null || similarUsersList.isEmpty()) {
            CompletableFuture.runAsync(() -> {
                try {
                    computeSimilarityForUser(userEmail);
                    generateRecommendationQueue(userEmail);
                } catch (Exception e) {
                    log.error("Error computing similarity for user: {}", userEmail, e);
                }
            });
        } else {
            generateRecommendationsAsync(userEmail);
        }

        return getRandomProblem(solvedProblems);
    }

    // BACKGROUND JOBS

    @Scheduled(fixedRate = 21600000, initialDelay = 21600000) // 6 hours
    public void preComputeAllRecommendations() {
        log.info("Starting scheduled Redis cache refresh...");
        long startTime = System.currentTimeMillis();

        try {
            updateAvailableProblems();

            loadAllUserProfiles();

            computeSimilarityMatrix();

            generateAllRecommendations();

            redisTemplate.opsForValue().set(KEY_CACHE_INITIALIZED, true);

            long duration = System.currentTimeMillis() - startTime;
            log.info("Redis cache refresh completed in {}ms", duration);

        } catch (Exception e) {
            log.error("Scheduled cache refresh failed", e);
        }
    }

    @Scheduled(fixedRate = 3600000)
    public void updateAvailableProblems() {
        try {
            List<Integer> problems = problemRepository
                    .findByDeletedFalseAndIsContestProblemFalse()
                    .stream()
                    .map(Problem::getId)
                    .toList();

            redisTemplate.delete(KEY_AVAILABLE_PROBLEMS);
            if (!problems.isEmpty()) {
                redisTemplate.opsForSet().add(KEY_AVAILABLE_PROBLEMS,
                        problems.toArray());
            }

            log.info("Updated {} available problems in Redis", problems.size());

        } catch (Exception e) {
            log.error("Failed to update available problems", e);
        }
    }

    @Async
    public void onNewSubmission(String userEmail, Integer problemId) {
        try {
            log.debug("Processing new submission: user={}, problem={}", userEmail, problemId);

            String solvedKey = PREFIX_USER_SOLVED + userEmail;
            redisTemplate.opsForSet().add(solvedKey, problemId);
            redisTemplate.expire(solvedKey, PROFILE_TTL, TimeUnit.SECONDS);

            computeRecommendationsAsync(userEmail);

            String similarKey = PREFIX_SIMILAR_USERS + userEmail;
            List<Object> similarUsers = redisTemplate.opsForList()
                    .range(similarKey, 0, 4); // Top 5

            if (similarUsers != null) {
                similarUsers.stream()
                        .map(obj -> (String) obj)
                        .forEach(this::computeRecommendationsAsync);
            }

        } catch (Exception e) {
            log.error("Error handling new submission", e);
        }
    }

    // HELPER METHODS

    private void loadAllUserProfiles() {
        log.info("Loading all user profiles to Redis...");

        try {
            List<Submission> allSubmissions = submissionRepository.findAll();

            Map<String, Set<Integer>> profiles = allSubmissions.stream()
                    .collect(Collectors.groupingBy(
                            Submission::getCreatedBy,
                            Collectors.mapping(
                                    Submission::getPid,
                                    Collectors.toSet()
                            )
                    ));

            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                profiles.forEach((email, problems) -> {
                    String key = PREFIX_USER_SOLVED + email;
                    redisTemplate.delete(key);
                    if (!problems.isEmpty()) {
                        redisTemplate.opsForSet().add(key, problems.toArray());
                        redisTemplate.expire(key, PROFILE_TTL, TimeUnit.SECONDS);
                    }
                });
                return null;
            });

            log.info("Loaded {} user profiles to Redis", profiles.size());

        } catch (Exception e) {
            log.error("Failed to load user profiles", e);
        }
    }

    private void computeSimilarityMatrix() {
        log.info("Computing similarity matrix in Redis...");

        try {
            Set<String> allUserKeys = redisTemplate.keys(PREFIX_USER_SOLVED + "*");

            if (allUserKeys.isEmpty()) {
                log.warn("No user profiles found in Redis");
                return;
            }

            List<String> allUsers = allUserKeys.stream()
                    .map(key -> key.substring(PREFIX_USER_SOLVED.length()))
                    .toList();

            log.info("Computing similarity for {} users", allUsers.size());

            allUsers.parallelStream().forEach(user -> {
                try {
                    computeSimilarityForUser(user);
                } catch (Exception e) {
                    log.error("Error computing similarity for user: {}", user, e);
                }
            });

            log.info("Similarity matrix computation completed");

        } catch (Exception e) {
            log.error("Failed to compute similarity matrix", e);
        }
    }

    private void computeSimilarityForUser(String targetUser) {
        String targetKey = PREFIX_USER_SOLVED + targetUser;
        Set<Object> targetProblems = redisTemplate.opsForSet().members(targetKey);

        if (targetProblems == null || targetProblems.isEmpty()) {
            return;
        }

        Set<Integer> targetProblemIds = targetProblems.stream()
                .map(obj -> (Integer) obj)
                .collect(Collectors.toSet());

        Set<String> allUserKeys = redisTemplate.keys(PREFIX_USER_SOLVED + "*");

        List<UserSimilarity> similarities = new ArrayList<>();

        for (String otherKey : allUserKeys) {
            String otherUser = otherKey.substring(PREFIX_USER_SOLVED.length());

            if (otherUser.equals(targetUser)) {
                continue;
            }

            Set<Object> otherProblems = redisTemplate.opsForSet().members(otherKey);

            if (otherProblems == null || otherProblems.isEmpty()) {
                continue;
            }

            Set<Integer> otherProblemIds = otherProblems.stream()
                    .map(obj -> (Integer) obj)
                    .collect(Collectors.toSet());

            Set<Integer> intersection = new HashSet<>(targetProblemIds);
            intersection.retainAll(otherProblemIds);

            Set<Integer> union = new HashSet<>(targetProblemIds);
            union.addAll(otherProblemIds);

            double similarity = union.isEmpty() ? 0.0 :
                    (double) intersection.size() / union.size();

            if (similarity > 0.1) {
                similarities.add(new UserSimilarity(otherUser, similarity));
            }
        }

        List<String> topSimilar = similarities.stream()
                .sorted(Comparator.comparingDouble(
                        UserSimilarity::getSimilarity).reversed())
                .limit(20)
                .map(UserSimilarity::getEmail)
                .toList();

        if (!topSimilar.isEmpty()) {
            String similarKey = PREFIX_SIMILAR_USERS + targetUser;
            redisTemplate.delete(similarKey);
            redisTemplate.opsForList().rightPushAll(similarKey, topSimilar.toArray());
            redisTemplate.expire(similarKey, SIMILARITY_TTL, TimeUnit.SECONDS);
        }
    }

    private void generateAllRecommendations() {
        log.info("Generating recommendations for all users...");

        Set<String> allUserKeys = redisTemplate.keys(PREFIX_USER_SOLVED + "*");

        List<String> allUsers = allUserKeys.stream()
                .map(key -> key.substring(PREFIX_USER_SOLVED.length()))
                .toList();

        int total = allUsers.size();
        int[] processed = {0};

        allUsers.parallelStream().forEach(user -> {
            try {
                generateRecommendationQueue(user);

                synchronized (processed) {
                    processed[0]++;
                    if (processed[0] % 1000 == 0) {
                        log.info("Generated recommendations: {}/{}", processed[0], total);
                    }
                }
            } catch (Exception e) {
                log.error("Error generating recommendations for user: {}", user, e);
            }
        });

        log.info("Recommendations generation completed");
    }

    private void generateRecommendationQueue(String userEmail) {
        String solvedKey = PREFIX_USER_SOLVED + userEmail;
        Set<Object> solvedSet = redisTemplate.opsForSet().members(solvedKey);

        Set<Integer> solvedProblems = new HashSet<>();
        if (solvedSet != null) {
            solvedProblems = solvedSet.stream()
                    .map(obj -> (Integer) obj)
                    .collect(Collectors.toSet());
        }

        String similarKey = PREFIX_SIMILAR_USERS + userEmail;
        List<Object> similarUsers = redisTemplate.opsForList().range(similarKey, 0, -1);

        List<Integer> recommendations = new ArrayList<>();
        Set<Integer> finalSolvedProblems = solvedProblems;

        if (similarUsers != null && !similarUsers.isEmpty()) {
            // Collect problems từ similar users
            for (Object similarUserObj : similarUsers) {
                String similarUser = (String) similarUserObj;
                String similarSolvedKey = PREFIX_USER_SOLVED + similarUser;

                Set<Object> similarProblems = redisTemplate.opsForSet()
                        .members(similarSolvedKey);

                if (similarProblems != null) {
                    similarProblems.stream()
                            .map(obj -> (Integer) obj)
                            .filter(pid -> !finalSolvedProblems.contains(pid))
                            .limit(10)
                            .forEach(recommendations::add);
                }

                if (recommendations.size() >= 50) {
                    break;
                }
            }
        }

        if (recommendations.size() < 20) {
            Set<Object> availableSet = redisTemplate.opsForSet()
                    .members(KEY_AVAILABLE_PROBLEMS);

            if (availableSet != null) {
                availableSet.stream()
                        .map(obj -> (Integer) obj)
                        .filter(pid -> !finalSolvedProblems.contains(pid))
                        .limit(30 - recommendations.size())
                        .forEach(recommendations::add);
            }
        }

        if (!recommendations.isEmpty()) {
            String queueKey = PREFIX_RECOMMENDATION_QUEUE + userEmail;
            redisTemplate.delete(queueKey);
            redisTemplate.opsForList().rightPushAll(queueKey,
                    recommendations.toArray());
            redisTemplate.expire(queueKey, RECOMMENDATION_TTL, TimeUnit.SECONDS);
        }
    }

    @Async
    protected void generateRecommendationsAsync(String userEmail) {
        generateRecommendationQueue(userEmail);
    }

    @Async
    protected void refillRecommendationsAsync(String userEmail) {
        generateRecommendationQueue(userEmail);
    }

    private Set<Integer> loadUserSolvedProblemsFromDB(String userEmail) {
        try {
            Set<Integer> problems = submissionRepository.findByCreatedBy(userEmail)
                    .stream()
                    .map(Submission::getPid)
                    .collect(Collectors.toSet());

            if (!problems.isEmpty()) {
                String key = PREFIX_USER_SOLVED + userEmail;
                redisTemplate.opsForSet().add(key, problems.toArray());
                redisTemplate.expire(key, PROFILE_TTL, TimeUnit.SECONDS);
            }

            return problems;

        } catch (Exception e) {
            log.error("Error loading user problems from DB", e);
            return Collections.emptySet();
        }
    }

    private boolean isValidProblem(Integer problemId) {
        try {
            Optional<Problem> problemOpt = problemRepository.findById(problemId);

            if (problemOpt.isEmpty()) {
                return false;
            }

            Problem problem = problemOpt.get();

            return !Boolean.TRUE.equals(problem.getDeleted())
                    && !Boolean.TRUE.equals(problem.getIsContestProblem());

        } catch (Exception e) {
            log.error("Error checking problem validity: {}", problemId, e);
            return false;
        }
    }

    private Problem getRandomProblem(Set<Integer> excludeProblems) {
        Set<Object> availableSet = redisTemplate.opsForSet()
                .members(KEY_AVAILABLE_PROBLEMS);

        if (availableSet == null || availableSet.isEmpty()) {
            return problemRepository.findByDeletedFalseAndIsContestProblemFalse()
                    .stream()
                    .filter(p -> !excludeProblems.contains(p.getId()))
                    .findAny()
                    .orElse(null);
        }

        List<Integer> candidates = availableSet.stream()
                .map(obj -> (Integer) obj)
                .filter(pid -> !excludeProblems.contains(pid))
                .collect(Collectors.toList());

        if (candidates.isEmpty()) {
            candidates = availableSet.stream()
                    .map(obj -> (Integer) obj)
                    .toList();
        }

        if (candidates.isEmpty()) {
            return null;
        }

        Integer randomId = candidates.get(random.nextInt(candidates.size()));

        return problemRepository.findById(randomId)
                .filter(p -> !Boolean.TRUE.equals(p.getIsContestProblem()))
                .orElse(null);
    }

    // ========== MONITORING & MANAGEMENT ==========

    public Map<String, Object> getCacheStats() {
        try {
            Long recCount = (long) redisTemplate.keys(PREFIX_RECOMMENDATION_QUEUE + "*").size();
            Long profileCount = (long) redisTemplate.keys(PREFIX_USER_SOLVED + "*").size();
            Long similarityCount = (long) redisTemplate.keys(PREFIX_SIMILAR_USERS + "*").size();

            Set<Object> availableSet = redisTemplate.opsForSet()
                    .members(KEY_AVAILABLE_PROBLEMS);
            Integer availableCount = availableSet != null ? availableSet.size() : 0;

            Boolean initialized = (Boolean) redisTemplate.opsForValue()
                    .get(KEY_CACHE_INITIALIZED);

            return Map.of(
                    "cacheInitialized", initialized != null ? initialized : false,
                    "recommendationQueues", recCount,
                    "userProfiles", profileCount,
                    "similarityMappings", similarityCount,
                    "availableProblems", availableCount,
                    "status", Boolean.TRUE.equals(initialized) ? "READY" : "INITIALIZING"
            );

        } catch (Exception e) {
            log.error("Error getting cache stats", e);
            return Map.of("error", e.getMessage());
        }
    }

    public Map<String, Object> getHealthStatus() {
        return getCacheStats();
    }

    public void warmUpCache() {
        log.info("Manual warm-up triggered - FULL preload");

        long startTime = System.currentTimeMillis();

        try {
            log.info("[1/4] Loading available problems...");
            updateAvailableProblems();

            log.info("[2/4] Loading user profiles...");
            loadAllUserProfiles();

            log.info("[3/4] Computing similarity matrix...");
            computeSimilarityMatrix();

            log.info("[4/4] Generating recommendations...");
            generateAllRecommendations();

            redisTemplate.opsForValue().set(KEY_CACHE_INITIALIZED, true);

            long duration = System.currentTimeMillis() - startTime;
            log.info("Full cache warm-up completed in {}ms (~{}s)",
                    duration, duration / 1000);

        } catch (Exception e) {
            log.error("Manual warm-up failed", e);
            throw new RuntimeException("Cache warm-up failed", e);
        }
    }

    public void clearCache() {
        log.info("Clearing Redis cache...");

        try {
            Set<String> allKeys = redisTemplate.keys("cf:*");
            if (!allKeys.isEmpty()) {
                redisTemplate.delete(allKeys);
            }

            log.info("Redis cache cleared");

        } catch (Exception e) {
            log.error("Error clearing cache", e);
        }
    }

}
