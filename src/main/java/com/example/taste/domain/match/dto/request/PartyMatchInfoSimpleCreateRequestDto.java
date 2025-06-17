package com.example.taste.domain.match.dto.request;

import jakarta.validation.Valid;

import lombok.Getter;

import com.example.taste.common.annotation.ValidEnum;
import com.example.taste.domain.match.vo.AgeRange;
import com.example.taste.domain.user.enums.Gender;

@Getter
public class PartyMatchInfoSimpleCreateRequestDto {
	@Valid
	private AgeRange ageRange;
	@ValidEnum(target = Gender.class)
	private String gender;
	private String region;
}
