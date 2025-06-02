package com.example.taste.domain.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateReviewRequestDto {

	@NotBlank
	private String contents;

	@Min(1)
	@Max(5)
	@NotNull
	private Integer score;

	@NotNull
	private Long imageId;
}
