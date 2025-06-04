package com.example.taste.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;

import com.example.taste.domain.user.entity.UserFavor;

@Getter

public class UserFavorResponseDto {
	private Long userFavorId;
	private String name;

	@Builder
	public UserFavorResponseDto(UserFavor userFavor) {
		this.userFavorId = userFavor.getId();
		this.name = userFavor.getFavor().getName();
	}
}
