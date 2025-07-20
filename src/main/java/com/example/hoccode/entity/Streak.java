package com.example.hoccode.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "streaks")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Streak extends BaseEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    Integer id;

    @OneToOne
    @JoinColumn(name = "uid")
    @JsonBackReference
    private User user;

    @Column(name = "max_streak")
    Integer maxStreak;

    @Column(name = "current_streak")
    Integer currentStreak;
}
