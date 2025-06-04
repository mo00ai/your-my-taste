package com.example.taste.domain.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReviewRequestDto {

	@Pattern(regexp = "^(?!.*(<script|</script|<img|<a\\s|select\\s|union\\s|insert\\s|update\\s|delete\\s|drop\\s|--|\\bor\\b|\\band\\b)).*$", message = "허용되지 않는 문자가 포함되어있습니다.")
	private String contents;

	@Min(1)
	@Max(5)
	private Integer score;
}
