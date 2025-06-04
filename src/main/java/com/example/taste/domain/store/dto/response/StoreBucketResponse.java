package com.example.taste.domain.store.dto.response;

import com.example.taste.domain.store.entity.StoreBucket;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoreBucketResponse {
	private Long id;
	private Long userId;
	private String name;
	private boolean isOpened;

	public static StoreBucketResponse from(StoreBucket storeBucket) {
		return StoreBucketResponse.builder()
			.id(storeBucket.getId())
			.userId(storeBucket.getUser().getId())
			.name(storeBucket.getName())
			.isOpened(storeBucket.isOpened())
			.build();
	}
}
