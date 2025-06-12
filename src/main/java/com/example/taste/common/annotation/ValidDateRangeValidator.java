package com.example.taste.common.annotation;

import com.example.taste.domain.board.dto.search.CreatedDateRange;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidDateRangeValidator implements ConstraintValidator<ValidDateRange, CreatedDateRange> {

	@Override
	public boolean isValid(CreatedDateRange dto, ConstraintValidatorContext context) {
		if (dto.getCreatedFrom() == null || dto.getCreatedTo() == null) {
			return true; // null 허용: 필터 조건이 선택적일 수 있음
		}
		return !dto.getCreatedFrom().isAfter(dto.getCreatedTo());
	}
}
