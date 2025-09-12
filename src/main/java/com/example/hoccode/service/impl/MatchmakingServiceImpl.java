package com.example.hoccode.service.impl;

import com.example.hoccode.dto.SocketMessage;
import com.example.hoccode.dto.request.create.CreateSubmissionRequest;
import com.example.hoccode.dto.response.call.CodeRunnerResponse;
import com.example.hoccode.entity.Problem;
import com.example.hoccode.entity.Room;
import com.example.hoccode.entity.RoomUser;
import com.example.hoccode.repository.ProblemRepository;
import com.example.hoccode.repository.RoomRepository;
import com.example.hoccode.repository.RoomUserRepository;
import com.example.hoccode.repository.SubmissionRepository;
import com.example.hoccode.service.ISubmissionService;
import com.example.hoccode.service.MatchmakingService;
import com.example.hoccode.util.RandomStringUtil;
import com.example.hoccode.util.SecurityUtil;
import com.example.hoccode.util.constant.RoomStatus;
import com.example.hoccode.util.constant.SocketType;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
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

    @Override
    @Transactional
    public void joinMatchmaking() {
        String email = securityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

        Optional<Room> activeRoom = roomRepo.findActiveRoomByUserId(email);
        if (activeRoom.isPresent()) {
            Room room = activeRoom.get();
            sendUserListToNewUser(room, email);
            webSocketTemplate.convertAndSendToUser(
                    email,
                    "/queue",
                    new SocketMessage(SocketType.ROOM_REJOIN, "Rejoined room",
                            new RoomJoinResponse(room.getId(), room.getStatus(), room.getProblemId()))
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

        RoomUser ru = RoomUser.builder()
                .roomId(lobby.getId())
                .userId(email)
                .build();
        roomUserRepo.save(ru);

        notifyExistingUsers(lobby, email);
        sendUserListToNewUser(lobby, email);

        long count = roomUserRepo.countByRoomId(lobby.getId());
        if (count >= 1) {
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
                            new RoomJoinResponse(room.getId(), room.getStatus(), room.getProblemId()))
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
                        new SocketMessage(SocketType.NEW_USER, "New user joined: " + newUserEmail,
                                new UserJoinResponse(newUserEmail))
                );
            }
        }
    }

    private void sendUserListToNewUser(Room room, String newUserEmail) {
        List<RoomUser> users = roomUserRepo.findByRoomId(room.getId());
        List<String> userEmails = users.stream()
                .map(RoomUser::getUserId)
                .filter(email -> !email.equals(newUserEmail))
                .toList();
        webSocketTemplate.convertAndSendToUser(
                newUserEmail,
                "/queue",
                new SocketMessage(SocketType.USER_LIST, "Current users in room",
                        new UserListResponse(userEmails))
        );
    }

    @Override
    @Scheduled(fixedRate = 10000)
    @Transactional
    public void checkLobbyTimeouts() {
        List<Room> lobbies = roomRepo.findAllByStatus(RoomStatus.LOBBY);
        for (Room lobby : lobbies) {
            long minutesPassed = ChronoUnit.MINUTES.between(lobby.getCreatedAt(), LocalDateTime.now());
            if (minutesPassed > 2) {
                long count = roomUserRepo.countByRoomId(lobby.getId());
                String message = count < 4
                        ? "Phòng đợi hủy do chưa đủ 4 người. Thoát phòng."
                        : "Phòng đợi hết thời gian, chưa đủ 8 người. Thoát phòng.";
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
    @Scheduled(fixedRate = 10000)
    @Transactional
    public void checkRoomTimeouts() {
        List<Room> activeRooms = roomRepo.findAllByStatus(RoomStatus.STARTED);
        for (Room room : activeRooms) {
            List<RoomUser> users = roomUserRepo.findByRoomId(room.getId());
            boolean allSubmitted = users.stream().allMatch(ru -> ru.getSubmissionId() != null);
            long minutesPassed = ChronoUnit.MINUTES.between(room.getStartedAt(), LocalDateTime.now());

            if (allSubmitted || minutesPassed > 15) {
                room.setStatus(RoomStatus.FINISHED);
                room.setFinishedAt(LocalDateTime.now());
                roomRepo.save(room);
                updateLeaderboard(room.getId());
                for (RoomUser ru : users) {
                    webSocketTemplate.convertAndSendToUser(
                            ru.getUserId(),
                            "/queue",
                            new SocketMessage(SocketType.ROOM_END, "Phòng kết thúc! Room ID: " + room.getId(),
                                    new RoomEndResponse(room.getId(), ru.getRank()))
                    );
                }
            }
        }
    }

    @Override
    @Transactional
    public void handleSubmission(String roomId, CreateSubmissionRequest submissionRequest) {
        String email = securityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new IllegalStateException("User not authenticated"));

        Room room = roomRepo.findById(roomId)
                .orElseThrow(() -> new IllegalStateException("Room not found"));

        if (!room.getStatus().equals(RoomStatus.STARTED)) {
            throw new IllegalStateException("Room is not in STARTED status");
        }
        if (!submissionRequest.getPid().equals(room.getProblemId())) {
            throw new IllegalStateException("Submission problem ID does not match room problem ID");
        }

        RoomUser roomUser = roomUserRepo.findByRoomIdAndUserId(roomId, email)
                .orElseThrow(() -> new IllegalStateException("User not in room"));
        if (roomUser.getSubmissionId() != null) {
            throw new IllegalStateException("User has already submitted");
        }

        String submissionId = RandomStringUtil.generateRandomString(8);

        roomUser.setSubmissionId(submissionId);
        roomUser.setSubmissionTime(LocalDateTime.now());
        roomUserRepo.save(roomUser);

        webSocketTemplate.convertAndSendToUser(
                email,
                "/queue",
                new SocketMessage(SocketType.STATUS, "Submission in progress:", null)
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
                    new SocketMessage(SocketType.STATUS, "Submission result: " + codeRunnerResponse.getStatus(),
                            new SubmissionResponse(submissionId, codeRunnerResponse.getStatus(), codeRunnerResponse.getPassed()))
            );
        }
    }

//    @RabbitListener(queues = "submission-result-queue")
//    @Transactional
//    public void processSubmissionResult(SubmissionUpdateRequest result) {
//        Submission submission = submissionRepo.findById(result.getId())
//                .orElseThrow(() -> new IllegalStateException("Submission not found"));
//        submission.setStatus(result.getStatus());
//        submission.setPassedTestCase(result.getPassedTestCase());
//        submission.setTotalTestCases(result.getTotalTestCases());
//        submission.setMessage(result.getMessage());
//        submission.setMaxTime(result.getMaxTime());
//        submissionRepo.save(submission);
//
//        Optional<RoomUser> roomUserOpt = roomUserRepo.findBySubmissionId(result.getId());
//        if (roomUserOpt.isPresent()) {
//            RoomUser roomUser = roomUserOpt.get();
//            roomUser.setScore(submission.getPassedTestCase() != null ? submission.getPassedTestCase() : 0);
//            roomUserRepo.save(roomUser);
//            updateLeaderboard(roomUser.getRoomId());
//
//            webSocketTemplate.convertAndSendToUser(
//                    roomUser.getUserId(),
//                    "/queue",
//                    new SocketMessage(SocketType.STATUS, "Submission result: " + submission.getStatus(),
//                            new SubmissionResponse(submission.getId(), submission.getStatus(), submission.getPassedTestCase()))
//            );
//        }
//    }

    @Override
    @Transactional
    public void updateLeaderboard(String roomId) {
        List<RoomUser> users = roomUserRepo.findByRoomIdOrderByScoreDesc(roomId);
        int rank = 1;
        for (RoomUser ru : users) {
            ru.setRank(rank++);
            roomUserRepo.save(ru);
        }

        for (RoomUser ru : users) {
            webSocketTemplate.convertAndSendToUser(
                    ru.getUserId(),
                    "/queue",
                    new SocketMessage(SocketType.LEADERBOARD, "Leaderboard updated",
                            new LeaderboardResponse(users.stream()
                                    .map(u -> new LeaderboardEntry(u.getUserId(), u.getRank(), u.getScore()))
                                    .toList()))
            );
        }
    }
}

@Getter
@AllArgsConstructor
class RoomJoinResponse {
    private String roomId;
    private RoomStatus status;
    private Integer problemId;
}

@Getter
@AllArgsConstructor
class SubmissionResponse {
    private String submissionId;
    private String status;
    private Integer passedTestCase;
}
@Getter
@AllArgsConstructor
class UserJoinResponse {
    private String userId;
}

@Getter
@AllArgsConstructor
class UserListResponse {
    private List<String> userIds;
}

@Getter
@AllArgsConstructor
class LeaderboardEntry {
    private String userId;
    private Integer rank;
    private Integer score;
}

@Getter
@AllArgsConstructor
class LeaderboardResponse {
    private List<LeaderboardEntry> leaderboard;
}

@Getter
@AllArgsConstructor
class RoomEndResponse {
    private String roomId;
    private Integer rank;
}