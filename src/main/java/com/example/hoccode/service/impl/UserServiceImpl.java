package com.example.hoccode.service.impl;

import com.example.hoccode.advice.AppException;
import com.example.hoccode.advice.ErrorCode;
import com.example.hoccode.advice.exception.BadRequestException;
import com.example.hoccode.advice.exception.DuplicateRecordException;
import com.example.hoccode.advice.exception.ResourceNotFoundException;
import com.example.hoccode.config.template.RabbitKafkaTemplate;
import com.example.hoccode.dto.mapper.UserMapper;
import com.example.hoccode.dto.request.UserUpdateRequest;
import com.example.hoccode.dto.request.create.CreateUserRequest;
import com.example.hoccode.dto.request.PasswordCreationRequest;
import com.example.hoccode.dto.response.pageable.ResultPaginationDTO;
import com.example.hoccode.dto.response.SimpInfoUserResponse;
import com.example.hoccode.dto.response.UserPointResponse;
import com.example.hoccode.dto.response.UserResponse;
import com.example.hoccode.entity.User;
import com.example.hoccode.entity.VerificationCode;
import com.example.hoccode.event.NotificationEvent;
import com.example.hoccode.repository.SubmissionRepository;
import com.example.hoccode.repository.UserRepository;
import com.example.hoccode.service.IStreakService;
import com.example.hoccode.service.UserService;
import com.example.hoccode.service.VerifyCodeService;
import com.example.hoccode.util.SecurityUtil;
import com.example.hoccode.util.constant.VerifyTypeEnum;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    VerifyCodeService verifyCodeService;
    PasswordEncoder passwordEncoder;
    UserMapper userMapper;
    SecurityUtil securityUtil;
    RabbitKafkaTemplate kafkaTemplate;
    IStreakService streakService;
    SubmissionRepository submissionRepository;

    /**
     * @param email - Input email
     * @return boolean indicating if the email already exited or not
     */
    @Override
    public boolean isEmailExistAndActive(String email) {
        return this.userRepository.existsByEmailAndActive(email, true);
    }

    /**
     * @param id - Input UserId
     * @return User Object on a given userId
     */
    @Override
    public User fetchUserById(String id) {
        return this.userRepository.findById(id).orElse(null);
    }

    /**
     * @return UserResponse Object - Info currentUser
     */
    @Override
    public UserResponse fetchMyInfo() {
        String email = securityUtil.getCurrentUserLogin().orElse(null);
        return this.userMapper.toUserResponse(Objects.requireNonNull(userRepository.findByEmail(email).orElse(null)));
    }

    /**
     * @param request - password
     */
    @Override
    public void createPassword(PasswordCreationRequest request) {
        var context = SecurityContextHolder.getContext();
        String email = context.getAuthentication().getName();

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (StringUtils.hasText(user.getPassword()))
            throw new AppException(ErrorCode.PASSWORD_EXISTED);

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
    }

    /**
     * @param email - Input email
     * @return User Object on a given email
     */
    @Override
    public User fetchUserByEmail(String email) {
        return this.userRepository.findByEmail(email).orElse(null);
    }

    /**
     * @param id - Input UserId
     * @return User Details based on a given data updated to database
     */
    @Override
    public UserResponse fetchResUserDtoById(String id) {
        User user = this.userRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("User", "userId", id)
        );

        return this.userMapper.toUserResponse(user);
    }

    /**
     * @param spec - filter
     * @param pageable - page, size, sort(field,asc(desc))
     * @return ResultPaginationDTO based on a given spec and pageable
     */
    @Override
    public ResultPaginationDTO fetchAllUser(Specification<User> spec, Pageable pageable) {
        Specification<User> finalSpec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.notEqual(root.get("role"), "ADMIN"));
        Page<User> pageUser = this.userRepository.findAll(finalSpec, pageable);
        ResultPaginationDTO rs = new ResultPaginationDTO();
        ResultPaginationDTO.Meta mt = new ResultPaginationDTO.Meta();

        mt.setPage(pageable.getPageNumber() + 1);
        mt.setPageSize(pageable.getPageSize());

        mt.setPages(pageUser.getTotalPages());
        mt.setTotal(pageUser.getTotalElements());

        rs.setMeta(mt);
        List<UserResponse> listUser = pageUser.getContent()
                .stream().map(this.userMapper::toUserResponse)
                .collect(Collectors.toList());

        rs.setResult(listUser);

        return rs;
    }

    /**
     * @param createUserRequest - Input CreateUserRequest Object
     * @return User Details based on a given data saved to database
     */
    @Transactional
    @Override
    public UserResponse handleCreateUser(CreateUserRequest createUserRequest) {
        User user = userRepository.findByEmail(createUserRequest.getEmail()).orElse(null);
        if (user != null) {
            if (user.getActive()) throw new DuplicateRecordException("USER ", "Email", createUserRequest.getEmail());
            else userRepository.delete(user);
        }

        User newUser = User.builder()
                .name(createUserRequest.getName())
                .dob(createUserRequest.getDob())
                .email(createUserRequest.getEmail())
                .password(passwordEncoder.encode(createUserRequest.getPassword()))
                .active(false)
                .role("USER")
                .isLocked(false)
                .build();
        VerificationCode verificationCode = verifyCodeService.findByEmail(createUserRequest.getEmail());

        if (verificationCode != null) {
            verifyCodeService.delete(verificationCode);
        }

        String code = RandomStringUtils.randomAlphanumeric(64);

        verifyCodeService.save(VerificationCode.builder()
                .code(code)
                .type(VerifyTypeEnum.REGISTER)
                .email(createUserRequest.getEmail())
                .exp(LocalDateTime.now()).build());

        userRepository.save(newUser);

        Map<String, String> param = new HashMap<>();
        param.put("code", code);
        param.put("name", newUser.getName());

        NotificationEvent notificationEvent = NotificationEvent.builder()
                .channel("EMAIL")
                .recipient(newUser.getEmail())
                .subject("Welcome to HocCode!!!")
                .param(param)
                .body("Hello, " + newUser.getName())
                .build();
        System.out.println("send notificationEvent: ");

        // Publish message to kafka
        kafkaTemplate.send("notification-delivery", notificationEvent);
        return this.userMapper.toUserResponse(newUser);
    }

//    /**
//     * @param id - Input UserId
//     * @param updateUserRequest - Input UpdateUserRequest Object
//     * @return User Details based on a given data updated to database
//     */
//    @Override
//    public UserResponse handleUpdateUser(String id, UpdateUserRequest updateUserRequest) {
//        User currentUser = this.userRepository.findById(id).orElseThrow(
//                () -> new ResourceNotFoundException("User", "userId", id)
//        );
//
//        currentUser.setName(updateUserRequest.getName());
//        currentUser.setGender(updateUserRequest.getGender());
//        currentUser.setDob(updateUserRequest.getDob());
//        currentUser.setAvatar(updateUserRequest.getAvatar());
//        currentUser = this.userRepository.save(currentUser);
//        return this.userMapper.toUserResponse(currentUser);
//    }

//    /**
//     * @param id          - Input UserId
//     * @param lockRequest - Input LockRequest Object
//     * @return User Details based on a given data updated to database
//     */
//    @Override
//    public UserResponse handlerLockUser(String id, LockRequest lockRequest) {
//        User user = userRepository.findByIdAndActive(id, true).orElseThrow(
//                () -> new ResourceNotFoundException("User", "userId", id)
//        );
//
//        user.setIsLocked(lockRequest.getLock());
//        userRepository.save(user);
//
//        return this.userMapper.toUserResponse(user);
//    }

    @Override
    public List<SimpInfoUserResponse> fetchUserByIdIn(List<String> ids) {
        return userRepository.findByIdIn(ids).stream().map(userMapper::toSimpInfoUserResponse).toList();
    }

    @Override
    public Boolean forgotPassword(String email) {
        User user = userRepository.findFirstByEmailAndActiveAndIsLocked(email, true, false).orElseThrow(
            () -> new AppException(ErrorCode.USER_NOT_EXISTED)
        );

        if (!StringUtils.hasText(user.getPassword()))
            throw new AppException(ErrorCode.LOGIN_WITH_GOOGLE);

        VerificationCode verificationCode = verifyCodeService.findByEmail(email);

        if (verificationCode != null) {
            verifyCodeService.delete(verificationCode);
        }

        String code = RandomStringUtils.randomAlphanumeric(64);

        verifyCodeService.save(VerificationCode.builder()
                .code(code)
                .type(VerifyTypeEnum.FORGOT_PASSWORD)
                .email(email)
                .exp(LocalDateTime.now()).build());

        Map<String, String> param = new HashMap<>();
        param.put("code", code);
        param.put("name", user.getName());

        NotificationEvent notificationEvent = NotificationEvent.builder()
                .channel("EMAIL")
                .recipient(email)
                .subject("Reset your password")
                .param(param)
                .build();

        // Publish message to kafka
        kafkaTemplate.send("forgot-password", notificationEvent);
        return true;
    }

    @Override
    public List<UserPointResponse> getLeaderBoard() {
        return userRepository.findTop9ByActiveTrueAndRoleOrderByPointDesc("USER").stream().map(userMapper::toUserPointResponse).toList();
    }

    @Override
    @Cacheable(value = "shortCache", key = "T(String).format('profile:%s', @securityUtil.getCurrentUserLogin().get())")
    public Map<String, Object> getUserStats(){
        Map<String, Object> userStats = new HashMap<>();
        User currentUser = userRepository.findByEmail(securityUtil.getCurrentUserLogin().orElse(null)).orElseThrow(() -> new BadRequestException("Login"));

        Map<String, Object> stats = getStats(currentUser);

        userStats.put("rank", stats.getOrDefault("rank", 999));
        userStats.put("info", userMapper.toUserResponse(currentUser));
        userStats.put("joined", stats.getOrDefault("joined", LocalDateTime.now()));
        userStats.put("streak", streakService.getStreak(currentUser.getEmail()));
        userStats.put("solved", submissionRepository.countAcceptedProblems(currentUser.getEmail()));
        userStats.put("point", stats.getOrDefault("point", 0));
        return userStats;
    }

    @Override
    public Map<String, Object> getStats(User user){
        Map<String, Object> stats = new HashMap<>();
        stats.put("rank", userRepository.getUserRankByEmail(securityUtil.getCurrentUserLogin().orElse(null)));
        stats.put("joined", user.getCreatedAt());
        stats.put("point", user.getPoint());

        return stats;
    }

    @Override
    public List<UserResponse> getAllUsers(){
        return userRepository.findAllOrderByLocked().stream().map(userMapper::toUserResponse).toList();
    }

    @Override
    public void lock(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new BadRequestException("Người dùng không tồn tại"));
        user.setIsLocked(!user.getIsLocked());
        userRepository.save(user);
    }

    @Override
    @CacheEvict(
            value = "shortCache",
            key = "T(String).format('profile:%s', @securityUtil.getCurrentUserLogin().get())"
    )
    @Transactional
    public void update(UserUpdateRequest request){
        User user = userRepository.findByEmail(securityUtil.getCurrentUserLogin().get()).orElseThrow(() -> new BadRequestException("Người dùng không tồn tại"));

        user.setAvatar(request.getAvatar());
        user.setName(request.getName());
        user.setDob(request.getDob());

        userRepository.save(user);
    }
}