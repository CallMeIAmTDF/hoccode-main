package com.example.hoccode.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "discussion")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Discussion extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Integer id;

    String title;

    @Column(columnDefinition = "LONGTEXT")
    String description;

    Integer pid;

    @Column(name = "is_delete")
    Boolean delete;

    Integer view;

    public void incrementView(){
        this.view++;
    }
}
