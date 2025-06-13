package com.example.taste.domain.match.dto.response;

import lombok.Builder;
import lombok.Getter;

import com.example.taste.domain.match.entity.UserMatchInfoCategory;

@Getter
public class UserMatchInfoCategoryResponseDto {
	private Long condCategoryId;
	private String name;

	@Builder
	public UserMatchInfoCategoryResponseDto(UserMatchInfoCategory userMatchInfoCategory) {
		this.condCategoryId = userMatchInfoCategory.getId();
		this.name = userMatchInfoCategory.getCategory().getName();
	}
}
