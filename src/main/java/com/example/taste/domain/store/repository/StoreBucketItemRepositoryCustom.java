package com.example.taste.domain.store.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.taste.domain.store.dto.response.BucketItemResponse;
import com.example.taste.domain.store.entity.StoreBucket;

public interface StoreBucketItemRepositoryCustom {
	Page<BucketItemResponse> findAllByStoreBucket(StoreBucket storeBucket, Pageable pageable);
}
