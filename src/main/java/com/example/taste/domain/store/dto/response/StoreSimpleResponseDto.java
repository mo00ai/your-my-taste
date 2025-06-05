package com.example.taste.domain.store.dto.response;

import lombok.Builder;
import lombok.Getter;

import com.example.taste.domain.store.entity.Store;

@Getter
public class StoreSimpleResponseDto {
	private Long storeId;
	private String name;

	@Builder
	public StoreSimpleResponseDto(Store store) {
		this.storeId = store.getId();
		this.name = store.getName();
	}
}
