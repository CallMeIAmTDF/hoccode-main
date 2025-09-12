package com.example.hoccode.entity;

import com.example.hoccode.util.constant.RoomStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true)
public class Room extends BaseEntity {
    @Id
    private String id;

    @Column(name = "problem_id")
    private Integer problemId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private RoomStatus status = RoomStatus.LOBBY;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "max_players")
    @Builder.Default
    private int maxPlayers = 8;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @PrePersist
    public void generateId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }
}