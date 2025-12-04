package com.example.hoccode.service.impl;

import com.example.hoccode.advice.exception.BadRequestException;
import com.example.hoccode.dto.request.create.CreateSolutionRequest;
import com.example.hoccode.entity.Solution;
import com.example.hoccode.repository.SolutionRepository;
import com.example.hoccode.service.ISolutionService;
import com.example.hoccode.util.SecurityUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SolutionService implements ISolutionService {

    SolutionRepository repository;
    SecurityUtil securityUtil;
    Integer MAX_SOLUTIONS = 3;

//    @Transactional
//    @Override
//    public List<Solution> create(List<CreateSolutionRequest> requests) {
//        if (!securityUtil.isAdmin()) throw new BadRequestException("Không có quyền truy cập");
//        if (requests.isEmpty()) {
//            throw new BadRequestException("Danh sách rỗng");
//        }
//
//        List<Solution> creations = requests.stream().map(
//                t -> Solution.builder()
//                        .pid(t.getPid())
//                        .space(t.getSpace())
//                        .time(t.getTime())
//                        .note(t.getNote())
//                        .code(t.getCode())
//                        .language(t.getLanguage())
//                        .build()
//        ).toList();
//        return repository.saveAll(creations);
//    }

    @Override
    public Solution create(CreateSolutionRequest request) {
        if (!securityUtil.isAdmin()) throw new BadRequestException("Không có quyền truy cập");
        Solution solution = Solution.builder()
                .pid(request.getPid())
                .space(request.getSpace())
                .time(request.getTime())
                .note(request.getNote())
                .code(request.getCode())
                .language(request.getLanguage())
                .build();
        return repository.save(
                solution
        );
    }

    @Override
    public Solution update(CreateSolutionRequest request, Integer solutionId) {
        if (!securityUtil.isAdmin()) throw new BadRequestException("Không có quyền truy cập");
        Solution solution = repository.findFirstById(solutionId);
        if (solution == null) {
            throw new BadRequestException("Không tìm thấy lời giải");
        }
        String DEFAULT_TEXT = "__tdf__";
        if (!DEFAULT_TEXT.equals(request.getLanguage())) {
            solution.setLanguage(request.getLanguage());
        }
        if (!DEFAULT_TEXT.equals(request.getCode())) {
            solution.setCode(request.getCode());
        }
        if (!DEFAULT_TEXT.equals(request.getNote())) {
            solution.setNote(request.getNote());
        }
        if (request.getTime() != null && !DEFAULT_TEXT.equals(request.getTime())) {
            solution.setTime(request.getTime());
        }
        if (request.getSpace() != null && !DEFAULT_TEXT.equals(request.getSpace())) {
            solution.setSpace(request.getSpace());
        }
        return repository.save(solution);
    }

    @Override
    public List<Solution> getSolutionsForAdmin(Integer pid) {
        return repository.findAllByPid(pid);
    }

    @Override
    @Cacheable(value = "shortCache", key = "#pid.hashCode()")
    public List<Solution> getSolutions(Integer pid) {
        return repository.findAllByPidAndDeletedFalse(pid);
    }

    @Override
    public Solution delete(Integer id) {
        if (!securityUtil.isAdmin()) throw new BadRequestException("Không có quyền truy cập");
        Solution solution = repository.findFirstById(id);
        if (solution == null) {
            throw new BadRequestException("Không tìm thấy lời giải");
        }
        repository.delete(solution);
        return solution;
    }
}
