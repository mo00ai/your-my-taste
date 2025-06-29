package com.example.taste.domain.user.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

import com.example.taste.domain.user.enums.Role;

@Getter
@AllArgsConstructor
public class UserSigninDto {
	private Long id;
	private String password;
	private String email;
	private Role role;
	private LocalDateTime deletedAt;
}
