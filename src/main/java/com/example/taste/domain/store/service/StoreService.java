package com.example.taste.domain.store.service;

import com.example.taste.domain.store.dto.request.AddBucketItemRequest;
import com.example.taste.domain.store.dto.request.CreateBucketRequest;
import com.example.taste.domain.store.dto.response.StoreBucketResponse;
import com.example.taste.domain.store.dto.response.StoreResponse;

public interface StoreService {
	StoreBucketResponse createBucket(CreateBucketRequest request, Long userId);

	void addBucketItem(AddBucketItemRequest request);

	StoreResponse getStore(Long id);
}
