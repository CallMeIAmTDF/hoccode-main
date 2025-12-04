package com.example.hoccode.service.impl;

import com.example.hoccode.advice.exception.BadRequestException;
import com.example.hoccode.advice.exception.IdInvalidException;
import com.example.hoccode.dto.mapper.ProblemMapper;
import com.example.hoccode.dto.projection.CategoryCountProjection;
import com.example.hoccode.dto.request.ProblemSearchRequest;
import com.example.hoccode.dto.request.create.CreateProblemRequest;
import com.example.hoccode.dto.request.update.UpdateContestProblemRequest;
import com.example.hoccode.dto.request.update.UpdateProblemRequest;
import com.example.hoccode.dto.response.AdminProblemResponse;
import com.example.hoccode.dto.response.CategoryCountResponse;
import com.example.hoccode.dto.response.ProblemResponse;
import com.example.hoccode.entity.Hint;
import com.example.hoccode.entity.Problem;
import com.example.hoccode.entity.TestCase;
import com.example.hoccode.repository.*;
import com.example.hoccode.service.IProblemService;
import com.example.hoccode.service.redis.IStringStringIntegerRedis;
import com.example.hoccode.util.SecurityUtil;
import com.example.hoccode.util.constant.ProblemCategoryEnum;
import jakarta.persistence.criteria.Join;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProblemService implements IProblemService {
    ProblemRepository problemRepository;
    ProblemMapper problemMapper;
    TestCaseRepository testCaseRepository;
    HintRepository hintRepository;
    SecurityUtil securityUtil;
    IStringStringIntegerRedis redis;
    SubmissionRepository submissionRepository;

    @Override
    @Transactional
    public Problem create(CreateProblemRequest createProblemRequest, boolean isContest) {
        if(!isContest){
            if(!securityUtil.isAdmin()){
                throw new BadRequestException("Chỉ dành cho quản trị viên");
            }
        }

        Problem problem = Problem.builder()
                .description(createProblemRequest.getDescription())
                .constrains(createProblemRequest.getConstrains())
                .difficulty(createProblemRequest.getDifficulty())
                .followUp(createProblemRequest.getFollowUp())
                .memoryLimit(createProblemRequest.getMemoryLimit())
                .isContestProblem(isContest)
                .title(createProblemRequest.getTitle())
                .point(createProblemRequest.getPoint())
                .timeLimit(createProblemRequest.getTimeLimit())
                .hints(List.of())
                .categories(
                        createProblemRequest.getCategories() != null
                                ? createProblemRequest.getCategories()
                                : new ArrayList<>()
                )
                .allTestCases(List.of())
                .build();

        List<Hint> hints = new ArrayList<>();
        if (createProblemRequest.getHints() != null && !createProblemRequest.getHints().isEmpty()) {
            hints = createProblemRequest.getHints().stream().map(hint -> Hint.builder().problem(problem).description(hint).build()).collect(Collectors.toList());
        }
        problem.setHints(hints);

        List<TestCase> testCases = new ArrayList<>();
        if(createProblemRequest.getTestCases() != null && !createProblemRequest.getTestCases().isEmpty()){
            testCases = createProblemRequest.getTestCases().stream().map(
                    testCase -> TestCase.builder().problem(problem).expectedOutput(testCase.getExpectedOutput()).input(testCase.getInput()).isHidden(testCase.getIsHidden()).build()
            ).collect(Collectors.toList());
        }else{
            throw new BadRequestException("Testcase không được trống");
        }
        problem.setAllTestCases(testCases);



        return problemRepository.save(problem);
    }


    @Override
    public Page<ProblemResponse> getAll(ProblemSearchRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), Sort.by("id").ascending());
        Specification<Problem> spec = Specification.where(null);
        if (request.getKeyword() != null && !request.getKeyword().isEmpty()) {
            spec = spec.and((root, query, cb) ->
                    cb.like(cb.lower(root.get("title")), "%" + request.getKeyword().toLowerCase() + "%")
            );
        }
        if (request.getDifficulty() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("difficulty"), request.getDifficulty())
            );
        }
        if (request.getCategories() != null && !request.getCategories().isEmpty()) {
            spec = spec.and((root, query, cb) -> {
                Join<Problem, ProblemCategoryEnum> categoryJoin = root.join("categories");
                return categoryJoin.in(request.getCategories());
            });
        }
        spec = spec.and((root, query, cb) ->
                cb.isFalse(root.get("isContestProblem"))
        );

        spec = spec.and((root, query, cb) ->
                cb.isFalse(root.get("deleted"))
        );

        Set<Integer> finalAcceptedIds = getListAcceptedId();

        Page<Problem> problems = problemRepository.findAll(spec, pageable);
        List<Integer> problemIds = problems.stream().map(Problem::getId).toList();

        Map<String, Map<Integer, Long>> stats = getProblemStats(problemIds);
        Map<Integer, Long> passCountMap = stats.get("pass");
        Map<Integer, Long> failCountMap = stats.get("fail");

        return problems.map(p -> {
            ProblemResponse response = problemMapper.toProblemResponse(p, true, finalAcceptedIds);
            response.setPassCount(passCountMap.getOrDefault(p.getId(), 0L));
            response.setFailCount(failCountMap.getOrDefault(p.getId(), 0L));
            return response;
        });
    }

    @Override
    public List<AdminProblemResponse> getProblemForAdmin(){
        Set<Integer> fakeSet = new HashSet<>();
        List<Problem> problems = problemRepository.findAll();
        List<Integer> problemIds = problems.stream().map(Problem::getId).toList();
        Map<String, Map<Integer, Long>> stats = getProblemStats(problemIds);
        Map<Integer, Long> passCountMap = stats.get("pass");
        Map<Integer, Long> failCountMap = stats.get("fail");

        return problems.stream().filter(p -> !p.getIsContestProblem()).map(p -> {
            AdminProblemResponse response = problemMapper.toAdminProblemResponse(p);
            response.setPassCount(passCountMap.getOrDefault(p.getId(), 0L));
            response.setFailCount(failCountMap.getOrDefault(p.getId(), 0L));
            return response;
        }).toList();
    }

    @Override
    @Transactional
    public ProblemResponse update(Integer id, UpdateProblemRequest request) throws IdInvalidException {
        Problem problem = problemRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new IdInvalidException("Không tìm thấy thử thách"));

        if(problem.getIsContestProblem()){
            if(!problem.getCreatedBy().equals(securityUtil.getCurrentUserLogin().get())){
                throw new BadRequestException("Không thể truy cập tài nguyên này");
            }
        }else{
            if(!securityUtil.isAdmin()){
                throw new BadRequestException("Chỉ dành cho quản trị viên");
            }
        }
        if (request.getTitle() != null) problem.setTitle(request.getTitle());
        if (request.getDescription() != null) problem.setDescription(request.getDescription());
        if (request.getConstrains() != null) problem.setConstrains(request.getConstrains());
        if (request.getFollowUp() != null) problem.setFollowUp(request.getFollowUp());
        if (request.getTimeLimit() != null) problem.setTimeLimit(request.getTimeLimit());
        if (request.getMemoryLimit() != null) problem.setMemoryLimit(request.getMemoryLimit());
        if (request.getDifficulty() != null) problem.setDifficulty(request.getDifficulty());
        if (request.getPoint() != null) problem.setPoint(request.getPoint());
        if (request.getIsContestProblem() != null) problem.setIsContestProblem(request.getIsContestProblem());
        if (request.getCategories() != null) problem.setCategories(request.getCategories());
//        if (request.get)
        if (request.getTestCases() != null) {
            testCaseRepository.deleteAllByProblemId(problem.getId());
            List<TestCase> newTestCases = request.getTestCases().stream()
                    .map(dto -> {
                        TestCase testCase = new TestCase();
                        testCase.setProblem(problem);
                        testCase.setInput(dto.getInput());
                        testCase.setExpectedOutput(dto.getExpectedOutput());
                        testCase.setIsHidden(dto.getIsHidden() != null ? dto.getIsHidden() : true);
                        return testCase;
                    })
                    .collect(Collectors.toList());
            problem.setAllTestCases(newTestCases);
        }
        if (request.getHints() != null) {
            hintRepository.deleteAllByProblemId(problem.getId());
            List<Hint> newHints = request.getHints().stream()
                    .map(dto -> {
                        Hint hint = new Hint();
                        hint.setProblem(problem);
                        hint.setDescription(dto.getDescription());
                        return hint;
                    })
                    .collect(Collectors.toList());
            problem.setHints(newHints);
        }
        problemRepository.save(problem);
        return problemMapper.toPublicProblemResponse(problem, true);
    }

    @Override
    public ProblemResponse getDailyProblem() {
        Integer pid = redis.getDailyChallenge();
        Problem problem;
        if(pid != null){
            problem = problemRepository.findFirstByIdAndDeletedFalse(pid);
        }
        else{
            problem = problemRepository.getDailyProblem();
            redis.setDailyChallenge(problem.getId());
        }
        return getAProblem(problem);
    }

    @Override
    public List<CategoryCountResponse> getCategoryCounts() {
            List<CategoryCountProjection> used = problemRepository.countProblemsByCategory();
            Map<ProblemCategoryEnum, Long> countMap = used.stream()
                    .collect(Collectors.toMap(CategoryCountProjection::getCategory, CategoryCountProjection::getCount));
            return Arrays.stream(ProblemCategoryEnum.values())
                    .map(category -> new CategoryCountResponse(
                            category,
                            countMap.getOrDefault(category, 0L)))
                    .sorted(Comparator.comparingLong(CategoryCountResponse::count).reversed())
                    .toList();
    }

    @Override
    public ProblemResponse problemDetail(Integer id) {
        Problem problem = problemRepository.findByIdAndDeletedFalse(id).orElseThrow(() -> new BadRequestException("Problem not found"));
        if(problem.getIsContestProblem()){
            throw new BadRequestException("Thử thách cuộc thi");
        }
        return getAProblem(problem);
    }

    @Override
    public ProblemResponse problemDetailForContest(Integer id, Integer contestId) {
        Problem problem = problemRepository.findByIdAndDeletedFalse(id).orElseThrow(() -> new BadRequestException("Problem not found"));
        if(!problem.getIsContestProblem()){
            throw new BadRequestException("Không phải là thử thách của cuộc thi");
        }
        if(!Objects.equals(problem.getContestProblem().getContest().getId(), contestId)){
            throw new BadRequestException("Không phải là thử thách của cuộc thi này");
        }
        return getAProblem(problem);
    }

    @Override
    public Set<Integer> getListAcceptedId(){
        Optional<String> currentUserEmail = securityUtil.getCurrentUserLogin();
        Set<Integer> acceptedProblemIds = new HashSet<>();
        if (currentUserEmail.isPresent()) {
            acceptedProblemIds = submissionRepository.findAcceptedProblemIdsByUser(currentUserEmail.get());
        }
        return acceptedProblemIds;
    }

    @Override
    public Map<String, Map<Integer, Long>> getProblemStats(List<Integer> problemIds){
        Map<Integer, Long> passCountMap = new HashMap<>();
        Map<Integer, Long> failCountMap = new HashMap<>();
        for (Object[] row : submissionRepository.countSubmissionByProblemIds(problemIds)) {
            Integer pid = (Integer) row[0];
            Long pass = (Long) row[1];
            Long fail = (Long) row[2];
            passCountMap.put(pid, pass);
            failCountMap.put(pid, fail);
        }
        return Map.of(
                "pass", passCountMap,
                "fail", failCountMap
        );
    }

    public ProblemResponse getAProblem(Problem problem){
        Set<Integer> finalAcceptedIds = getListAcceptedId();
        List<Integer> problemIds = List.of(problem.getId());
        Map<String, Map<Integer, Long>> stats = getProblemStats(problemIds);
        Map<Integer, Long> passCountMap = stats.get("pass");
        Map<Integer, Long> failCountMap = stats.get("fail");
        ProblemResponse response = problemMapper.toProblemResponse(problem, true, finalAcceptedIds);
        response.setPassCount(passCountMap.getOrDefault(problem.getId(), 0L));
        response.setFailCount(failCountMap.getOrDefault(problem.getId(), 0L));
        return response;
    }

    @Override
    public Problem updateAllowedFields(Integer problemId, UpdateContestProblemRequest req) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy thử thách với id:" + problemId));

        problem.getHints().clear();
        problem.getTestCases().clear();


        problem.setHints(req.getHints() != null ? req.getHints().stream().map(hint -> Hint.builder().problem(problem).description(hint).build()).toList() : List.of());
        problem.setTestCases(req.getTestCases() == null ? List.of() : req.getTestCases().stream().map(testCase -> TestCase.builder().problem(problem).expectedOutput(testCase.getExpectedOutput()).input(testCase.getInput()).isHidden(testCase.getIsHidden()).build()).toList());
        if (req.getFollowUp() != null) {
            problem.setFollowUp(req.getFollowUp());
        }
        if (req.getConstrains() != null) {
            problem.setConstrains(req.getConstrains());
        }

        return problemRepository.save(problem);
    }


    @Override
    public String deleteProblem(Integer id){
        if(!securityUtil.isAdmin()){
            throw new BadRequestException("Không có quyền truy cập");
        }

        Problem problem = problemRepository.findFirstById(id).orElseThrow(() -> new BadRequestException("Thử thách không tồn tại"));
        boolean status = problem.getDeleted();
        problem.setDeleted(!problem.getDeleted());
        problemRepository.save(problem);
        if(!status) return "Xóa Thử thách thành công";
        return "Khôi phục Thử thách thành công";
    }
}