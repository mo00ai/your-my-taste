package com.example.taste.domain.match.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

import com.example.taste.domain.match.entity.UserMatchCond;
import com.example.taste.domain.match.vo.AgeRange;

@Getter    // TODO: JSON NULLABLE - @윤예진
public class UserMatchCondResponseDto {
	private Long id;
	private List<UserMatchCondStoreResponseDto> stores;
	private List<UserMatchCondCategoryResponseDto> categories;
	private AgeRange ageRange;
	private String gender;
	private String region;
	private String matchStatus;
	private LocalDateTime matchStartedAt;

	@Builder
	public UserMatchCondResponseDto(UserMatchCond userMatchCond) {
		this.id = userMatchCond.getId();
		this.stores = userMatchCond.getStores().stream()
			.map(UserMatchCondStoreResponseDto::new).toList();
		this.categories = userMatchCond.getCategories().stream()
			.map(UserMatchCondCategoryResponseDto::new).toList();
		this.ageRange = userMatchCond.getAgeRange();
		this.gender = userMatchCond.getUserGender().toString();
		this.region = userMatchCond.getRegion();
		this.matchStatus = userMatchCond.getMatchStatus().toString();
	}
}
