package com.example.taste.domain.match.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import lombok.Builder;
import lombok.Getter;

import com.example.taste.common.annotation.ValidEnum;
import com.example.taste.domain.match.vo.AgeRange;
import com.example.taste.domain.user.enums.Gender;

@Getter
public class PartyMatchInfoCreateRequestDto {
	@Positive(message = "유효하지 않은 파티 ID 값입니다.")
	private Long partyId;
	@Valid
	private AgeRange ageRange;
	@ValidEnum(target = Gender.class)
	private String gender;
	private String region;

	@Builder
	public PartyMatchInfoCreateRequestDto(
		Long partyId, PartyMatchInfoSimpleCreateRequestDto partyMatchInfo) {
		this.partyId = partyId;
		if (partyMatchInfo != null) {
			this.ageRange = partyMatchInfo.getAgeRange() != null ? partyMatchInfo.getAgeRange() : null;
			this.gender = partyMatchInfo.getGender() != null ? partyMatchInfo.getGender() : null;
			this.region = partyMatchInfo.getRegion() != null ? partyMatchInfo.getRegion() : null;
		}
	}
}
