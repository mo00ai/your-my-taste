package com.example.taste.common.exception;

import org.springframework.http.HttpStatus;

public interface BaseCode {
	HttpStatus getHttpStatus();

	String getCode();

	String getMessage();

}
