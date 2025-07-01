package com.example.taste.domain.recommend.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RecommendResponseDto {

	private String recommend;

	@Builder
	public RecommendResponseDto(String recommend) {
		this.recommend = recommend;
	}
}
