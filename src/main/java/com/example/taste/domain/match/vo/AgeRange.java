package com.example.taste.domain.match.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AgeRange {
	private Integer minAge;
	private Integer maxAge;

	public boolean includes(int age) {
		return minAge <= age && age <= maxAge;
	}

	public boolean includes(double age) {
		return minAge <= age && age <= maxAge;
	}
}
