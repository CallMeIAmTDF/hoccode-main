package com.example.hoccode.entity;

import com.example.hoccode.util.constant.StatusEnum;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "submissions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Submission extends BaseEntity{
    @Id
    String id;

    Integer pid;

    @Column(columnDefinition = "LONGTEXT")
    String code;

    String language;

    @Enumerated(EnumType.STRING)
    StatusEnum status;

    @Column(name = "total_test_cases")
    Integer totalTestCases;

    @Column(name = "passed_test_cases")
    Integer passedTestCase;

    @Nullable
    String message;

    @Column(name = "max_time")
    Integer maxTime;
}
