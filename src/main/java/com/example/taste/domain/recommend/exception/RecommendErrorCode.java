package com.example.taste.domain.recommend.exception;

import org.springframework.http.HttpStatus;

import com.example.taste.common.exception.BaseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RecommendErrorCode implements BaseCode {

	ADDRESS_LOAD_FAILED(HttpStatus.BAD_REQUEST, "A001", "요청한 주소의 위도,경도를 찾을 수 없습니다."),
	WEATHER_LOAD_FAILED(HttpStatus.BAD_REQUEST, "A002", "날씨 정보를 찾을 수 없습니다."),
	WEATHER_TEMP_FAILED(HttpStatus.NOT_FOUND, "A003", "날씨 기온 로딩 실패했습니다."),
	TOO_MANY_API_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "A004", "동시 요청자 수가 많아 요청이 지연되고 있습니다. 다음에 다시 이용해주세요.");

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
