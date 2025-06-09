package com.example.taste.domain.party.enums;

import static com.example.taste.common.exception.ErrorCode.INVALID_TYPE_VALUE;

import java.util.Arrays;

import com.example.taste.common.exception.CustomException;

public enum PartyFilter {
	ALL,
	MY;

	public static PartyFilter of(String partyFilter) {
		return Arrays.stream(PartyFilter.values())
			.filter(r -> r.name().equalsIgnoreCase(partyFilter))
			.findFirst()
			.orElseThrow(() -> new CustomException(INVALID_TYPE_VALUE));
	}
}
