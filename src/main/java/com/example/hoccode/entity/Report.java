package com.example.hoccode.entity;

import com.example.hoccode.util.constant.ReportTypeEnum;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reports_tbl")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(callSuper = true)
public class Report extends BaseEntity{
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    Integer id;

    String title;

    @Column(columnDefinition = "LONGTEXT")
    String description;

    @Enumerated(EnumType.STRING)
    ReportTypeEnum type;

    @Column(name = "target_id")
    String target;

    Boolean solved;

    @Column(columnDefinition = "LONGTEXT")
    String solved_message;
}
