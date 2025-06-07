package com.example.taste.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target(value = ElementType.METHOD)
@Retention(value = RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {DateRangeValidator.class})
public @interface DateRange {
	String message() default "최소 현재로부터 30분 이후, 최대 1달 뒤의 약속시간을 설정할 수 있습니다.";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};

	Class<? extends Enum<?>> target();

	String minDate();

	String maxDate();

	TimeUnit unit() default TimeUnit.MINUTES;
}
