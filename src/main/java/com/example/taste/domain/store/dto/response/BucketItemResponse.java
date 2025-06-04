package com.example.taste.domain.store.dto.response;

import java.math.BigDecimal;

import com.example.taste.domain.store.entity.StoreBucketItem;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BucketItemResponse {
	private Long id;
	private Long storeId;
	private String name;
	private BigDecimal mapx;
	private BigDecimal mapy;

	public static BucketItemResponse from(StoreBucketItem item) {
		return BucketItemResponse.builder()
			.id(item.getId())
			.storeId(item.getStore().getId())
			.name(item.getStore().getName())
			.mapx(item.getStore().getMapx())
			.mapy(item.getStore().getMapy())
			.build();
	}
}