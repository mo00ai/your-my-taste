package com.example.taste.domain.favor.dto.response;

import lombok.Builder;
import lombok.Getter;

import com.example.taste.domain.favor.entity.Favor;

@Getter
public class FavorAdminResponseDto {
	private Long id;
	private String name;

	@Builder
	public FavorAdminResponseDto(Favor favor) {
		this.id = favor.getId();
		this.name = favor.getName();
	}
}
