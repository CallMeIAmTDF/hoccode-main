package com.example.hoccode.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "solutions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Solution extends BaseEntity{
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    Integer id;

    String language;

    @Column(columnDefinition = "LONGTEXT")
    String code;

    String time;

    String space;

    String note;

    Integer pid;

    @Builder.Default
    Boolean deleted = false;
}
