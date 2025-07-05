package com.example.taste.domain.user.dto;

import java.time.LocalDateTime;

import com.example.taste.domain.user.enums.Role;

public interface UserSigninProjectionDto {
	Long getId();

	String getEmail();

	String getPassword();

	Role getRole();

	LocalDateTime getDeletedAt();
}
