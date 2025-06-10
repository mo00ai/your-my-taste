package com.example.taste.domain.map.exception;

import org.springframework.http.HttpStatus;

import com.example.taste.common.exception.BaseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MapErrorCode implements BaseCode {
	GEOCODING_API_ERROR(HttpStatus.BAD_REQUEST, "M001", "주소를 좌표로 변환할 수 없습니다. 주소를 확인해주세요."),
	REVERSE_GEOCODING_API_ERROR(HttpStatus.BAD_REQUEST, "M002", "좌표를 주소로 변환할 수 없습니다. 좌표를 확인해주세요."),
	INVALID_ADDRESS_FORMAT(HttpStatus.BAD_REQUEST, "M003", "올바르지 않은 주소 형식입니다."),
	INVALID_COORDINATE_FORMAT(HttpStatus.BAD_REQUEST, "M004", "올바르지 않은 좌표 형식입니다. 위도와 경도를 확인해주세요."),

	;

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;

}
