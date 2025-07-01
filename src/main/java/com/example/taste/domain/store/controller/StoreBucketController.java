package com.example.taste.domain.store.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.common.response.PageResponse;
import com.example.taste.domain.user.entity.CustomUserDetails;
import com.example.taste.domain.store.dto.request.AddBucketItemRequest;
import com.example.taste.domain.store.dto.request.CreateBucketRequest;
import com.example.taste.domain.store.dto.request.RemoveBucketItemRequest;
import com.example.taste.domain.store.dto.response.BucketItemResponse;
import com.example.taste.domain.store.dto.response.StoreBucketResponse;
import com.example.taste.domain.store.service.StoreBucketService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
public class StoreBucketController {
	private final StoreBucketService storeBucketService;

	// 맛집 리스트 생성
	@PostMapping("/store-buckets")
	public CommonResponse<StoreBucketResponse> createBucket(@RequestBody @Valid CreateBucketRequest request,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		Long userId = userDetails.getId();
		return CommonResponse.created(storeBucketService.createBucket(request, userId));
	}

	// 맛집리스트에 맛집 추가(하나의 가게 여러 리스트에 추가 가능)
	@PostMapping("/store-buckets/store-bucket-items")
	public CommonResponse<Void> addBucketItem(@RequestBody @Valid AddBucketItemRequest request,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		Long userId = userDetails.getId();
		storeBucketService.addBucketItem(request, userId);
		return CommonResponse.ok();
	}

	//특정 유저의 맛집 리스트 조회(유저 프로필로 접근)
	@GetMapping("/users/{targetUserId}/store-buckets")
	public CommonResponse<PageResponse<StoreBucketResponse>> getBucketsByUserId(@PathVariable Long targetUserId,
		@PageableDefault(sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
		return CommonResponse.ok(storeBucketService.getBucketsByUserId(targetUserId, pageable));
	}

	// 맛집리스트에 있는 맛집 목록 조회(맛집 리스트 조회)
	@GetMapping("/store-buckets/{bucketId}/store-bucket-items")
	public CommonResponse<PageResponse<BucketItemResponse>> getBucketItems(@PathVariable Long bucketId,
		Pageable pageable,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		Long userId = userDetails.getId();
		return CommonResponse.ok(storeBucketService.getBucketItems(bucketId, userId, pageable));
	}

	// 맛집리스트명 수정
	@PatchMapping("/store-buckets/{bucketId}")
	public CommonResponse<StoreBucketResponse> updateBucketName(@PathVariable Long bucketId,
		@RequestParam @NotBlank String name, @AuthenticationPrincipal CustomUserDetails userDetails) {
		Long userId = userDetails.getId();
		return CommonResponse.ok(storeBucketService.updateBucketName(bucketId, name, userId));
	}

	// 맛집리스트 삭제
	@DeleteMapping("/store-buckets/{bucketId}")
	public CommonResponse<Void> deleteBucket(@PathVariable Long bucketId,
		@AuthenticationPrincipal CustomUserDetails userDetails) {
		Long userId = userDetails.getId();
		storeBucketService.deleteBucket(bucketId, userId);
		return CommonResponse.ok();
	}

	// 맛집리스트에서 맛집 삭제
	@DeleteMapping("/store-buckets/{bucketId}/store-bucket-items")
	public CommonResponse<Void> removeBucketItem(@PathVariable Long bucketId,
		@RequestBody @Valid RemoveBucketItemRequest request, @AuthenticationPrincipal CustomUserDetails userDetails) {
		Long userId = userDetails.getId();
		storeBucketService.removeBucketItem(bucketId, request, userId);
		return CommonResponse.ok();
	}

	// 나의 맛집리스트(버킷)에서 키워드 조회
	@GetMapping("/users/me/store-buckets")
	public CommonResponse<PageResponse<StoreBucketResponse>> getMyBuckets(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestParam(required = false) String keyword,
		@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
	) {
		Long userId = userDetails.getId();
		return CommonResponse.ok(storeBucketService.getMyBuckets(userId, keyword, pageable));

	}

	// 내가 팔로우한 사람들의 맛집리스트(버킷)에서 키워드 조회
	// 키워드 없으면 조회결과 많아서 제한(키워드 필수)
	@GetMapping("/users/me/following/store-buckets")
	public CommonResponse<PageResponse<StoreBucketResponse>> getBucketsOfMyFollowings(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestParam @NotBlank(message = "검색 키워드는 필수입니다.") String keyword,
		@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
	) {
		Long userId = userDetails.getId();
		return CommonResponse.ok(storeBucketService.getBucketsOfMyFollowings(userId, keyword, pageable));

	}
}
