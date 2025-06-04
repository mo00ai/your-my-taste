package com.example.taste.domain.pk.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class PkCriteriaResponseDto {

	private Long id;
	private String type;
	private Integer point;
	private boolean isActive;

	@Builder
	public PkCriteriaResponseDto(Long id, String type, Integer point, boolean isActive) {
		this.id = id;
		this.type = type;
		this.point = point;
		this.isActive = isActive;
	}
}
