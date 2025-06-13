package com.example.taste.domain.match.vo;

import jakarta.validation.constraints.Positive;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AgeRange {
	@Positive
	private Integer minAge;

	@Positive
	private Integer maxAge;

	public AgeRange(Integer minAge, Integer maxAge) {
		if (minAge == null || maxAge == null || (minAge > maxAge) || minAge % 10 != 0 || maxAge % 10 != 0) {
			throw new IllegalArgumentException("minAge <= maxAge 이어야하며, minAge 와 maxAge 는 십자리 수 단위로만 가능합니다.");
		}
		this.minAge = minAge;
		this.maxAge = maxAge;
	}

	public boolean includes(int age) {
		return minAge <= age && age <= maxAge;
	}

	public boolean includes(double age) {
		return minAge <= age && age <= maxAge;
	}
}
