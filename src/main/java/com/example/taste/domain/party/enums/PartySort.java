package com.example.taste.domain.party.enums;

import java.util.Arrays;

import lombok.Getter;

@Getter
public enum PartySort {
	MEETING_DATE("meetingDate"),
	CREATED_AT("createdAt"),
	NEARLY_FULL("nearlyFull");

	private final String label;

	PartySort(String label) {
		this.label = label;
	}

	public static boolean isValid(String label) {
		return Arrays.stream(values())
			.anyMatch(s -> s.getLabel().equals(label));
	}
}
