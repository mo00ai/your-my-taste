package com.example.taste.domain.review.dto;

import java.time.LocalDateTime;

import com.example.taste.domain.review.entity.Review;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class CreateReviewResponseDto {
	private Long id;
	private String contents;
	private Integer score;
	private String imageUrl;
	private Boolean isPresented;
	private LocalDateTime createdAt;

	@Builder
	public CreateReviewResponseDto(Long id, String contents, Integer score, String imageUrl, Boolean isPresented,
		LocalDateTime createdAt) {
		this.id = id;
		this.contents = contents;
		this.score = score;
		this.imageUrl = imageUrl;
		this.isPresented = isPresented;
		this.createdAt = createdAt;
	}

	public CreateReviewResponseDto(Review review) {
		this.id = review.getId();
		this.contents = review.getContents();
		this.score = review.getScore();
		this.imageUrl = review.getImage().getUrl();
		this.isPresented = review.isPresented();
		this.createdAt = review.getCreatedAt();
	}
}
