package com.example.taste.domain.match.dto.request;

import lombok.Getter;

import com.example.taste.domain.match.vo.AgeRange;

@Getter
public class PartyMatchCondCreateRequestDto {
	private Long partyId;
	private AgeRange ageRange;
	private String gender;
	private String region;
}
