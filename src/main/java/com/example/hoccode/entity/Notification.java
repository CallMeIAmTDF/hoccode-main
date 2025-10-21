package com.example.hoccode.entity;

import com.example.hoccode.util.constant.NotificationStatusEnum;
import com.example.hoccode.util.constant.NotificationTypeEnum;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@EntityListeners(AuditingEntityListener.class)
public class Notification extends BaseEntity{
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    Integer id;

    String receiver;

    @Column(name = "redirect_url")
    String redirectUrl;

    String message;

    String title;

    @Enumerated(EnumType.STRING)
    NotificationStatusEnum status;

    @Enumerated(EnumType.STRING)
    NotificationTypeEnum type;
}