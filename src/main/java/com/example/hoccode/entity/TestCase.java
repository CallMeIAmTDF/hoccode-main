package com.example.hoccode.entity;


import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Table(name = "testcases", indexes = {
        @Index(name = "idx_testcase_pid", columnList = "pid"),
        @Index(name = "idx_testcase_hidden", columnList = "is_hidden")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@EntityListeners(AuditingEntityListener.class)
public class TestCase extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    @ManyToOne
    @JoinColumn(name = "pid")
    @JsonBackReference
    Problem problem;
//    String pid;

    @Column(columnDefinition = "LONGTEXT")
    String input;

    @Column(name = "expected_output", columnDefinition = "LONGTEXT")
    String expectedOutput;

    @Column(name = "is_hidden")
    Boolean isHidden;

}
