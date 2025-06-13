package com.example.taste.domain.review.dto;

import java.time.LocalDateTime;

import com.example.taste.domain.review.entity.Review;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GetReviewResponseDto {
	private Long id;
	private Long userId;
	private String contents;
	private Integer score;
	private String imageUrl;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@Builder
	public GetReviewResponseDto(Long id, Long userId, String contents, Integer score, String imageUrl,
		LocalDateTime createdAt,
		LocalDateTime updatedAt) {
		this.id = id;
		this.userId = userId;
		this.contents = contents;
		this.score = score;
		this.imageUrl = imageUrl;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public GetReviewResponseDto(Review review) {
		this.id = review.getId();
		this.userId = review.getUser().getId();
		this.contents = review.getContents();
		this.score = review.getScore();
		this.imageUrl = review.getImage() != null ? review.getImage().getUrl() : null;
		this.createdAt = review.getCreatedAt();
		this.updatedAt = review.getUpdatedAt();
	}
}
