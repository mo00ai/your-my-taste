package com.example.taste.domain.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateReviewResponseDto {

	@NotEmpty
	private String contents;

	@Min(1)
	@Max(5)
	@NotNull
	private Long score;

	@NotNull
	private Long image;

}
