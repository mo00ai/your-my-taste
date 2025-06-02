package com.example.taste.domain.review.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.domain.review.dto.CreateReviewRequestDto;
import com.example.taste.domain.review.dto.CreateReviewResponseDto;
import com.example.taste.domain.review.dto.GetReviewResponseDto;
import com.example.taste.domain.review.dto.UpdateReviewRequestDto;
import com.example.taste.domain.review.dto.UpdateReviewResponseDto;
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

	@GetMapping()
	public CommonResponse<Page<GetReviewResponseDto>> getAllReview(@PathVariable Long storeId,
		@RequestParam(defaultValue = "1", required = false) int index,
		@RequestParam(required = false, defaultValue = "0") int score) {
		return CommonResponse.ok(reviewService.getAllReview(storeId, index, score));
	}

	@GetMapping("/{reviewId}")
	public CommonResponse<GetReviewResponseDto> getAllReviewOfUser(@PathVariable Long reviewId) {
		return CommonResponse.ok(reviewService.getReview(reviewId));
	}

	@DeleteMapping("/{reviewId}")
	public CommonResponse<String> deleteReview(@PathVariable Long reviewId) {
		reviewService.deleteReview(reviewId);
		return CommonResponse.ok("삭제되었습니다.");
	}

	@PostMapping
	public CommonResponse<String> createValidation(@PathVariable Long storeId) {
		return CommonResponse.ok(reviewService.createValidation(storeId));
	}

}
