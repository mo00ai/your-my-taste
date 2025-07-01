package com.example.taste.domain.store.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
public class StoreSearchResult {
	private Long id;
	private String name;
	private String address;
	private String categoryName;
	private Double similarity; // 유사도 점수

	@Builder
	public StoreSearchResult(Long id, String name, String address, String categoryName, Double similarity) {
		this.id = id;
		this.name = name;
		this.address = address;
		this.categoryName = categoryName;
		this.similarity = similarity;
	}
}
