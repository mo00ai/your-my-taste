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
	private String content;
	private int score;
	private String imageUrl;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@Builder
	public CreateReviewResponseDto(Long id, String content, int score, String imageUrl, LocalDateTime createdAt,
		LocalDateTime updatedAt) {
		this.id = id;
		this.content = content;
		this.score = score;
		this.imageUrl = imageUrl;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public CreateReviewResponseDto(Review review) {
		this.id = review.getId();
		this.content = review.getContent();
		this.score = review.getScore();
		this.imageUrl = review.getImage().getUrl();
		this.createdAt = review.getCreatedAt();
		this.updatedAt = review.getUpdatedAt();
	}
}
