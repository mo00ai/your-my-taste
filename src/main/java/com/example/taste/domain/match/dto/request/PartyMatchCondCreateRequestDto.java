package com.example.taste.domain.match.dto.request;

import lombok.Getter;

@Getter
public class PartyMatchCondCreateRequestDto {
	private Long partyId;
	private int ageMinRange;
	private int ageMaxRange;
	private String gender;
	private String region;
}
