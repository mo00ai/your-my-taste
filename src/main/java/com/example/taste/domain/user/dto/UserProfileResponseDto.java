package com.example.taste.domain.user.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.entity.UserFavor;

@Getter
public class UserProfileResponseDto {
	private Long id;
	private String nickname;
	private String email;
	private String level;
	private List<UserFavorResponseDto> favors;
	private int point;
	private String profile;

	@Builder
	public UserProfileResponseDto(User user, List<UserFavor> favors) {
		this.id = user.getId();
		this.nickname = user.getNickname();
		this.email = user.getEmail();
		this.level = user.getLevel().toString();
		this.favors = favors.stream()
			.map(UserFavorResponseDto::new)
			.toList();
		this.point = user.getPoint();
		this.profile = user.getImage().getUrl();
	}
}
