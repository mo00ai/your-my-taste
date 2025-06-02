package com.example.taste.domain.store.dto;

import com.example.taste.domain.store.entity.StoreBucket;

import lombok.Getter;

@Getter
public class StoreBucketResponse {
	private Long id;
	private Long userId;
	private String name;
	private boolean isOpened;

	public StoreBucketResponse(StoreBucket storeBucket){
		this.id = storeBucket.getId();
		this.userId = storeBucket.getUser().getId();
		this.name = storeBucket.getName();
		this.isOpened = storeBucket.isOpened();
	}
}
