package com.example.hoccode.entity;

import com.example.hoccode.util.constant.StatusEnum;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
//@SqlResultSetMapping(
//        name = "SubmissionWithProblemTitleMapping",
//        classes = @ConstructorResult(
//                targetClass = SubmissionWithProblemTitleDTO.class,
//                columns = {
//                        @ColumnResult(name = "submissionId", type = String.class),
//                        @ColumnResult(name = "problemId", type = String.class),
//                        @ColumnResult(name = "code", type = String.class),
//                        @ColumnResult(name = "language", type = String.class),
//                        @ColumnResult(name = "username", type = String.class),
//                        @ColumnResult(name = "status", type = String.class),
//                        @ColumnResult(name = "problemTitle", type = String.class)
//                }
//        )
//)
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
