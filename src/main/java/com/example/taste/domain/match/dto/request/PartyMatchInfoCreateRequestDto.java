package com.example.taste.domain.match.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

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
	// MEMO : 주소 검증 필요
	private String region;
}
