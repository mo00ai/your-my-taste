package com.example.taste.domain.match.dto.response;

import lombok.Builder;
import lombok.Getter;

import com.example.taste.domain.match.entity.UserMatchInfoFavor;

@Getter
public class UserMatchInfoFavorResponseDto {
	private Long userMatchInfoFavorId;
	private String name;

	@Builder
	public UserMatchInfoFavorResponseDto(UserMatchInfoFavor userMatchInfoFavor) {
		this.userMatchInfoFavorId = userMatchInfoFavor.getId();
		this.name = userMatchInfoFavor.getFavor().getName();
	}
}
