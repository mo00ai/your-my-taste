package com.example.taste.domain.review.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.taste.common.annotation.ImageValid;
import com.example.taste.common.response.CommonResponse;
import com.example.taste.domain.image.enums.ImageType;
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

	@ImageValid
	@PostMapping
	public CommonResponse<CreateReviewResponseDto> createReview(@RequestBody CreateReviewRequestDto requestDto,
		@PathVariable Long storeId,
		@RequestPart(value = "files", required = false) List<MultipartFile> files)
		throws IOException {
		return CommonResponse.created(reviewService.createReview(requestDto, storeId, files.get(0), ImageType.REVIEW));
	}

	@ImageValid
	@PatchMapping("/{reviewId}")
	public CommonResponse<UpdateReviewResponseDto> updateReview(@RequestBody UpdateReviewRequestDto requestDto,
		@PathVariable Long reviewId,
		@RequestPart(value = "files", required = false) List<MultipartFile> files)
		throws IOException {
		return CommonResponse.ok(reviewService.updateReview(requestDto, reviewId, files.get(0), ImageType.REVIEW));
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
	public CommonResponse<Void> deleteReview(@PathVariable Long reviewId) {
		reviewService.deleteReview(reviewId);
		return CommonResponse.ok();
	}

	@PostMapping("/validate")
	public CommonResponse<Void> createValidation(@PathVariable Long storeId,
		@RequestPart("image") MultipartFile image) throws IOException {
		reviewService.createValidation(storeId, image);
		return CommonResponse.ok();
	}

}
