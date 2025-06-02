package com.example.taste.domain.user.dto.request;

import lombok.Getter;

@Getter
public class UserDeleteRequestDto {
	private String email;
	private String password;
}
