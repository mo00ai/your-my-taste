package com.example.taste.domain.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Level {
	NORMAL(1), PK(3);

	private final int postingLimit;
}
