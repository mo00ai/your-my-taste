package com.example.taste.common.response;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;

import com.example.taste.common.exception.BaseCode;
import com.example.taste.common.exception.SuccessCode;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommonResponse<T> {

	@Builder.Default
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private final LocalDateTime timestamp = LocalDateTime.now();
	@JsonIgnore
	private final HttpStatus status;
	private final String code;
	private final String message;
	private final T data;

	public static <T> CommonResponse<T> ok() {
		return CommonResponse.<T>builder()
			.timestamp(LocalDateTime.now())
			.status(SuccessCode.OK.getHttpStatus())
			.code(SuccessCode.OK.getCode())
			.message(SuccessCode.OK.getMessage())
			.data(null)
			.build();
	}

	public static <T> CommonResponse<T> ok(T data) {
		return CommonResponse.<T>builder()
			.timestamp(LocalDateTime.now())
			.status(SuccessCode.OK.getHttpStatus())
			.code(SuccessCode.OK.getCode())
			.message(SuccessCode.OK.getMessage())
			.data(data)
			.build();
	}

	public static <T> CommonResponse<T> created(T data) {
		return CommonResponse.<T>builder()
			.timestamp(LocalDateTime.now())
			.status(SuccessCode.CREATED.getHttpStatus())
			.code(SuccessCode.CREATED.getCode())
			.message(SuccessCode.CREATED.getMessage())
			.data(data)
			.build();
	}

	public static CommonResponse<ErrorResponse> error(BaseCode baseCode) {
		return CommonResponse.<ErrorResponse>builder()
			.timestamp(LocalDateTime.now())
			.status(baseCode.getHttpStatus())
			.code(baseCode.getCode())
			.message(baseCode.getMessage())
			.data(ErrorResponse.of(baseCode))
			.build();
	}

	public static CommonResponse<ErrorResponse> error(BaseCode baseCode,
		List<ErrorResponse.FieldError> fieldErrors) {
		return CommonResponse.<ErrorResponse>builder()
			.timestamp(LocalDateTime.now())
			.status(baseCode.getHttpStatus())
			.code(baseCode.getCode())
			.message(baseCode.getMessage())
			.data(ErrorResponse.of(baseCode, fieldErrors))
			.build();
	}

	public static <T> CommonResponse<T> error(HttpStatus status, String code, String message) {
		return CommonResponse.<T>builder()
			.timestamp(LocalDateTime.now())
			.status(status)
			.code(code)
			.message(message)
			.data(null)
			.build();
	}

	public static <T> CommonResponse<T> success(BaseCode successCode) {
		return CommonResponse.<T>builder()
			.timestamp(LocalDateTime.now())
			.status(successCode.getHttpStatus())
			.code(successCode.getCode())
			.message(successCode.getMessage())
			.data(null)
			.build();
	}

}
