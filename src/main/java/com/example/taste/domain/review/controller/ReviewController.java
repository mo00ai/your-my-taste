package com.example.taste.domain.review.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.domain.review.service.ReviewService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stores/{storeId}/review")
public class ReviewController {
	private final ReviewService reviewService;

	@PostMapping
	public CommonResponse<CreateReviewResponseDto> createReview(@RequestBody CreateReviewRequestDto requestDto) {
		return CommonResponse.created(reviewService.createReview(requestDto));
	}
}
