package com.example.taste.fixtures;

import com.example.taste.domain.store.entity.Store;
import com.example.taste.domain.store.entity.StoreBucket;
import com.example.taste.domain.store.entity.StoreBucketItem;

public class StoreBucketItemFixture {
	public static StoreBucketItem create(StoreBucket bucket, Store store) {
		return StoreBucketItem.builder()
			.storeBucket(bucket)
			.store(store)
			.build();
	}
}
