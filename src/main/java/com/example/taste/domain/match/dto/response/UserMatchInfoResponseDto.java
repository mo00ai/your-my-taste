package com.example.taste.domain.match.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

import com.example.taste.domain.match.entity.AgeRange;
import com.example.taste.domain.match.entity.UserMatchInfo;
import com.fasterxml.jackson.annotation.JsonInclude;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserMatchInfoResponseDto {
	private Long id;
	private List<UserMatchInfoStoreResponseDto> storeList;
	private List<UserMatchInfoCategoryResponseDto> categoryList;
	private List<UserMatchInfoFavorResponseDto> favorList;
	private AgeRange ageRange;
	private String gender;
	private String region;
	private String matchStatus;
	private LocalDateTime matchStartedAt;

	@Builder
	public UserMatchInfoResponseDto(UserMatchInfo userMatchInfo) {
		this.id = userMatchInfo.getId();
		this.storeList = userMatchInfo.getStoreList().stream()
			.map(UserMatchInfoStoreResponseDto::new).toList();
		this.categoryList = userMatchInfo.getCategoryList().stream()
			.map(UserMatchInfoCategoryResponseDto::new).toList();
		this.favorList = userMatchInfo.getFavorList().stream()
			.map(UserMatchInfoFavorResponseDto::new).toList();
		this.ageRange = userMatchInfo.getAgeRange();
		this.gender = userMatchInfo.getUserGender().toString();
		this.region = userMatchInfo.getRegion();
		this.matchStatus = userMatchInfo.getMatchStatus().toString();
		this.matchStartedAt = userMatchInfo.getMatchStartedAt();
	}
}
