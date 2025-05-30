package com.example.taste.common.response;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

import com.example.taste.common.exception.BaseCode;
import com.fasterxml.jackson.annotation.JsonInclude;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	private final List<FieldError> fieldErrors;

	public static ErrorResponse of(BaseCode baseCode) {
		return ErrorResponse.builder()
			.fieldErrors(new ArrayList<>())
			.build();
	}

	public static ErrorResponse of(BaseCode baseCode, String message) {
		return ErrorResponse.builder()
			.fieldErrors(new ArrayList<>())
			.build();
	}

	public static ErrorResponse of(BaseCode baseCode, List<FieldError> fieldErrors) {
		return ErrorResponse.builder()
			.fieldErrors(fieldErrors)
			.build();
	}

	@Getter
	@Builder
	public static class FieldError {

		private String field;
		private String value;
		private String reason;

		public static FieldError of(String field, String value, String reason) {
			return FieldError.builder()
				.field(field)
				.value(value)
				.reason(reason)
				.build();
		}
	}

}
