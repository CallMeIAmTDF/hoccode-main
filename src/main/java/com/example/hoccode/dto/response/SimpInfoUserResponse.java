package com.example.hoccode.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SimpInfoUserResponse {
	String id;
	String email;
	String name;
	LocalDate dob;
	String avatar;
}