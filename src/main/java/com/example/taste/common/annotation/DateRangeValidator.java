package com.example.taste.common.annotation;

import java.time.LocalDateTime;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DateRangeValidator implements ConstraintValidator<DateRange, String> {
	private DateRange annotation;
	private LocalDateTime now;

	@Override
	public void initialize(DateRange constraintAnnotation) {
		this.annotation = constraintAnnotation;
		this.now = LocalDateTime.now();
	}

	@Override
	public boolean isValid(LocalDateTime time, ConstraintValidatorContext context) {
		// 현재보다 30분 이후 ~ 한달 뒤인지 검사

		return false;
	}

}