package com.example.taste.fixtures;

import com.example.taste.domain.store.entity.StoreBucket;
import com.example.taste.domain.user.entity.User;

public class StoreBucketFixture {
	public static StoreBucket createOpenedBucket(User user) {
		return StoreBucket.builder()
			.user(user)
			.name("기본 리스트")
			.isOpened(true)
			.build();
	}

	public static StoreBucket createClosedBucket(User user) {
		return StoreBucket.builder()
			.user(user)
			.name("기본 리스트")
			.isOpened(true)
			.build();
	}
}
