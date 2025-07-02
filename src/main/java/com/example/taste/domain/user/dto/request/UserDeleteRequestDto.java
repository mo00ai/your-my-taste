package com.example.taste.domain.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserDeleteRequestDto {
	private String email;
	private String password;
}
