package com.example.taste.common.exception;

import static com.example.taste.domain.auth.exception.AuthErrorCode.INVALID_ROLE;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.common.response.ErrorResponse;

@Slf4j
@Builder
@RestControllerAdvice
public class GlobalExceptionHandler {

	/**
	 * AccessDeniedException 처리 - hasRole('ADMIN') 예외처리
	 */
	@ExceptionHandler(AccessDeniedException.class)
	protected ResponseEntity<CommonResponse<ErrorResponse>> handleHasRoleException(AccessDeniedException ex) {
		log.error("AccessDeniedException: {}", ex.getMessage());

		return new ResponseEntity<>(
			CommonResponse.error(INVALID_ROLE.getHttpStatus(), INVALID_ROLE.getCode(),
				INVALID_ROLE.getMessage()),
			INVALID_ROLE.getHttpStatus()
		);
	}

	/**
	 * Custom Exception 처리 - Service 계층에서 발생한 비즈니스 예외 처리
	 */
	@ExceptionHandler(CustomException.class)
	protected ResponseEntity<CommonResponse<ErrorResponse>> handleCustomException(CustomException ex) {
		log.error("CustomException: {}", ex.getMessage());

		BaseCode errorCode = ex.getBaseCode();
		String message =
			ex.getDetailMessage() != null ? ex.getDetailMessage() : errorCode.getMessage();

		return new ResponseEntity<>(
			CommonResponse.error(errorCode.getHttpStatus(), errorCode.getCode(), message),
			errorCode.getHttpStatus()
		);
	}

	/**
	 * Valid 예외 처리 - Controller의 @Valid 검증 실패 시 발생 컨트롤러에서 개별적으로 처리하지 않은 경우에만 여기서 처리됨
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	protected ResponseEntity<CommonResponse<ErrorResponse>> handleMethodArgumentNotValidException(
		MethodArgumentNotValidException ex) {
		log.error("MethodArgumentNotValidException: {}", ex.getMessage());
		List<ErrorResponse.FieldError> fieldErrors = processFieldErrors(ex.getBindingResult());

		return new ResponseEntity<>(
			CommonResponse.error(ErrorCode.INVALID_INPUT_VALUE, fieldErrors),
			ErrorCode.INVALID_INPUT_VALUE.getHttpStatus());
	}

	/**
	 * Validated 예외 처리 - Controller의 @Validated 검증 실패 시 발생 컨트롤러에서 개별적으로 처리하지 않은 경우에만 여기서 처리됨
	 */
	@ExceptionHandler(ConstraintViolationException.class)
	protected ResponseEntity<CommonResponse<ErrorResponse>> handleConstraintViolationException(
		ConstraintViolationException ex) {
		log.error("ConstraintViolationException: {}", ex.getMessage());

		return new ResponseEntity<>(
			CommonResponse.error(ErrorCode.INVALID_INPUT_VALUE), ErrorCode.INVALID_INPUT_VALUE.getHttpStatus());
	}

	/**
	 * Repository(JPA) 계층 예외 처리 - EntityNotFoundException 처리
	 */
	@ExceptionHandler(EntityNotFoundException.class)
	protected ResponseEntity<CommonResponse<ErrorResponse>> handleEntityNotFoundException(
		EntityNotFoundException ex) {
		log.error("EntityNotFoundException: {}", ex.getMessage());

		return new ResponseEntity<>(CommonResponse.error(ErrorCode.ENTITY_NOT_FOUND),
			ErrorCode.ENTITY_NOT_FOUND.getHttpStatus());
	}

	/**
	 * Repository(JPA) 계층 예외 처리 - DataAccessException 처리 (SQL 예외, Lock 획득 실패 등 DB 관련 예외)
	 */
	@ExceptionHandler(DataAccessException.class)
	protected ResponseEntity<CommonResponse<ErrorResponse>> handleDataAccessException(
		DataAccessException ex) {
		log.error("DataAccessException: {}", ex.getMessage());

		return new ResponseEntity<>(CommonResponse.error(ErrorCode.INTERNAL_SERVER_ERROR),
			ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus());
	}

	/**
	 * 지원하지 않는 HTTP 메소드 호출 시 발생하는 예외 처리
	 */
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	protected ResponseEntity<CommonResponse<ErrorResponse>> handleHttpRequestMethodNotSupportedException(
		HttpRequestMethodNotSupportedException ex) {
		log.error("HttpRequestMethodNotSupportedException: {}", ex.getMessage());

		return new ResponseEntity<>(CommonResponse.error(ErrorCode.METHOD_NOT_ALLOWED),
			ErrorCode.METHOD_NOT_ALLOWED.getHttpStatus());
	}

	/**
	 * 요청 파라미터 타입 불일치 예외 처리
	 */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	protected ResponseEntity<CommonResponse<ErrorResponse>> handleMethodArgumentTypeMismatchException(
		MethodArgumentTypeMismatchException ex) {
		log.error("MethodArgumentTypeMismatchException: {}", ex.getMessage());

		return new ResponseEntity<>(CommonResponse.error(ErrorCode.INVALID_TYPE_VALUE),
			ErrorCode.INVALID_TYPE_VALUE.getHttpStatus());
	}

	/**
	 * 필수 요청 파라미터 누락 예외 처리
	 */
	@ExceptionHandler(MissingServletRequestParameterException.class)
	protected ResponseEntity<CommonResponse<ErrorResponse>> handleMissingServletRequestParameterException(
		MissingServletRequestParameterException ex) {
		log.error("MissingServletRequestParameterException: {}", ex.getMessage());

		return new ResponseEntity<>(CommonResponse.error(ErrorCode.INVALID_INPUT_VALUE),
			ErrorCode.INVALID_INPUT_VALUE.getHttpStatus());
	}

	/**
	 * JSON 파싱 오류 등의 예외 처리
	 */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	protected ResponseEntity<CommonResponse<ErrorResponse>> handleHttpMessageNotReadableException(
		HttpMessageNotReadableException ex) {
		log.error("HttpMessageNotReadableException: {}", ex.getMessage());

		return new ResponseEntity<>(CommonResponse.error(ErrorCode.INVALID_INPUT_VALUE),
			ErrorCode.INVALID_INPUT_VALUE.getHttpStatus());
	}

	/**
	 * 그 외 모든 예외 처리
	 */
	@ExceptionHandler(Exception.class)
	protected ResponseEntity<CommonResponse<ErrorResponse>> handleException(Exception ex) {
		log.error("Exception: {}", ex.getMessage(), ex);

		return new ResponseEntity<>(CommonResponse.error(ErrorCode.INTERNAL_SERVER_ERROR),
			ErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus());
	}

	/**
	 * BindingResult 에서 발생한 필드 에러 목록을 ErrorResponse.FieldError 목록으로 반환
	 */
	private List<ErrorResponse.FieldError> processFieldErrors(BindingResult bindingResult) {
		List<ErrorResponse.FieldError> fieldErrors = new ArrayList<>();

		for (FieldError fieldError : bindingResult.getFieldErrors()) {
			ErrorResponse.FieldError error = ErrorResponse.FieldError.of(fieldError.getField(),
				fieldError.getRejectedValue() == null ? ""
					: fieldError.getRejectedValue().toString(),
				fieldError.getDefaultMessage());
			fieldErrors.add(error);
		}

		return fieldErrors;
	}
}
