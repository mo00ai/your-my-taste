package com.example.taste.fixtures;

import com.example.taste.domain.store.entity.Category;

public class CategoryFixture {
	public static Category create() {
		return Category.builder()
			.name("testCategory")
			.displayOrder(0)
			.build();
	}
}
