package com.example.taste.domain.match.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

import com.example.taste.domain.match.entity.UserMatchCond;

@Getter
public class UserMatchCondResponseDto {
	private Long id;
	private List<UserMatchCondStoreResponseDto> stores;
	private List<UserMatchCondCategoryResponseDto> categories;
	private int ageMinRange;
	private int ageMaxRange;
	private String gender;
	private String region;
	private String matchingStatus;
	private LocalDateTime matchStartedAt;

	@Builder
	public UserMatchCondResponseDto(UserMatchCond userMatchCond) {
		this.id = userMatchCond.getId();
		this.stores = userMatchCond.getStores().stream()
			.map(UserMatchCondStoreResponseDto::new).toList();
		this.categories = userMatchCond.getCategories().stream()
			.map(UserMatchCondCategoryResponseDto::new).toList();
		this.ageMinRange = userMatchCond.getAgeMinRange();
		this.ageMaxRange = userMatchCond.getAgeMaxRange();
		this.gender = userMatchCond.getGender().toString();
		this.region = userMatchCond.getRegion();
		this.matchingStatus = userMatchCond.getMatchingStatus().toString();
	}
}
