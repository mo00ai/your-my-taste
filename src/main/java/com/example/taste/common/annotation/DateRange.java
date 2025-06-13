package com.example.taste.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.temporal.ChronoUnit;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target(value = {ElementType.FIELD, ElementType.PARAMETER})
@Retention(value = RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {DateRangeValidator.class})
public @interface DateRange {
	String message() default "날짜는 현재 시간 기준 {min} {unit}부터 {max} {unit}까지 입력 가능합니다.";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	int min() default 1;

	int max() default 7;

	ChronoUnit unit() default ChronoUnit.DAYS;
}
