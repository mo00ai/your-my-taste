package com.example.taste.domain.review.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.taste.common.annotation.ImageValid;
import com.example.taste.common.response.CommonResponse;
import com.example.taste.config.security.CustomUserDetails;
import com.example.taste.domain.image.enums.ImageType;
import com.example.taste.domain.review.dto.CreateReviewRequestDto;
import com.example.taste.domain.review.dto.CreateReviewResponseDto;
import com.example.taste.domain.review.dto.GetReviewResponseDto;
import com.example.taste.domain.review.dto.UpdateReviewRequestDto;
import com.example.taste.domain.review.dto.UpdateReviewResponseDto;
import com.example.taste.domain.review.service.OCRService;
import com.example.taste.domain.review.service.ReviewService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/stores/{storeId}/review")
public class ReviewController {
	private final ReviewService reviewService;
	private final OCRService ocrService;

	//리뷰 생성
	@ImageValid
	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public CommonResponse<CreateReviewResponseDto> createReview(
		@RequestPart("requestDto") CreateReviewRequestDto requestDto,
		@PathVariable Long storeId,
		@RequestPart(value = "files", required = false) List<MultipartFile> files,
		@AuthenticationPrincipal CustomUserDetails userDetails)
		throws IOException {
		return CommonResponse.created(
			reviewService.createReview(requestDto, storeId, files, ImageType.REVIEW, userDetails.getUser()));
	}

	// 리뷰 업데이트
	@ImageValid
	@PatchMapping(value = "/{reviewId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public CommonResponse<UpdateReviewResponseDto> updateReview(
		@RequestPart("requestDto") UpdateReviewRequestDto requestDto,
		@PathVariable Long reviewId,
		@RequestPart(value = "files", required = false) List<MultipartFile> files,
		@AuthenticationPrincipal CustomUserDetails userDetails)
		throws IOException {
		return CommonResponse.ok(
			reviewService.updateReview(requestDto, reviewId, files, ImageType.REVIEW, userDetails.getUser()));
	}

	// 가게의 모든 리뷰 조회
	@GetMapping()
	public CommonResponse<Page<GetReviewResponseDto>> getAllReview(@PathVariable Long storeId,
		@RequestParam(defaultValue = "1", required = false) @Min(1) int index,
		@RequestParam(required = false, defaultValue = "0") @Min(0) @Max(5) int score) {
		return CommonResponse.ok(reviewService.getAllReview(storeId, index, score));
	}

	// 리뷰 단건 상세
	@GetMapping("/{reviewId}")
	public CommonResponse<GetReviewResponseDto> getReview(@PathVariable Long reviewId) {
		return CommonResponse.ok(reviewService.getReview(reviewId));
	}

	// 리뷰 삭제 (hard)
	@DeleteMapping("/{reviewId}")
	public CommonResponse<Void> deleteReview(@PathVariable Long reviewId,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		reviewService.deleteReview(reviewId, userDetails.getUser());
		return CommonResponse.ok();
	}

	// 영수증 인증
	@PostMapping("/validate")
	public CommonResponse<Void> createValidation(@PathVariable Long storeId,
		@RequestPart("image") MultipartFile image,
		@AuthenticationPrincipal CustomUserDetails userDetails) throws IOException {
		ocrService.createValidation(storeId, image, userDetails.getUser());
		return CommonResponse.ok();
	}
}
