package com.example.taste.domain.store.controller;

import java.util.List;

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

	@PostMapping("/store-buckets")
	public CommonResponse<StoreBucketResponse> createBucket(@RequestBody @Valid CreateBucketRequest request) {
		Long userId = 1L; // Todo : 세션에서 추출
		return CommonResponse.created(storeBucketService.createBucket(request, userId));
	}

	@PostMapping("/store-buckets/store-bucket-items")
	public CommonResponse<Void> addBucketItem(@RequestBody @Valid AddBucketItemRequest request) {
		Long userId = 1L;
		storeBucketService.addBucketItem(request, userId);
		return CommonResponse.ok();
	}

	@GetMapping("/users/{targetUserId}/store-buckets")
	public CommonResponse<List<StoreBucketResponse>> getBucketsByUserId(@PathVariable Long targetUserId) {
		return CommonResponse.ok(storeBucketService.getBucketsByUserId(targetUserId));
	}

	@GetMapping("/store-buckets/{bucketId}/store-bucket-items")
	public CommonResponse<List<BucketItemResponse>> getBucketItems(@PathVariable Long bucketId) {
		Long userId = 1L;
		return CommonResponse.ok(storeBucketService.getBucketItems(bucketId, userId));
	}

	@PatchMapping("/store-buckets/{bucketId}")
	public CommonResponse<StoreBucketResponse> updateBucketName(@PathVariable Long bucketId,
		@RequestParam @NotBlank String name) {
		Long userId = 1L;
		return CommonResponse.ok(storeBucketService.updateBucketName(bucketId, name, userId));
	}

	@DeleteMapping("/store-buckets/{bucketId}")
	public CommonResponse<Void> deleteBucket(@PathVariable Long bucketId) {
		Long userId = 1L;
		storeBucketService.deleteBucket(bucketId, userId);
		return CommonResponse.ok();
	}

	@DeleteMapping("/store-buckets/{bucketId}/store-bucket-items")
	public CommonResponse<Void> removeBucketItem(@PathVariable Long bucketId,
		@RequestBody @Valid RemoveBucketItemRequest request) {
		Long userId = 1L;
		storeBucketService.removeBucketItem(bucketId, request, userId);
		return CommonResponse.ok();
	}
}
