package com.example.taste.domain.store.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.taste.common.response.CommonResponse;
import com.example.taste.domain.store.dto.AddStoreRequest;
import com.example.taste.domain.store.dto.CreateBucketRequest;
import com.example.taste.domain.store.dto.StoreBucketResponse;
import com.example.taste.domain.store.service.StoreService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class StoreBucketController {
	private final StoreService storeService;

	@PostMapping("/store-buckets")
	public CommonResponse<StoreBucketResponse> createBucket(@RequestBody CreateBucketRequest request) {
		Long userId = 1L; // Todo : 세션에서 추출
		return CommonResponse.created(storeService.createBucket(request, userId));
	}

	@PostMapping("/store-buckets/store-bucket-items")
	public CommonResponse<Void> addStore(@RequestBody AddStoreRequest request) {
		storeService.addStore(request);
		return CommonResponse.ok();
	}
}
