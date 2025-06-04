package com.example.taste.common.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EnumValidator implements ConstraintValidator<ValidEnum, String> {

	private ValidEnum annotation;

	@Override
	public void initialize(ValidEnum constraintAnnotation) {
		this.annotation = constraintAnnotation;
	}

	@SuppressWarnings("checkstyle:RegexpSingleline")
	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null || value.isBlank()) {
			return true; // 빈 문자열도 허용하려면 이렇게
		}

		Object[] enumValues = this.annotation.target().getEnumConstants();
		if (enumValues != null) {
			for (Object enumValue : enumValues) {
				if (value.equals(enumValue.toString())) {
					return true;
				}
			}
		}
		return false;
	}

}
