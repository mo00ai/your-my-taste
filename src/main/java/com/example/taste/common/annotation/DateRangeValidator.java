package com.example.taste.common.annotation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import jakarta.validation.ConstraintDeclarationException;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DateRangeValidator implements ConstraintValidator<DateRange, Object> {
	private DateRange annotation;
	private int min;
	private int max;
	private ChronoUnit unit;

	@Override
	public void initialize(DateRange constraintAnnotation) {
		this.annotation = constraintAnnotation;
		this.min = constraintAnnotation.min();
		this.max = constraintAnnotation.max();
		this.unit = constraintAnnotation.unit();

		if (min > max) {
			throw new ConstraintDeclarationException(
				String.format("@DateRange 설정 오류: min(%d)이 max(%d)보다 클 수 없습니다.", min, max));
		}
	}

	@Override
	public boolean isValid(Object value, ConstraintValidatorContext context) {
		if (value == null) {
			return true;
		}

		if (value instanceof LocalDate) {
			return validLocalDate((LocalDate)value);
		} else if (value instanceof LocalDateTime) {
			return validLocalDateTime((LocalDateTime)value);
		} else {
			return false;        // 지원하지 않는 타입
		}
	}

	private boolean validLocalDate(LocalDate value) {
		// 시간이 min unit ~ max unit 내의 날짜인지 검사
		LocalDate now = LocalDate.now();
		LocalDate minBound = now.plus(min, unit);
		LocalDate maxBound = now.plus(max, unit);

		return (value.isAfter(minBound) || value.isEqual(minBound))
			&& (value.isBefore(maxBound)) || value.isEqual(maxBound);
	}

	public boolean validLocalDateTime(LocalDateTime value) {
		// 시간이 min unit ~ max unit 내의 날짜인지 검사
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime minBound = now.plus(min, unit);
		LocalDateTime maxBound = now.plus(max, unit);

		return (value.isAfter(minBound) || value.isEqual(minBound))
			&& (value.isBefore(maxBound) || value.isEqual(maxBound));

	}
}