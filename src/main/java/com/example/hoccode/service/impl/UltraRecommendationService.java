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

    /**
     * Handle cache miss: Return random NGAY, compute async
     * Đảm bảo response time luôn nhanh (< 50ms)
     */
    private Problem handleCacheMiss(String userEmail) {
        log.info("Cache miss for user: {}. Returning random and computing async.", userEmail);

        // Step 1: Get solved problems từ Redis (nếu có)
        String solvedKey = PREFIX_USER_SOLVED + userEmail;
        Set<Object> solvedSet = redisTemplate.opsForSet().members(solvedKey);

        Set<Integer> solvedProblems = new HashSet<>();
        if (solvedSet != null && !solvedSet.isEmpty()) {
            solvedProblems = solvedSet.stream()
                    .map(obj -> (Integer) obj)
                    .collect(Collectors.toSet());
        }

        // Step 2: Return random problem NGAY (không đợi)
        Problem randomProblem = getRandomProblem(solvedProblems);

        // Step 3: Trigger ASYNC computation cho lần sau
        computeRecommendationsAsync(userEmail);

        return randomProblem;
    }

    /**
     * ASYNC: Compute recommendations cho user
     * Không block request, chạy background
     */
    @Async
    protected void computeRecommendationsAsync(String userEmail) {
        try {
            log.info("Starting async computation for user: {}", userEmail);
            long startTime = System.currentTimeMillis();

            // Step 1: Load user's solved problems (nếu chưa có trong Redis)
            String solvedKey = PREFIX_USER_SOLVED + userEmail;
            Set<Object> existingSolved = redisTemplate.opsForSet().members(solvedKey);

            if (existingSolved == null || existingSolved.isEmpty()) {
                Set<Integer> solvedProblems = loadUserSolvedProblemsFromDB(userEmail);

                if (solvedProblems.isEmpty()) {
                    log.info("User {} has no submissions. Skipping computation.", userEmail);
                    return;
                }
            }

            // Step 2: Compute similarity với other users
            computeSimilarityForUser(userEmail);

            // Step 3: Generate recommendation queue
            generateRecommendationQueue(userEmail);

            long duration = System.currentTimeMillis() - startTime;
            log.info("✓ Async computation completed for user {} in {}ms", userEmail, duration);

        } catch (Exception e) {
            log.error("Error in async computation for user: {}", userEmail, e);
        }
    }

    /**
     * Ensure cache đã initialized
     * Nếu chưa, chỉ load essentials (fast path)
     */
    public void ensureCacheInitialized() {
        Boolean initialized = (Boolean) redisTemplate.opsForValue()
                .get(KEY_CACHE_INITIALIZED);

        if (Boolean.TRUE.equals(initialized)) {
            return;
        }

        // Cache chưa init - load minimal data để serve request ngay
        log.warn("Redis cache not fully initialized. Loading essentials...");
        lazyInitializeCache();
    }

    /**
     * Lazy initialization - CHỈ load essentials (< 500ms)
     * Full preload sẽ chạy background
     */
    private void lazyInitializeCache() {
        try {
            log.info("Starting MINIMAL lazy initialization...");
            long startTime = System.currentTimeMillis();

            // Step 1: Load available problems (CRITICAL - must have)
            updateAvailableProblems();
            log.info("✓ Loaded available problems in {}ms",
                    System.currentTimeMillis() - startTime);

            // Step 2: Mark as partially initialized
            // Điều này ngăn các requests khác cũng chạy lazy init
//            redisTemplate.opsForValue().set(KEY_CACHE_INITIALIZED, true,
//                    1, TimeUnit.HOURS);

            redisTemplate.opsForValue().set(KEY_CACHE_INITIALIZED, true);
            redisTemplate.expire(KEY_CACHE_INITIALIZED, 1, TimeUnit.SECONDS);

            long duration = System.currentTimeMillis() - startTime;
            log.info("Minimal lazy initialization completed in {}ms", duration);
            log.info("System can now serve requests. Full preload will run in background.");

            // Step 3: Trigger FULL preload trong background (không đợi)
            CompletableFuture.runAsync(() -> {
                try {
                    log.info("Starting FULL background preload...");
                    long bgStart = System.currentTimeMillis();

                    loadAllUserProfiles();
                    computeSimilarityMatrix();
                    generateAllRecommendations();

                    long bgDuration = System.currentTimeMillis() - bgStart;
                    log.info("✓ Full background preload completed in {}ms", bgDuration);

                    // Set permanent initialized flag
                    redisTemplate.opsForValue().set(KEY_CACHE_INITIALIZED, true);

                } catch (Exception e) {
                    log.error("Background preload failed", e);
                }
            });

        } catch (Exception e) {
            log.error("Lazy initialization failed", e);
            // Không throw exception, để requests vẫn có thể serve (dù chậm)
        }
    }

    /**
     * Generate recommendation khi cache miss
     */
    private Problem generateRecommendationSync(String userEmail) {
        // Step 1: Get solved problems từ Redis
        String solvedKey = PREFIX_USER_SOLVED + userEmail;
        Set<Object> solvedSet = redisTemplate.opsForSet().members(solvedKey);

        Set<Integer> solvedProblems = new HashSet<>();
        if (solvedSet != null) {
            solvedProblems = solvedSet.stream()
                    .map(obj -> (Integer) obj)
                    .collect(Collectors.toSet());
        }

        // Nếu chưa có trong Redis, load từ DB
        if (solvedProblems.isEmpty()) {
            solvedProblems = loadUserSolvedProblemsFromDB(userEmail);
        }

        // Step 2: Get similar users từ Redis
        String similarKey = PREFIX_SIMILAR_USERS + userEmail;
        List<Object> similarUsersList = redisTemplate.opsForList()
                .range(similarKey, 0, -1);

        if (similarUsersList != null && !similarUsersList.isEmpty()) {
            Set<Integer> finalSolvedProblems = solvedProblems;

            // Duyệt qua similar users
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
                        // Async generate full queue
                        generateRecommendationsAsync(userEmail);

                        return problemRepository.findById(recommendation.get())
                                .orElse(null);
                    }
                }
            }
        }

        // Step 3: No similar users - compute now
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

        // Fallback: random problem
        return getRandomProblem(solvedProblems);
    }

    // ========== BACKGROUND JOBS ==========

    /**
     * Job 1: Full pre-computation
     * Chạy mỗi 6 giờ, HOẶC ngay sau app start (nếu enabled)
     */
    @Scheduled(fixedRate = 21600000, initialDelay = 21600000) // 6 hours, no immediate run
    public void preComputeAllRecommendations() {
        log.info("Starting scheduled Redis cache refresh...");
        long startTime = System.currentTimeMillis();

        try {
            // Step 1: Load available problems
            updateAvailableProblems();

            // Step 2: Load all user profiles to Redis
            loadAllUserProfiles();

            // Step 3: Compute similarity matrix
            computeSimilarityMatrix();

            // Step 4: Generate recommendations
            generateAllRecommendations();

            // Mark as initialized
            redisTemplate.opsForValue().set(KEY_CACHE_INITIALIZED, true);

            long duration = System.currentTimeMillis() - startTime;
            log.info("Redis cache refresh completed in {}ms", duration);

        } catch (Exception e) {
            log.error("Scheduled cache refresh failed", e);
        }
    }

    /**
     * Job 2: Update available problems
     */
    @Scheduled(fixedRate = 3600000)
    public void updateAvailableProblems() {
        try {
            List<Integer> problems = problemRepository
                    .findByDeletedFalseAndIsContestProblemFalse()
                    .stream()
                    .map(Problem::getId)
                    .toList();

            // Store to Redis set
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

    /**
     * Job 3: Handle new submission event
     * Update cache và regenerate recommendations
     */
    @Async
    public void onNewSubmission(String userEmail, Integer problemId) {
        try {
            log.debug("Processing new submission: user={}, problem={}", userEmail, problemId);

            // Update Redis set
            String solvedKey = PREFIX_USER_SOLVED + userEmail;
            redisTemplate.opsForSet().add(solvedKey, problemId);
            redisTemplate.expire(solvedKey, PROFILE_TTL, TimeUnit.SECONDS);

            // Regenerate recommendations cho user này
            computeRecommendationsAsync(userEmail);

            // Optional: update recommendations cho top 5 similar users
            // (vì user này vừa solve problem mới, similar users có thể recommend problem đó)
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

    // ========== HELPER METHODS ==========

    /**
     * Load tất cả user profiles vào Redis
     */
    private void loadAllUserProfiles() {
        log.info("Loading all user profiles to Redis...");

        try {
            List<Submission> allSubmissions = submissionRepository.findAll();

            // Group by user
            Map<String, Set<Integer>> profiles = allSubmissions.stream()
                    .collect(Collectors.groupingBy(
                            Submission::getCreatedBy,
                            Collectors.mapping(
                                    Submission::getPid,
                                    Collectors.toSet()
                            )
                    ));

            // Batch write to Redis với pipeline
            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                profiles.forEach((email, problems) -> {
                    String key = PREFIX_USER_SOLVED + email;

                    // Clear old data
                    redisTemplate.delete(key);

                    // Add all problems
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

    /**
     * Compute similarity matrix cho tất cả users
     */
    private void computeSimilarityMatrix() {
        log.info("Computing similarity matrix in Redis...");

        try {
            // Get all user emails từ Redis
            Set<String> allUserKeys = redisTemplate.keys(PREFIX_USER_SOLVED + "*");

            if (allUserKeys.isEmpty()) {
                log.warn("No user profiles found in Redis");
                return;
            }

            List<String> allUsers = allUserKeys.stream()
                    .map(key -> key.substring(PREFIX_USER_SOLVED.length()))
                    .toList();

            log.info("Computing similarity for {} users", allUsers.size());

            // Parallel processing
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

    /**
     * Compute similarity cho 1 user
     */
    private void computeSimilarityForUser(String targetUser) {
        String targetKey = PREFIX_USER_SOLVED + targetUser;
        Set<Object> targetProblems = redisTemplate.opsForSet().members(targetKey);

        if (targetProblems == null || targetProblems.isEmpty()) {
            return;
        }

        Set<Integer> targetProblemIds = targetProblems.stream()
                .map(obj -> (Integer) obj)
                .collect(Collectors.toSet());

        // Get all other users
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

            // Jaccard similarity
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

        // Sort và lấy top 20
        List<String> topSimilar = similarities.stream()
                .sorted(Comparator.comparingDouble(
                        UserSimilarity::getSimilarity).reversed())
                .limit(20)
                .map(UserSimilarity::getEmail)
                .toList();

        // Store to Redis
        if (!topSimilar.isEmpty()) {
            String similarKey = PREFIX_SIMILAR_USERS + targetUser;
            redisTemplate.delete(similarKey);
            redisTemplate.opsForList().rightPushAll(similarKey, topSimilar.toArray());
            redisTemplate.expire(similarKey, SIMILARITY_TTL, TimeUnit.SECONDS);
        }
    }

    /**
     * Generate recommendations cho tất cả users
     */
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

    /**
     * Generate recommendation queue cho 1 user
     */
    private void generateRecommendationQueue(String userEmail) {
        // Get solved problems
        String solvedKey = PREFIX_USER_SOLVED + userEmail;
        Set<Object> solvedSet = redisTemplate.opsForSet().members(solvedKey);

        Set<Integer> solvedProblems = new HashSet<>();
        if (solvedSet != null) {
            solvedProblems = solvedSet.stream()
                    .map(obj -> (Integer) obj)
                    .collect(Collectors.toSet());
        }

        // Get similar users
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

        // Fill với random problems nếu không đủ
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

        // Store to Redis
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

    /**
     * Load user solved problems từ DB
     */
    private Set<Integer> loadUserSolvedProblemsFromDB(String userEmail) {
        try {
            Set<Integer> problems = submissionRepository.findByCreatedBy(userEmail)
                    .stream()
                    .map(Submission::getPid)
                    .collect(Collectors.toSet());

            // Cache to Redis
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

    /**
     * Check xem problem có hợp lệ không (not deleted, not contest)
     */
    private boolean isValidProblem(Integer problemId) {
        try {
            Optional<Problem> problemOpt = problemRepository.findById(problemId);

            if (problemOpt.isEmpty()) {
                return false;
            }

            Problem problem = problemOpt.get();

            // Check không bị delete và không phải contest problem
            return !Boolean.TRUE.equals(problem.getDeleted())
                    && !Boolean.TRUE.equals(problem.getIsContestProblem());

        } catch (Exception e) {
            log.error("Error checking problem validity: {}", problemId, e);
            return false;
        }
    }

    /**
     * Get random problem (non-contest only)
     */
    private Problem getRandomProblem(Set<Integer> excludeProblems) {
        Set<Object> availableSet = redisTemplate.opsForSet()
                .members(KEY_AVAILABLE_PROBLEMS);

        if (availableSet == null || availableSet.isEmpty()) {
            // Fallback: query từ DB
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

        // Double check không phải contest
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
            // Step 1: Available problems
            log.info("[1/4] Loading available problems...");
            updateAvailableProblems();

            // Step 2: User profiles
            log.info("[2/4] Loading user profiles...");
            loadAllUserProfiles();

            // Step 3: Similarity matrix
            log.info("[3/4] Computing similarity matrix...");
            computeSimilarityMatrix();

            // Step 4: Generate recommendations
            log.info("[4/4] Generating recommendations...");
            generateAllRecommendations();

            // Mark as initialized
            redisTemplate.opsForValue().set(KEY_CACHE_INITIALIZED, true);

            long duration = System.currentTimeMillis() - startTime;
            log.info("✓ Full cache warm-up completed in {}ms (~{}s)",
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
