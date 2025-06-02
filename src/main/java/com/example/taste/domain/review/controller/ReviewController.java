package com.example.taste.domain.review.controller;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.domain.review.dto.CreateReviewRequestDto;
import com.example.taste.domain.review.dto.CreateReviewResponseDto;
import com.example.taste.domain.review.dto.UpdateReviewRequestDto;
import com.example.taste.domain.review.service.ReviewService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stores/{storeId}/review")
public class ReviewController {
	private final ReviewService reviewService;

	@PostMapping
	public CommonResponse<CreateReviewResponseDto> createReview(@RequestBody CreateReviewRequestDto requestDto,
		@PathVariable Long storeId) {
		return CommonResponse.created(reviewService.createReview(requestDto, storeId));
	}

	@PatchMapping("/{reviewId}")
	public CommonResponse<UpdateReviewResponseDto> updateReview(@RequestBody UpdateReviewRequestDto requestDto,
		@PathVariable Long reviewId) {
		return CommonResponse.ok(reviewService.updateReview(requestDto, reviewId));
	}

}
