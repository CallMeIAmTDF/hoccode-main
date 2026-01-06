package com.example.hoccode.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "room_user", indexes = {
        @Index(name = "idx_room_user_room_id", columnList = "room_id")
}, uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "user_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true)
public class RoomUser extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private String roomId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "score")
    @Builder.Default
    private int score = 0;

    @Column(name = "user_rank")
    private Integer rank;

    @Column(name = "submission_id")
    private String submissionId;

    @Column(name = "submission_time")
    private LocalDateTime submissionTime;
}