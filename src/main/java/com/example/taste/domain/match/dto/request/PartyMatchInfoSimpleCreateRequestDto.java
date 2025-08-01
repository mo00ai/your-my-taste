package com.example.taste.domain.match.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import lombok.Getter;

import com.example.taste.common.annotation.ValidEnum;
import com.example.taste.domain.match.entity.AgeRange;
import com.example.taste.domain.user.enums.Gender;

@Getter
public class PartyMatchInfoSimpleCreateRequestDto {
	@Valid
	private AgeRange ageRange;
	@ValidEnum(target = Gender.class)
	private String gender;
	private String region;
	@Size(min = 0, max = 5, message = "선호 입맛은 5개 이하로 입력해야 합니다.")
	private List<String> favorList;
}
