package com.example.taste.domain.user.dto;

import lombok.Getter;

@Getter
public class UserDeleteRequestDto {
	private String email;
	private String password;
}
