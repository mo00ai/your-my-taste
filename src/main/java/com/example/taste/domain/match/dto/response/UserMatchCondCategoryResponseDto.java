package com.example.taste.domain.match.dto.response;

import lombok.Builder;
import lombok.Getter;

import com.example.taste.domain.match.entity.UserMatchCondCategory;

@Getter
public class UserMatchCondCategoryResponseDto {
	private Long condCategoryId;
	private String name;

	@Builder
	public UserMatchCondCategoryResponseDto(UserMatchCondCategory userMatchCondCategory) {
		this.condCategoryId = userMatchCondCategory.getId();
		this.name = userMatchCondCategory.getCategory().getName();
	}
}
