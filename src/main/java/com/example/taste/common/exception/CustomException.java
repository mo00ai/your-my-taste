package com.example.taste.common.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

	private final BaseCode baseCode;
	private final String detailMessage;

	public CustomException(BaseCode baseCode) {
		super(baseCode.getMessage());
		this.baseCode = baseCode;
		this.detailMessage = null;
	}

	public CustomException(BaseCode baseCode, String detailMessage) {
		super(detailMessage); // getMessage()로 쓸 수 있게
		this.baseCode = baseCode;
		this.detailMessage = detailMessage;
	}
}
