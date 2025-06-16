package com.example.taste.domain.user.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Level {
	NORMAL(2), PK(4);

	private final int postingLimit;
}
