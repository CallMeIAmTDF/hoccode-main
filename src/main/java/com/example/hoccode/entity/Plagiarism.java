package com.example.hoccode.entity;

import com.example.hoccode.util.constant.PlagiarismStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "plagiarism")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Plagiarism extends BaseEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    Integer id;

    String file;

    Integer cid;

    @Enumerated(EnumType.STRING)
    PlagiarismStatus status;
}
