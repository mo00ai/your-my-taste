package com.example.taste.fixtures;

import java.math.BigDecimal;
import java.util.UUID;

import com.example.taste.domain.store.entity.Category;
import com.example.taste.domain.store.entity.Store;

public class StoreFixture {
	public static Store create(Category category) {
		return Store.builder()
			.category(category)
			.name("testStore" + UUID.randomUUID())
			.description("testDescription")
			.address("서울특별시 00구 00동")
			.roadAddress("서울특별시 00대로")
			.mapx(BigDecimal.valueOf(36.123))
			.mapy(BigDecimal.valueOf(36.123))
			.build();
	}
}
