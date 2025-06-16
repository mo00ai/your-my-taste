package com.example.taste.fixtures;

import java.util.UUID;

import com.example.taste.domain.store.entity.Category;

public class CategoryFixture {
	public static Category create() {
		return Category.builder()
			.name("testCategory" + UUID.randomUUID())
			.displayOrder(0)
			.build();
	}
}
