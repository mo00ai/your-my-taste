package com.example.taste.domain.store.service;

import com.example.taste.domain.store.dto.CreateBucketRequest;
import com.example.taste.domain.store.dto.StoreBucketResponse;

public interface StoreService{
	StoreBucketResponse createBucket(CreateBucketRequest request, Long userId);
}
