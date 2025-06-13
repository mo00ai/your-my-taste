package com.example.taste.domain.match.dto.response;

import lombok.Builder;
import lombok.Getter;

import com.example.taste.domain.match.entity.UserMatchInfoCategory;

@Getter
public class UserMatchInfoCategoryResponseDto {
	private Long userMatchInfoCategoryId;
	private String name;

	@Builder
	public UserMatchInfoCategoryResponseDto(UserMatchInfoCategory userMatchInfoCategory) {
		this.userMatchInfoCategoryId = userMatchInfoCategory.getId();
		this.name = userMatchInfoCategory.getCategory().getName();
	}
}
