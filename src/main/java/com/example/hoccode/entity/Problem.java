package com.example.hoccode.entity;

import com.example.hoccode.util.constant.DifficultyEnum;
import com.example.hoccode.util.constant.ProblemCategoryEnum;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Where;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;

@Entity
@Table(name = "problems", indexes = {
        @Index(name = "idx_problem_difficulty", columnList = "difficulty"),
        @Index(name = "idx_problem_deleted", columnList = "deleted")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Problem extends BaseEntity{
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    Integer id;

    String title;

    @Column(columnDefinition = "LONGTEXT")
    String description;

    @Column(columnDefinition = "LONGTEXT")
    String constrains;

    @Column(name = "follow_up")
    String followUp;

    @Column(name = "time_limit")
    Integer timeLimit;

    @Column(name = "memory_limit")
    Integer memoryLimit;

    @Enumerated(EnumType.STRING)
    DifficultyEnum difficulty;

    @Column(name = "is_contest_problem")
    Boolean isContestProblem;

    Integer point;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    @Builder.Default
    Boolean deleted = false;

    @ElementCollection(targetClass = ProblemCategoryEnum.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "problem_categories", joinColumns = @JoinColumn(name = "problem_id"))
    @Column(name = "category")
    private List<ProblemCategoryEnum> categories;

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL)
    @JsonManagedReference
    private List<Hint> hints;

    @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<TestCase> allTestCases;

    @OneToMany
    @JoinColumn(name = "pid")
    @Where(clause = "is_hidden = false")
    @JsonManagedReference
    private List<TestCase> testCases;

    @OneToOne(mappedBy = "problem", cascade = CascadeType.ALL)
    @JsonIgnore
    private ContestProblem contestProblem;
}