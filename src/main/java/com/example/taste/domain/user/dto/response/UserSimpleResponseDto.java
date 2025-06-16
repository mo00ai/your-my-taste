package com.example.taste.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;

import com.example.taste.domain.user.entity.User;
import com.fasterxml.jackson.annotation.JsonInclude;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserSimpleResponseDto {
	private Long id;
	private String nickname;
	private String level;
	private String image;

	@Builder
	public UserSimpleResponseDto(User user) {
		this.id = user.getId();
		this.nickname = user.getNickname();
		this.level = user.getLevel().toString();
		this.image = user.getImage() != null ? user.getImage().getUrl() : null;
	}
}
