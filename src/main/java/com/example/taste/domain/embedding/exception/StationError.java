package com.example.taste.domain.embedding.exception;

import org.springframework.http.HttpStatus;

import com.example.taste.common.exception.BaseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StationError implements BaseCode {
	STATION_CSV_READ_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S001", "CSV 파일을 읽기 실패했습니다"),
	STATION_LOADING_ERROR(HttpStatus.NOT_FOUND, "S002", "역 데이터 로딩 오류"),
	STATION_SAVE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S003", "역 데이터 저장 실패"),
	
	;

	private final HttpStatus httpStatus;
	private final String code;
	private final String message;
}
