package com.example.taste.domain.user.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

import com.example.taste.domain.user.entity.User;
import com.example.taste.domain.user.entity.UserFavor;
import com.fasterxml.jackson.annotation.JsonInclude;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserMyProfileResponseDto {
	private Long id;
	private String nickname;
	private String email;
	private String address;
	private String level;
	private List<UserFavorResponseDto> favors;
	private int point;
	private String image;
	private int follower;
	private int following;
	private int postingCount;

	@Builder
	public UserMyProfileResponseDto(User user, List<UserFavor> favors) {
		this.id = user.getId();
		this.nickname = user.getNickname();
		this.email = user.getEmail();
		this.address = user.getAddress();
		this.level = user.getLevel().toString();
		this.favors = favors.stream()
			.map(UserFavorResponseDto::new)
			.toList();
		this.point = user.getPoint();
		this.image = user.getImage() != null ? user.getImage().getUrl() : null;
		this.follower = user.getFollower();
		this.following = user.getFollowing();
		this.postingCount = user.getPostingCount();
	}
}
