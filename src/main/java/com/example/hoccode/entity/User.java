package com.example.hoccode.entity;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "users")
public class User extends BaseEntity{
    @Id
    String id;
    String name;
    String email;
    String password;
    LocalDate dob;
    String role;
    Boolean isLocked;
    Boolean active;
    String avatar;

    @Column(name = "point")
    @Builder.Default
    private Integer point = 0;


    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference
    Streak streak;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<ContestParticipant> contestParticipants;

    public void incrementPoint(int point){
        this.point += point;
    }

    @PrePersist
    public void generateId() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }
}