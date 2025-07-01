package com.example.taste.domain.store.dto.response;

import java.math.BigDecimal;

import lombok.Getter;

@Getter
public class BucketItemResponse {
	private final Long id;
	private final Long storeId;
	private final String name;
	private final BigDecimal mapx;
	private final BigDecimal mapy;

	public BucketItemResponse(Long id, Long storeId, String name, BigDecimal mapx, BigDecimal mapy) {
		this.id = id;
		this.storeId = storeId;
		this.name = name;
		this.mapx = mapx;
		this.mapy = mapy;
	}
}