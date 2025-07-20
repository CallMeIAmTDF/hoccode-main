package com.example.hoccode.dto.mapper;

import com.example.hoccode.dto.response.SimpInfoUserResponse;
import com.example.hoccode.dto.response.UserPointResponse;
import com.example.hoccode.dto.response.UserResponse;
import com.example.hoccode.entity.User;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserMapper {

    public UserResponse toUserResponse(User user) {

        return UserResponse.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .dob(user.getDob())
                        .avatar(user.getAvatar())
                        .email(user.getEmail())
                        .noPassword(!StringUtils.hasText(user.getPassword()))
                        .isLocked(user.getIsLocked())
                        .build();
    }

    public SimpInfoUserResponse toSimpInfoUserResponse(User user) {
        return SimpInfoUserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .dob(user.getDob())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .build();
    }

    public UserPointResponse toUserPointResponse(User user) {
        return UserPointResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .point(user.getPoint() == null ? 0 : user.getPoint())
                .avatar(user.getAvatar() == null ? "https://ui-avatars.com/api/?background=random&format=png&name="+user.getName() : user.getAvatar())
                .build();
    }
}