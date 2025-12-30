package com.example.hoccode.service.impl;

import com.example.hoccode.advice.exception.BadRequestException;
import com.example.hoccode.dto.SocketMessage;
import com.example.hoccode.dto.request.create.CreateSubmissionRequest;
import com.example.hoccode.dto.response.ProblemResponse;
import com.example.hoccode.dto.response.call.CodeRunnerResponse;
import com.example.hoccode.entity.Notification;
import com.example.hoccode.entity.Problem;
import com.example.hoccode.entity.Room;
import com.example.hoccode.entity.RoomUser;
import com.example.hoccode.repository.*;
import com.example.hoccode.service.IProblemService;
import com.example.hoccode.service.ISubmissionService;
import com.example.hoccode.service.MatchmakingService;
import com.example.hoccode.util.RandomStringUtil;
import com.example.hoccode.util.SecurityUtil;
import com.example.hoccode.util.constant.NotificationStatusEnum;
import com.example.hoccode.util.constant.NotificationTypeEnum;
import com.example.hoccode.util.constant.RoomStatus;
import com.example.hoccode.util.constant.SocketType;
import jakarta.transaction.Transactional;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MatchmakingServiceImpl implements MatchmakingService {

    private RabbitTemplate rabbitTemplate;
    private SimpMessagingTemplate webSocketTemplate;
    private RoomRepository roomRepo;
    private RoomUserRepository roomUserRepo;
    private ProblemRepository problemRepo;
    private SecurityUtil securityUtil;
    private ISubmissionService submissionService;
    private SubmissionRepository submissionRepo;
    IProblemService problemService;
    NotificationRepository notificationRepository;

    private int MAX_PLAYER = 2;

    @Override
    @Transactional
    public void joinMatchmaking() {
        String email = securityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new BadRequestException("Người dùng chưa được xác thực"));

        Optional<Room> activeRoom = roomRepo.findActiveRoomByUserId(email);
        if (activeRoom.isPresent()) {
            Room room = activeRoom.get();
            sendUserListToNewUser(room, email);
            webSocketTemplate.convertAndSendToUser(
                    email,
                    "/queue",
                    new SocketMessage(SocketType.ROOM_REJOIN, "Rejoined room",
                            new RoomJoinResponse(room.getId(), room.getStatus(), RoomStatus.STARTED.equals(room.getStatus()) ? problemService.problemDetail(room.getProblemId()) : null))
            );

            webSocketTemplate.convertAndSendToUser(
                    email,
                    "/queue",
                    new SocketMessage(SocketType.TIME_REMAIN, "Thời gian còn lại...", RoomStatus.STARTED.equals(room.getStatus()) ? room.getStartedAt() : room.getCreatedAt())
            );

            return;
        }

        rabbitTemplate.convertAndSend("matchmaking-queue", email);
        webSocketTemplate.convertAndSendToUser(
                email,
                "/queue",
                new SocketMessage(SocketType.STATUS, "Đang chờ ghép phòng...", null)
        );
    }

    @Transactional
    @RabbitListener(queues = "matchmaking-queue")
    public void processJoin(String email) {
        List<Room> openLobbies = roomRepo.findOpenLobbies();
        Room lobby = openLobbies.isEmpty() ? createNewLobby() : openLobbies.get(0);

//        long count = roomUserRepo.countByRoomId(lobby.getId());
        long count = roomUserRepo.countByRoomId(lobby.getId());

        RoomUser ru = RoomUser.builder()
                .roomId(lobby.getId())
                .userId(email)
                .score(0)
                .rank((int) (count + 1))
                .build();
        roomUserRepo.save(ru);

        notifyExistingUsers(lobby, email);
        sendUserListToNewUser(lobby, email);

        webSocketTemplate.convertAndSendToUser(
                email,
                "/queue",
                new SocketMessage(SocketType.TIME_REMAIN, "Thời gian còn lại...", lobby.getCreatedAt())
        );
        count += 1;
        if (count >= MAX_PLAYER) {
            startRoom(lobby);
        }
    }

    private Room createNewLobby() {
        Room lobby = Room.builder()
                .status(RoomStatus.LOBBY)
                .build();
        return roomRepo.save(lobby);
    }

    @Transactional
    protected void startRoom(Room room) {
        Problem randomProblem = problemRepo.findRandomProblem();
        room.setProblemId(randomProblem.getId());
        room.setStatus(RoomStatus.STARTED);
        room.setStartedAt(LocalDateTime.now());
        roomRepo.save(room);

        List<RoomUser> users = roomUserRepo.findByRoomId(room.getId());
        for (RoomUser ru : users) {
            webSocketTemplate.convertAndSendToUser(
                    ru.getUserId(),
                    "/queue",
                    new SocketMessage(SocketType.ROOM_START, "Phòng bắt đầu! Room ID: " + room.getId(),
                            new RoomJoinResponse(room.getId(), room.getStatus(), problemService.problemDetail(room.getProblemId())))
            );
        }
    }

    private void notifyExistingUsers(Room room, String newUserEmail) {
        List<RoomUser> users = roomUserRepo.findByRoomId(room.getId());
        for (RoomUser ru : users) {
            if (!ru.getUserId().equals(newUserEmail)) {
                webSocketTemplate.convertAndSendToUser(
                        ru.getUserId(),
                        "/queue",
                        new SocketMessage(SocketType.NEW_USER, newUserEmail,
                                new UserJoinResponse(newUserEmail))
                );
            }
        }
    }

    private void sendUserListToNewUser(Room room, String newUserEmail) {
        List<RoomUser> users = roomUserRepo.findByRoomId(room.getId());
        List<String> userEmails = users.stream()
                .sorted(Comparator.comparing(RoomUser::getId))
                .map(RoomUser::getUserId)
                .toList();
        webSocketTemplate.convertAndSendToUser(
                newUserEmail,
                "/queue",
                new SocketMessage(SocketType.USER_LIST, "Current users in room",
                        new UserListResponse(userEmails))
        );
    }

    @Override
    @Scheduled(fixedRate = 4000)
    @Transactional
    public void checkLobbyTimeouts() {
        List<Room> lobbies = roomRepo.findAllByStatus(RoomStatus.LOBBY);
        for (Room lobby : lobbies) {
            long minutesPassed = ChronoUnit.MINUTES.between(lobby.getCreatedAt(), LocalDateTime.now());
            if (minutesPassed >= 2) {
                long count = roomUserRepo.countByRoomId(lobby.getId());
                String message = count < 4
                        ? "Phòng đợi hủy do chưa đủ 4 người"
                        : "Phòng đợi hết thời gian";
                List<RoomUser> users = roomUserRepo.findByRoomId(lobby.getId());
                for (RoomUser ru : users) {
                    webSocketTemplate.convertAndSendToUser(
                            ru.getUserId(),
                            "/queue",
                            new SocketMessage(SocketType.ROOM_CANCEL, message, null)
                    );
                }
                roomUserRepo.deleteByRoomId(lobby.getId());
                roomRepo.delete(lobby);
            }
        }
    }

    @Override
    @Scheduled(fixedRate = 5000)
    @Transactional
    public void checkRoomTimeouts() {
        List<Room> activeRooms = roomRepo.findAllByStatus(RoomStatus.STARTED);
        for (Room room : activeRooms) {
            List<RoomUser> users = roomUserRepo.findByRoomId(room.getId());
            boolean allSubmitted = users.stream().allMatch(ru -> ru.getSubmissionId() != null);
            long minutesPassed = ChronoUnit.MINUTES.between(room.getStartedAt(), LocalDateTime.now());

            if (allSubmitted || minutesPassed >= 15) {
                room.setStatus(RoomStatus.FINISHED);
                room.setFinishedAt(LocalDateTime.now());
                roomRepo.save(room);
                updateLeaderboard(room.getId());
                List<Notification> notifications = new ArrayList<>();
                for (RoomUser ru : users) {
                    webSocketTemplate.convertAndSendToUser(
                            ru.getUserId(),
                            "/queue",
                            new SocketMessage(SocketType.ROOM_END, "Phòng kết thúc! Room ID: " + room.getId(),
                                    new RoomEndResponse(room.getId(), ru.getRank()))
                    );

                    webSocketTemplate.convertAndSendToUser(
                            ru.getUserId(),
                            "/topic/notification",
                            new SocketMessage(SocketType.NEW_NOTIFICATION, "Thông báo mới",
                                    null)
                    );

                    notifications.add(Notification.builder()
                            .title("Trận đấu kết thúc")
                            .type(NotificationTypeEnum.COMBAT)
                            .message("Bạn đã đạt được thứ hạng #" + ru.getRank())
                            .receiver(ru.getUserId())
                            .status(NotificationStatusEnum.SEND)
                            .build());
                }
                notificationRepository.saveAll(
                    notifications
                );
            }
        }
    }

    @Override
    @Transactional
    public void handleSubmission(String roomId, CreateSubmissionRequest submissionRequest) {
        String email = securityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new BadRequestException("Người dùng chưa được xác thực"));

        Room room = roomRepo.findById(roomId)
                .orElseThrow(() -> new BadRequestException("Không tìm thấy phòng"));

        if (!room.getStatus().equals(RoomStatus.STARTED)) {
            throw new BadRequestException("Phòng không ở trạng thái ĐÃ BẮT ĐẦU");
        }
        if (!submissionRequest.getPid().equals(room.getProblemId())) {
            throw new BadRequestException("ID thử thách gửi không khớp với ID thử thách phòng");
        }

        RoomUser roomUser = roomUserRepo.findByRoomIdAndUserId(roomId, email)
                .orElseThrow(() -> new BadRequestException("Người dùng không có trong phòng"));
        if (roomUser.getSubmissionId() != null) {
            throw new BadRequestException("Người dùng đã gửi trước đó");
        }

        String submissionId = RandomStringUtil.generateRandomString(8);

        roomUser.setSubmissionId(submissionId);
        roomUser.setSubmissionTime(LocalDateTime.now());
        roomUserRepo.save(roomUser);

        webSocketTemplate.convertAndSendToUser(
                email,
                "/queue",
                new SocketMessage(SocketType.SUBMIT_RESPONSE, "Bài nộp đang được chấm", null)
        );

        CodeRunnerResponse codeRunnerResponse = submissionService.run(submissionRequest, false);

        Optional<RoomUser> roomUserOpt = roomUserRepo.findBySubmissionId(submissionId);
        if (roomUserOpt.isPresent()) {
            RoomUser ru = roomUserOpt.get();
            ru.setScore(codeRunnerResponse.getPassed() != null ? codeRunnerResponse.getPassed() : 0);
            roomUserRepo.save(ru);
            updateLeaderboard(ru.getRoomId());

            webSocketTemplate.convertAndSendToUser(
                    ru.getUserId(),
                    "/queue",
                    new SocketMessage(SocketType.SUBMIT_RESPONSE, "Kết quả chạy: " + codeRunnerResponse.getStatus(),
                            new SubmissionResponse(submissionId, codeRunnerResponse.getStatus(), codeRunnerResponse.getPassed()))
            );
        }
    }


    @Override
    @Transactional
    public void updateLeaderboard(String roomId) {
        List<RoomUser> users = roomUserRepo.findByRoomIdOrderByScoreDesc(roomId);
        int rank = 1;
        for (RoomUser ru : users) {
            ru.setRank(rank++);
            roomUserRepo.save(ru);
        }

        List<LeaderboardEntry> leaderboardEntries = users.stream()
                .map(u -> LeaderboardEntry.builder()
                        .score(u.getScore())
                        .username(u.getUserId())
                        .rank(u.getRank())
                        .build())
                .toList();

        LeaderboardResponse response = new LeaderboardResponse(leaderboardEntries);
        SocketMessage message = new SocketMessage(SocketType.LEADERBOARD, "Leaderboard updated", response);
        for (RoomUser ru : users) {
            webSocketTemplate.convertAndSendToUser(
                    ru.getUserId(),
                    "/queue",
                    message
            );
        }
    }
}

@Builder
@Getter
@AllArgsConstructor
class RoomJoinResponse {
    private String roomId;
    private RoomStatus status;
    private ProblemResponse problem;
}

@Builder
@Getter
@AllArgsConstructor
class SubmissionResponse {
    private String submissionId;
    private String status;
    private Integer passedTestCase;
}


@Builder
@Getter
@AllArgsConstructor
class UserJoinResponse {
    private String username;
}

@Getter
@Builder
@AllArgsConstructor
class UserListResponse {
    private List<String> usernames;
}

@Builder
@Getter
@AllArgsConstructor
class LeaderboardEntry {
    private String username;
    private Integer rank;
    private Integer score;
}

@Builder
@Getter
@AllArgsConstructor
class LeaderboardResponse {
    private List<LeaderboardEntry> leaderboard;
}

@Builder
@Getter
@AllArgsConstructor
class RoomEndResponse {
    private String roomId;
    private Integer rank;
}