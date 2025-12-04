package com.example.hoccode.controller;

import com.example.hoccode.dto.SocketMessage;
import com.example.hoccode.service.redis.IInterviewRedis;
import com.example.hoccode.util.constant.SocketType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;
import java.util.Set;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class WebSocketVideoController {

    SimpMessagingTemplate messagingTemplate;
    IInterviewRedis redis;

    /**
     * User join vào room
     * Destination: /app/call/join
     */
    @MessageMapping("/call/join")
    public void joinCall(@Payload SocketMessage message, Principal principal) {
        String roomId = (String) ((Map) message.getData()).get("roomId");
        String userEmail = principal.getName();

        // Validate room tồn tại
        if (!redis.isRoomExist(roomId)) {
            log.warn("User {} tried to join non-existent room: {}", userEmail, roomId);

            SocketMessage errorMessage = new SocketMessage(
                    SocketType.ERROR,
                    "Room does not exist",
                    Map.of("roomId", roomId, "error", "ROOM_NOT_FOUND")
            );

            messagingTemplate.convertAndSendToUser(
                    userEmail,
                    "/queue/call",
                    errorMessage
            );
            return;
        }

        log.info("User {} joining call room: {}", userEmail, roomId);

        // Thêm user vào room trong Redis
        redis.addUserToRoom(roomId, userEmail);

        // Lấy danh sách participants
        Set<String> participants = redis.getRoomParticipants(roomId);

        // Thông báo cho các user khác trong room
        SocketMessage notification = new SocketMessage(
                SocketType.CALL_JOIN,
                "User joined",
                Map.of(
                        "roomId", roomId,
                        "userEmail", userEmail,
                        "participants", participants
                )
        );

        messagingTemplate.convertAndSend("/topic/call/" + roomId, notification);
    }

    /**
     * User gửi offer (SDP)
     * Destination: /app/call/offer
     */
    @MessageMapping("/call/offer")
    public void sendOffer(@Payload SocketMessage message, Principal principal) {
        Map<String, Object> data = (Map<String, Object>) message.getData();
        String roomId = (String) data.get("roomId");
        String targetUser = (String) data.get("targetUser");
        Object offer = data.get("offer");

        // Validate room và user có trong room
        if (!redis.isRoomExist(roomId)) {
            log.warn("Offer sent to non-existent room: {}", roomId);
            return;
        }

        if (!redis.isUserInRoom(roomId, principal.getName())) {
            log.warn("User {} not in room {} when sending offer", principal.getName(), roomId);
            return;
        }

        log.info("Sending offer from {} to {} in room {}", principal.getName(), targetUser, roomId);

        SocketMessage offerMessage = new SocketMessage(
                SocketType.CALL_OFFER,
                "WebRTC offer",
                Map.of(
                        "roomId", roomId,
                        "fromUser", principal.getName(),
                        "offer", offer
                )
        );

        // Gửi trực tiếp đến user cụ thể
        messagingTemplate.convertAndSendToUser(
                targetUser,
                "/queue/call",
                offerMessage
        );
    }

    /**
     * User gửi answer (SDP)
     * Destination: /app/call/answer
     */
    @MessageMapping("/call/answer")
    public void sendAnswer(@Payload SocketMessage message, Principal principal) {
        Map<String, Object> data = (Map<String, Object>) message.getData();
        String roomId = (String) data.get("roomId");
        String targetUser = (String) data.get("targetUser");
        Object answer = data.get("answer");

        // Validate
        if (!redis.isRoomExist(roomId) || !redis.isUserInRoom(roomId, principal.getName())) {
            log.warn("Invalid answer attempt from {} in room {}", principal.getName(), roomId);
            return;
        }

        log.info("Sending answer from {} to {} in room {}", principal.getName(), targetUser, roomId);

        SocketMessage answerMessage = new SocketMessage(
                SocketType.CALL_ANSWER,
                "WebRTC answer",
                Map.of(
                        "roomId", roomId,
                        "fromUser", principal.getName(),
                        "answer", answer
                )
        );

        messagingTemplate.convertAndSendToUser(
                targetUser,
                "/queue/call",
                answerMessage
        );
    }

    /**
     * User gửi ICE candidate
     * Destination: /app/call/ice-candidate
     */
    @MessageMapping("/call/ice-candidate")
    public void sendIceCandidate(@Payload SocketMessage message, Principal principal) {
        Map<String, Object> data = (Map<String, Object>) message.getData();
        String roomId = (String) data.get("roomId");
        String targetUser = (String) data.get("targetUser");
        Object candidate = data.get("candidate");

        // Validate (lightweight check cho ICE candidates vì có nhiều)
        if (!redis.isRoomExist(roomId)) {
            return;
        }

        log.debug("Sending ICE candidate from {} to {} in room {}", principal.getName(), targetUser, roomId);

        SocketMessage iceMessage = new SocketMessage(
                SocketType.CALL_ICE_CANDIDATE,
                "ICE candidate",
                Map.of(
                        "roomId", roomId,
                        "fromUser", principal.getName(),
                        "candidate", candidate
                )
        );

        messagingTemplate.convertAndSendToUser(
                targetUser,
                "/queue/call",
                iceMessage
        );
    }

    /**
     * User gửi screen share offer
     * Destination: /app/call/screen-offer
     */
    @MessageMapping("/call/screen-offer")
    public void sendScreenOffer(@Payload SocketMessage message, Principal principal) {
        Map<String, Object> data = (Map<String, Object>) message.getData();
        String roomId = (String) data.get("roomId");
        String targetUser = (String) data.get("targetUser");
        Object offer = data.get("offer");

        if (!redis.isRoomExist(roomId) || !redis.isUserInRoom(roomId, principal.getName())) {
            return;
        }

        log.info("Sending screen offer from {} to {} in room {}", principal.getName(), targetUser, roomId);

        SocketMessage offerMessage = new SocketMessage(
                SocketType.CALL_SCREEN_OFFER,
                "Screen share offer",
                Map.of(
                        "roomId", roomId,
                        "fromUser", principal.getName(),
                        "offer", offer
                )
        );

        messagingTemplate.convertAndSendToUser(
                targetUser,
                "/queue/call",
                offerMessage
        );
    }

    /**
     * User gửi screen share answer
     * Destination: /app/call/screen-answer
     */
    @MessageMapping("/call/screen-answer")
    public void sendScreenAnswer(@Payload SocketMessage message, Principal principal) {
        Map<String, Object> data = (Map<String, Object>) message.getData();
        String roomId = (String) data.get("roomId");
        String targetUser = (String) data.get("targetUser");
        Object answer = data.get("answer");

        if (!redis.isRoomExist(roomId) || !redis.isUserInRoom(roomId, principal.getName())) {
            return;
        }

        log.info("Sending screen answer from {} to {} in room {}", principal.getName(), targetUser, roomId);

        SocketMessage answerMessage = new SocketMessage(
                SocketType.CALL_SCREEN_ANSWER,
                "Screen share answer",
                Map.of(
                        "roomId", roomId,
                        "fromUser", principal.getName(),
                        "answer", answer
                )
        );

        messagingTemplate.convertAndSendToUser(
                targetUser,
                "/queue/call",
                answerMessage
        );
    }

    /**
     * User stop screen sharing
     * Destination: /app/call/screen-stop
     */
    @MessageMapping("/call/screen-stop")
    public void stopScreenShare(@Payload SocketMessage message, Principal principal) {
        String roomId = (String) ((Map) message.getData()).get("roomId");

        log.info("User {} stopped screen sharing in room {}", principal.getName(), roomId);

        SocketMessage notification = new SocketMessage(
                SocketType.CALL_SCREEN_STOP,
                "Screen share stopped",
                Map.of(
                        "roomId", roomId,
                        "userEmail", principal.getName()
                )
        );

        messagingTemplate.convertAndSend("/topic/call/" + roomId, notification);
    }

    /**
     * Sync code changes
     * Destination: /app/call/sync-code
     */
    @MessageMapping("/call/sync-code")
    public void syncCode(@Payload SocketMessage message, Principal principal) {
        Map<String, Object> data = (Map<String, Object>) message.getData();
        String roomId = (String) data.get("roomId");
        String code = (String) data.get("code");
        String language = (String) data.get("language");

        if (!redis.isRoomExist(roomId) || !redis.isUserInRoom(roomId, principal.getName())) {
            return;
        }

        // Broadcast to all other users in room
        Set<String> participants = redis.getRoomParticipants(roomId);
        for (String participant : participants) {
            if (!participant.equals(principal.getName())) {
                SocketMessage syncMessage = new SocketMessage(
                        SocketType.CALL_SYNC_CODE,
                        "Code updated",
                        Map.of(
                                "roomId", roomId,
                                "fromUser", principal.getName(),
                                "code", code,
                                "language", language
                        )
                );

                messagingTemplate.convertAndSendToUser(
                        participant,
                        "/queue/call",
                        syncMessage
                );
            }
        }
    }

    /**
     * Sync notes changes
     * Destination: /app/call/sync-notes
     */
    @MessageMapping("/call/sync-notes")
    public void syncNotes(@Payload SocketMessage message, Principal principal) {
        Map<String, Object> data = (Map<String, Object>) message.getData();
        String roomId = (String) data.get("roomId");
        String notes = (String) data.get("notes");

        if (!redis.isRoomExist(roomId) || !redis.isUserInRoom(roomId, principal.getName())) {
            return;
        }

        // Broadcast to all other users in room
        Set<String> participants = redis.getRoomParticipants(roomId);
        for (String participant : participants) {
            if (!participant.equals(principal.getName())) {
                SocketMessage syncMessage = new SocketMessage(
                        SocketType.CALL_SYNC_NOTES,
                        "Notes updated",
                        Map.of(
                                "roomId", roomId,
                                "fromUser", principal.getName(),
                                "notes", notes
                        )
                );

                messagingTemplate.convertAndSendToUser(
                        participant,
                        "/queue/call",
                        syncMessage
                );
            }
        }
    }

    /**
     * User media state change (mic/camera on/off)
     * Destination: /app/call/media-state
     */
    @MessageMapping("/call/media-state")
    public void handleMediaStateChange(@Payload SocketMessage message, Principal principal) {
        Map<String, Object> data = (Map<String, Object>) message.getData();
        String roomId = (String) data.get("roomId");
        Boolean audioEnabled = (Boolean) data.get("audioEnabled");
        Boolean videoEnabled = (Boolean) data.get("videoEnabled");

        if (!redis.isRoomExist(roomId) || !redis.isUserInRoom(roomId, principal.getName())) {
            return;
        }

        log.debug("User {} media state: audio={}, video={} in room {}",
                principal.getName(), audioEnabled, videoEnabled, roomId);

        // Gửi đến tất cả users khác trong room
        Set<String> participants = redis.getRoomParticipants(roomId);
        for (String participant : participants) {
            if (!participant.equals(principal.getName())) {
                SocketMessage stateMessage = new SocketMessage(
                        SocketType.CALL_MEDIA_STATE,
                        "User media state changed",
                        Map.of(
                                "roomId", roomId,
                                "fromUser", principal.getName(),
                                "audioEnabled", audioEnabled,
                                "videoEnabled", videoEnabled
                        )
                );

                messagingTemplate.convertAndSendToUser(
                        participant,
                        "/queue/call",
                        stateMessage
                );
            }
        }
    }

    /**
     * User focus change (tab visibility)
     * Destination: /app/call/focus-change
     */
    @MessageMapping("/call/focus-change")
    public void handleFocusChange(@Payload SocketMessage message, Principal principal) {
        Map<String, Object> data = (Map<String, Object>) message.getData();
        String roomId = (String) data.get("roomId");
        Boolean isFocused = (Boolean) data.get("isFocused");

        if (!redis.isRoomExist(roomId) || !redis.isUserInRoom(roomId, principal.getName())) {
            return;
        }

        log.debug("User {} focus changed to {} in room {}", principal.getName(), isFocused, roomId);

        // Gửi đến tất cả users khác trong room
        Set<String> participants = redis.getRoomParticipants(roomId);
        for (String participant : participants) {
            if (!participant.equals(principal.getName())) {
                SocketMessage focusMessage = new SocketMessage(
                        SocketType.CALL_FOCUS_CHANGE,
                        "User focus changed",
                        Map.of(
                                "roomId", roomId,
                                "fromUser", principal.getName(),
                                "isFocused", isFocused
                        )
                );

                messagingTemplate.convertAndSendToUser(
                        participant,
                        "/queue/call",
                        focusMessage
                );
            }
        }
    }

    /**
     * User leave/hangup
     * Destination: /app/call/leave
     */
    @MessageMapping("/call/leave")
    public void leaveCall(@Payload SocketMessage message, Principal principal) {
        String roomId = (String) ((Map) message.getData()).get("roomId");
        String userEmail = principal.getName();

        log.info("User {} leaving call room: {}", userEmail, roomId);

        // Remove user khỏi room trong Redis
        redis.removeUserFromRoom(roomId, userEmail);

        // Lấy participants còn lại
        Set<String> remainingParticipants = redis.getRoomParticipants(roomId);

        // Nếu room rỗng thì xóa room
        if (remainingParticipants.isEmpty()) {
            redis.deleteRoom(roomId);
            log.info("Room {} deleted (no participants)", roomId);
        } else {
            // Thông báo cho các user còn lại
            SocketMessage notification = new SocketMessage(
                    SocketType.CALL_LEAVE,
                    "User left",
                    Map.of(
                            "roomId", roomId,
                            "userEmail", userEmail,
                            "participants", remainingParticipants
                    )
            );

            messagingTemplate.convertAndSend("/topic/call/" + roomId, notification);
        }
    }
}