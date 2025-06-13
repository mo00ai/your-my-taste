package com.example.taste.domain.match.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

import com.example.taste.domain.match.entity.UserMatchInfo;
import com.example.taste.domain.match.vo.AgeRange;
import com.fasterxml.jackson.annotation.JsonInclude;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserMatchInfoResponseDto {
	private Long id;
	private List<UserMatchInfoStoreResponseDto> stores;
	private List<UserMatchInfoCategoryResponseDto> categories;
	private AgeRange ageRange;
	private String gender;
	private String region;
	private String matchStatus;
	private LocalDateTime matchStartedAt;

	@Builder
	public UserMatchInfoResponseDto(UserMatchInfo userMatchInfo) {
		this.id = userMatchInfo.getId();
		this.stores = userMatchInfo.getStoreList().stream()
			.map(UserMatchInfoStoreResponseDto::new).toList();
		this.categories = userMatchInfo.getCategoryList().stream()
			.map(UserMatchInfoCategoryResponseDto::new).toList();
		this.ageRange = userMatchInfo.getAgeRange();
		this.gender = userMatchInfo.getUserGender().toString();
		this.region = userMatchInfo.getRegion();
		this.matchStatus = userMatchInfo.getMatchStatus().toString();
	}
}
